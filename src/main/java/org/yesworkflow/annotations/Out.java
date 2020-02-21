package org.yesworkflow.annotations;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.openprovenance.prov.model.Identifiable;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.yesworkflow.YWKeywords;
import org.yesworkflow.YWKeywords.Tag;

public class Out extends Flow {
    
    protected List<Log> logAnnotations = new LinkedList<Log>();
    
    public Out(Long id,Long sourceId, Long lineNumber, String comment) throws Exception {        
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.OUT);
    }

    public Out(Long id, Long sourceId, Long lineNumber, String comment, Tag tag) throws Exception {
        super(id, sourceId, lineNumber, comment, tag);
    }    

    @Override
    public Flow qualifyWith(Qualification qualification) throws Exception {
        
        if (qualification instanceof Log) {
            this.logAnnotations.add((Log)qualification);
        } else {
            super.qualifyWith(qualification);
        }
        
        return this;
    }    
    
    public List<Log> logAnnotations() {
        return logAnnotations;
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        String desc = this.uriAnnotation() != null ? this.uriAnnotation().value() : null;
        if (desc == null && this.description() != null) // uri annotation has precedence over a possible desc annotation
            desc = this.description();

        return provFactory.newEntity(qualifierMethod.apply(escapeForQName(this.value().trim())), desc);
    }
}
