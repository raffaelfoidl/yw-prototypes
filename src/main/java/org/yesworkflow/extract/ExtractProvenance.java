package org.yesworkflow.extract;

import org.openprovenance.prov.interop.Formats;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.yesworkflow.annotations.Annotation;
import org.yesworkflow.annotations.util.AnnotationBlock;
import org.yesworkflow.annotations.util.AnnotationLine;

import java.util.*;

public class ExtractProvenance {

    public static final String YW_NS = "http://yesworkflow.org/";
    public static final String YW_PREFIX = "yw";

    public static final String JIM_PREFIX = "jim";
    public static final String JIM_NS = "http://www.cs.rpi.edu/~hendler/";

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
        namespace.register(JIM_PREFIX, JIM_NS);
    }

    private QualifiedName qualifiedName(String name) {
        return namespace.qualifiedName(YW_PREFIX, name, provFactory);
    }

    private Document createDocument() {
        // use map in order to guarantee uniqueness by ID (QName)
        Map<QualifiedName, StatementOrBundle> elements = new HashMap<>();

        for (AnnotationBlock block : this.blocks) {
            for (AnnotationLine line : block.getLines()) {
                for (Annotation annotation : line.getAnnotations()) {
                    StatementOrBundle prov = annotation.getProvenanceInfo(provFactory, this::qualifiedName);
                    if (prov == null)
                        continue;

                    QualifiedName key = ((Identifiable) prov).getId();
                    StatementOrBundle current = elements.getOrDefault(key, null);
                    // only potentially overwrite formerly stored value if the new one contains a label or the
                    // currently stored one does not
                    if (current == null || annotation.description() != null ||
                            (current instanceof HasLabel && ((HasLabel) current).getLabel() == null)) {
                        elements.put(key, prov);

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
