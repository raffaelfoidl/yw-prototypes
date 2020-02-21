package org.yesworkflow.extract;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.yesworkflow.annotations.Annotation;
import org.yesworkflow.db.YesWorkflowDB;
import org.yesworkflow.query.QueryEngine;

import java.util.List;

public class ExtractProvenance {

    private YesWorkflowDB ywdb;
    private final List<Annotation> annotations;
    private final QueryEngine queryEngine;
    private String provenance;
    private Document document;


    public ExtractProvenance(YesWorkflowDB ywdb, QueryEngine queryEngine, List<Annotation> annotations) {
        this.ywdb = ywdb;
        this.annotations = annotations;
        this.queryEngine = queryEngine;
    }


    public ExtractProvenance build() {
        ProvenanceCollector.main(new String[] {"dd"});

        this.provenance = "test";
        return this;
    }

    public String provenance() {
        return this.provenance;
    }

}
