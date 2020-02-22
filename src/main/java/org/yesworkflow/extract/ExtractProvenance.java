package org.yesworkflow.extract;

import org.openprovenance.prov.interop.Formats;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.yesworkflow.annotations.*;
import org.yesworkflow.annotations.util.AnnotationBlock;
import org.yesworkflow.annotations.util.AnnotationLine;
import org.yesworkflow.exceptions.YWToolUsageException;

import java.util.*;

class ExtractProvenance {

    private final ProvFactory provFactory;
    private final String namespacePrefix;


    private final Namespace namespace;
    private final List<AnnotationBlock> blocks;
    private final String file;
    private final String fileExtension;
    private final Formats.ProvFormat fileFormat;

    ExtractProvenance(List<AnnotationBlock> blocks, String file, String format, String namespace, String prefix) throws YWToolUsageException {
        List<String> allowedFormats = Arrays.asList("PROVN", "TURTLE", "XML", "TRIG", "JSON", "PDF", "SVG", "DOT", "PNG", "JPEG");
        if (!allowedFormats.contains(format.toUpperCase())) {
            throw new YWToolUsageException("Invalid provenance output format '" + format.toUpperCase() + "'");
        }

        this.fileFormat = Formats.ProvFormat.valueOf(format.toUpperCase());
        this.fileExtension = getExtension(this.fileFormat);
        this.blocks = blocks;
        this.file = file;
        this.provFactory = InteropFramework.getDefaultFactory();
        this.namespacePrefix = prefix;


        this.namespace = new Namespace();
        this.namespace.addKnownNamespaces();
        this.namespace.register(prefix, namespace);
    }

    private String getExtension(Formats.ProvFormat outputFormat) throws YWToolUsageException {
        switch (outputFormat) {
            case PROVN:
                return ".pn";
            case XML:
                return ".xml";
            case TURTLE:
                return ".ttl";
            case RDFXML:
                throw new YWToolUsageException("attempted to write to unsupported format '" + Formats.ProvFormat.RDFXML + "'");
            case TRIG:
                return ".trig";
            case JSON:
                return ".json";
            case JSONLD:
                throw new YWToolUsageException("attempted to write to unsupported format '" + Formats.ProvFormat.JSONLD + "'");
            case DOT:
                return ".gv";
            case JPEG:
                return ".jpeg";
            case PNG:
                return ".png";
            case SVG:
                return ".svg";
            case PDF:
                return ".pdf";
            default:
                throw new YWToolUsageException("attempted to write to unsupported format '<none>'");
        }
    }

    private String escapeForQName(String input) {
        // escaped because replaceAll takes regex
        String[] forbiddenChars = new String[]{"=", "'", "\\(", "\\)", ",", "_", ":", ";", "\\[", "\\]", "/",
                "\\\\", "\\?", "@", "~", "&", "\\+", "\\*", "#", "\\$", "\\^", "!", "<", ">", "%"};
        String returnValue = input;

        for (String forbiddenChar : forbiddenChars) {
            returnValue = returnValue.replaceAll(forbiddenChar, "_");
        }

        return returnValue;
    }

    private QualifiedName qualifiedName(String name) {
        return namespace.qualifiedName(this.namespacePrefix, escapeForQName(name), provFactory);
    }

    private QualifiedName qualifiedName(String name1, String name2, String connector) {
        return namespace.qualifiedName(this.namespacePrefix, String.format("%s__%s__%s",
                escapeForQName(name1), connector, escapeForQName(name2)), provFactory);
    }

    private Document createDocument() {
        // use map/set in order to guarantee uniqueness by ID (qualified name)
        Map<QualifiedName, StatementOrBundle> elements = new HashMap<>(); // contains all
        Set<StatementOrBundle> anonymousElements = new HashSet<>(); // contains those not implementing Identifiable

        // convert found annotations to provenance information
        for (AnnotationBlock block : this.blocks) {
            for (AnnotationLine line : block.getLines()) {
                for (Annotation annotation : line.getAnnotations()) {
                    StatementOrBundle prov = annotation.getProvenanceInfo(provFactory, this::qualifiedName);
                    if (prov == null && !(annotation instanceof Log))
                        continue;

                    if (prov instanceof Identifiable)
                        handleIdentifiable(elements, annotation, prov);
                    else if (prov instanceof AlternateOf)
                        anonymousElements.add(prov);

                    if (annotation instanceof Out) // Return extends Out => no extra disjunction necessary
                        handleOut(elements, block, prov);

                    if (annotation instanceof In) // Param extends In => no extra disjunction necessary
                        handleIn(elements, block, prov);

                    if (annotation instanceof Call)
                        handleCall(elements, block, prov);

                    if (annotation instanceof Log)
                        handleLog(elements, block, annotation);
                }
            }
        }

        Document doc = provFactory.newDocument();
        doc.getStatementOrBundle().addAll(elements.values());
        doc.getStatementOrBundle().addAll(anonymousElements);
        doc.setNamespace(namespace);

        return doc;
    }

