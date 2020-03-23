package org.yesworkflow.annotations;

import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.yesworkflow.YWKeywords;
import org.yesworkflow.YWKeywords.Tag;

import java.util.function.Function;

public class UriAnnotation extends Qualification {
    
    public UriAnnotation(Long id, Long sourceId, Long lineNumber,String comment, Annotation primaryAnnotation) throws Exception {
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.URI, primaryAnnotation);
    }

    protected UriAnnotation(Long id, Long sourceId, Long lineNumber,String comment, Tag tag, Annotation primaryAnnotation) throws Exception {
        super(id, sourceId, lineNumber,comment, tag, primaryAnnotation);
    }
    
    public String toString() {
        return value;
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        return null;
    }
}

