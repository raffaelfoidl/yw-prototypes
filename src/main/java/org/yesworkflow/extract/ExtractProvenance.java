package org.yesworkflow.extract;

import org.openprovenance.prov.interop.Formats;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.yesworkflow.annotations.Annotation;
import org.yesworkflow.annotations.In;
import org.yesworkflow.annotations.Out;
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

    private QualifiedName qualifiedName(String name) {
        return namespace.qualifiedName(this.namespacePrefix, name, provFactory);
    }

    private QualifiedName qualifiedName(String name1, String name2, String connector) {
        return namespace.qualifiedName(this.namespacePrefix, String.format("%s__%s__%s", name1, connector, name2), provFactory);
    }

    private WasGeneratedBy getGen(StatementOrBundle entity, AnnotationBlock activityBlock) {
        if (!(entity instanceof Entity))
            return null;

        QualifiedName entityId = ((Entity) entity).getId();
        QualifiedName activityId = qualifiedName(activityBlock.getBegin().value());
        QualifiedName genId = qualifiedName(entityId.getLocalPart(), activityId.getLocalPart(), "GEN");
        return provFactory.newWasGeneratedBy(genId, entityId, activityId);
    }

    private Used getUse(StatementOrBundle entity, AnnotationBlock activityBlock) {
        if (!(entity instanceof Entity))
            return null;

        QualifiedName entityId = ((Entity) entity).getId();
        QualifiedName activityId = qualifiedName(activityBlock.getBegin().value());
        QualifiedName genId = qualifiedName(entityId.getLocalPart(), activityId.getLocalPart(), "USE");
        return provFactory.newUsed(genId, activityId, entityId);
    }

    private Document createDocument() {
        // use map in order to guarantee uniqueness by ID (QName)
        Map<QualifiedName, StatementOrBundle> elements = new HashMap<>(); // contains all

        // convert found annotations to provenance activities (methods/tasks) and entities (in/out/return parameters)
        for (AnnotationBlock block : this.blocks) {
            for (AnnotationLine line : block.getLines()) {
                for (Annotation annotation : line.getAnnotations()) {
                    StatementOrBundle prov = annotation.getProvenanceInfo(provFactory, this::qualifiedName);
                    if (prov == null)
                        continue;

                    QualifiedName key = ((Identifiable) prov).getId();
                    StatementOrBundle current = elements.getOrDefault(key, null);
                    // only potentially overwrite value already stored if the new one contains a label or the one
                    // currently stored does not
                    if (current == null || annotation.description() != null ||
                            (current instanceof HasLabel && ((HasLabel) current).getLabel() == null)) {
                        elements.put(key, prov);
                    }

                    // connect outputs with activities
                    // Return annotation extends Out => no need for extra disjunctive filter term
                    if (annotation instanceof Out) {
                        WasGeneratedBy gen = getGen(prov, block);
                        elements.put(gen.getId(), gen);
                    }

                    // connect inputs with activities
                    // Param annotation extends In => no need for extra disjunctive filter term
                    if (annotation instanceof In) {
                        Used use = getUse(prov, block);
                        elements.put(use.getId(), use);
                    }
                }
            }
        }


        Document doc = provFactory.newDocument();
        doc.getStatementOrBundle().addAll(elements.values());
        doc.setNamespace(namespace);

        return doc;
    }

    void saveFile() {
        InteropFramework interopFx = new InteropFramework();
        Document provenance = this.createDocument();
        interopFx.writeDocument(String.join("", this.file, this.fileExtension), this.fileFormat, provenance);
    }

}
