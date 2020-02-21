package org.yesworkflow.extract;

import org.openprovenance.prov.interop.Formats;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.*;
import org.yesworkflow.annotations.AnnotationBlock;

import java.util.Arrays;
import java.util.List;

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
        Entity quote = provFactory.newEntity(qualifiedName("a-little-provenance-goes-a-long-way"));
        quote.setValue(provFactory.newValue("A little provenance goes a long way", provFactory.getName().XSD_STRING));

        Entity original = provFactory.newEntity(namespace.qualifiedName(JIM_PREFIX, "LittleSemanticsWeb.html", provFactory));

        Agent paul = provFactory.newAgent(qualifiedName("Paul"), "Paul Groth");
        Agent luc = provFactory.newAgent(qualifiedName("Luc"), "Luc Moreau");

        WasAttributedTo attr1 = provFactory.newWasAttributedTo(null, quote.getId(), paul.getId());
        WasAttributedTo attr2 = provFactory.newWasAttributedTo(null, quote.getId(), luc.getId());

        WasDerivedFrom wdf = provFactory.newWasDerivedFrom(quote.getId(), original.getId());

        Document document = provFactory.newDocument();
        document.getStatementOrBundle().addAll(Arrays.asList(quote, paul, luc, attr1, attr2, original, wdf));
        document.setNamespace(namespace);
        return document;
    }

    void saveFile() {
        InteropFramework interopFx = new InteropFramework();
        Document provenance = this.createDocument();

        interopFx.writeDocument(this.filePath, Formats.ProvFormat.PROVN, provenance);
        interopFx.writeDocument(System.out, Formats.ProvFormat.PROVN, provenance);
    }

}
