package org.yesworkflow.extract;

import org.openprovenance.prov.interop.Formats;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.yesworkflow.annotations.Annotation;
import org.yesworkflow.annotations.In;
import org.yesworkflow.annotations.Out;
import org.yesworkflow.annotations.Return;
import org.yesworkflow.annotations.util.AnnotationBlock;
import org.yesworkflow.annotations.util.AnnotationLine;

import java.util.*;

class ExtractProvenance {

    private static final String YW_NS = "http://yesworkflow.org/";
    private static final String YW_PREFIX = "yw";


    private final ProvFactory provFactory;
    private final Namespace namespace;
    private final List<AnnotationBlock> blocks;
    private final String filePath;

    ExtractProvenance(List<AnnotationBlock> blocks, String filePath) {
        this.blocks = blocks;
        this.filePath = filePath;
        this.provFactory = InteropFramework.getDefaultFactory();

        namespace = new Namespace();
        namespace.addKnownNamespaces();
        namespace.register(YW_PREFIX, YW_NS);
    }

    private QualifiedName qualifiedName(String name) {
        return namespace.qualifiedName(YW_PREFIX, name, provFactory);
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
                        QualifiedName entityId = ((Entity)prov).getId();
                        QualifiedName activityId = qualifiedName(block.getBegin().value());
                        QualifiedName wgbId = qualifiedName(entityId.getLocalPart() + "__GEN__" + activityId.getLocalPart());

                        WasGeneratedBy wgb = provFactory.newWasGeneratedBy(wgbId, entityId, activityId);
                        elements.put(wgbId, wgb);
                    }

                    // connect inputs with activities
                    // Param annotation extends In => no need for extra disjunctive filter term
                    if (annotation instanceof In) {
                        QualifiedName entityId = ((Entity)prov).getId();
                        QualifiedName activityId = qualifiedName(block.getBegin().value());
                        QualifiedName useId = qualifiedName(entityId.getLocalPart() + "__USE__" + activityId.getLocalPart());

                        Used use = provFactory.newUsed(useId, activityId, entityId);
                        elements.put(useId, use);
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

        interopFx.writeDocument(System.out, Formats.ProvFormat.PROVN, provenance);
        interopFx.writeDocument(this.filePath.replace(".txt", ".pdf"), Formats.ProvFormat.PDF, provenance);
    }

}