    /**
     * Add content of Log-annotation to current activity as label
     */
    private void handleLog(Map<QualifiedName, StatementOrBundle> elements, AnnotationBlock block, Annotation annotation) {
        QualifiedName activityId = qualifiedName(block.getBegin().value());
        StatementOrBundle activity = elements.get(activityId);

        if (!(activity instanceof Activity))
            return;

        String logMessage = annotation.value();
        Qualification annQual = (Qualification) annotation;
        String logEntity = annQual.primaryAnnotation.value();

        // if logged entity has an alias, use the alias as a log_-suffix instead of the real name
        if (annQual.primaryAnnotation instanceof AliasableAnnotation) {
            AliasableAnnotation annAlias = (AliasableAnnotation) annQual.primaryAnnotation;
            if (annAlias.alias() != null)
                logEntity = annAlias.alias();
        }

        addDescriptionLabel(String.format("log_%s: %s", logEntity, logMessage), ((Activity) activity).getLabel());
    }

    /**
     * Connect called functions/methods with their callers.
     */
    private void handleCall(Map<QualifiedName, StatementOrBundle> elements, AnnotationBlock block, StatementOrBundle prov) {
        if (!(prov instanceof Entity))
            return;

        QualifiedName entityId = ((Entity) prov).getId();
        QualifiedName activityId = qualifiedName(block.getBegin().value());
        QualifiedName genId = qualifiedName(entityId.getLocalPart(), activityId.getLocalPart(), "CALL");

        Used use = provFactory.newUsed(genId, activityId, entityId);
        elements.put(use.getId(), use);
    }

    /**
     * Connect outputs with activities.
     */
    private void handleOut(Map<QualifiedName, StatementOrBundle> elements, AnnotationBlock block, StatementOrBundle prov) {
        if (!(prov instanceof Entity))
            return;

        QualifiedName entityId = ((Entity) prov).getId();
        QualifiedName activityId = qualifiedName(block.getBegin().value());
        QualifiedName genId = qualifiedName(entityId.getLocalPart(), activityId.getLocalPart(), "GEN");

        WasGeneratedBy gen = provFactory.newWasGeneratedBy(genId, entityId, activityId);
        elements.put(gen.getId(), gen);
    }

    /**
     * Connect inputs with activities.
     */
    private void handleIn(Map<QualifiedName, StatementOrBundle> elements, AnnotationBlock block, StatementOrBundle prov) {
        if (!(prov instanceof Entity))
            return;

        QualifiedName entityId = ((Entity) prov).getId();
        QualifiedName activityId = qualifiedName(block.getBegin().value());
        QualifiedName genId = qualifiedName(entityId.getLocalPart(), activityId.getLocalPart(), "USE");

        Used use = provFactory.newUsed(genId, activityId, entityId);
        elements.put(use.getId(), use);
    }

    /**
     * Handle provenance information elements that implement the Identifiable interface.
     */
    private void handleIdentifiable(Map<QualifiedName, StatementOrBundle> elements, Annotation annotation, StatementOrBundle prov) {
        QualifiedName key = ((Identifiable) prov).getId();
        StatementOrBundle current = elements.getOrDefault(key, null);

        // if no element with this ID stored, add to map regularly
        if (current == null) {
            elements.put(key, prov);

        } else if (current instanceof HasLabel) {
            HasLabel currentWithLabel = (HasLabel) current;

            /*  if element currently stored with this ID does not have a label, replace it by this one
                if current provenance info has a label, add them as well as the current description
                else, add to map regularly */
            if (currentWithLabel.getLabel() == null || currentWithLabel.getLabel().size() < 1) {
                elements.put(key, prov);

            } else if (prov instanceof HasLabel) {
                addLabels(((HasLabel) prov).getLabel(), currentWithLabel.getLabel());
                addDescriptionLabel(annotation.descriptionClean(), currentWithLabel.getLabel());

            } else {
                elements.put(key, prov);
            }

            // if there is an element with this ID stored, but without description, replace it by this one
        } else if (annotation.description() != null) {
            elements.put(key, prov);
        }
    }

    private void addLabels(List<LangString> provLabels, List<LangString> currentLabels) {
        if (currentLabels == null)
            currentLabels = new ArrayList<>();

        // only add labels that have not yet been stored (Object.equals() with streams is necessary since
        // difference implementations of LangString are not equal despite having the same underlying string value)
        if (provLabels != null && provLabels.size() > 0) {
            List<LangString> finalCurrentLabels = currentLabels;
            provLabels.stream().filter(x ->
                    finalCurrentLabels.stream().noneMatch(y -> Objects.equals(y.getValue(), x.getValue())))
                    .forEach(currentLabels::add);
        }
    }

    private void addDescriptionLabel(String annotationDesc, List<LangString> currentLabels) {
        if (annotationDesc == null)
            return;

        if (currentLabels == null)
            currentLabels = new ArrayList<>();

        // only add description as label if has not yet been stored (Object.equals() with streams is necessary since
        // difference implementations of LangString are not equal despite having the same underlying string value)
        LangString descLabel = new org.openprovenance.prov.vanilla.LangString(annotationDesc);
        if (currentLabels.stream().noneMatch(x -> Objects.equals(x.getValue(), descLabel.getValue())))
            currentLabels.add(descLabel);
    }

    void saveFile() {
        InteropFramework interopFx = new InteropFramework();
        Document provenance = this.createDocument();
        interopFx.writeDocument(String.join("", this.file, this.fileExtension), this.fileFormat, provenance);
    }

}
