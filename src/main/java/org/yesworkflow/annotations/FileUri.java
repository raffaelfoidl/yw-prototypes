package org.yesworkflow.annotations;

import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.yesworkflow.YWKeywords;

import java.util.function.Function;

public class FileUri extends UriAnnotation {
    
    public FileUri(Long id, Long sourceId, Long lineNumber, String comment, Annotation primaryAnnotation) throws Exception {
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.FILE, primaryAnnotation);
    }
    
    public String toString() {
        return value;
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        return null;
    }
}

