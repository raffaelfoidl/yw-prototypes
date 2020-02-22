package org.yesworkflow.annotations;

import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.yesworkflow.YWKeywords.Tag;

import java.util.function.Function;

public abstract class Flow extends AliasableAnnotation {
    	
    protected UriAnnotation uriAnnotation;
    
    public Flow(Long id, Long sourceId, Long lineNumber, String comment, Tag tag) throws Exception {
    	super(id, sourceId, lineNumber, comment, tag);    	
    }
	
    @Override
    public Flow qualifyWith(Qualification qualification) throws Exception {
        
        if (qualification instanceof UriAnnotation) {
            this.uriAnnotation = (UriAnnotation)qualification;
        } else {
            super.qualifyWith(qualification);
        }
        
        return this;
    }

    public UriAnnotation uriAnnotation() {
        return uriAnnotation;
    }

    
    @Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        
        sb.append(keyword)
          .append("{value=")
          .append(value);

        if (as != null) {
            sb.append(",alias=")
              .append(as.value);
        }
        
        if (this.description() != null) {
          sb.append(",description=")
            .append(description());
        }
        
        sb.append("}");
        
        return sb.toString();
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        String desc = this.uriAnnotation() != null ? this.uriAnnotation().value() : null;
        if (desc == null && this.description() != null) // uri annotation has precedence over a possible desc annotation
            desc = this.description();

        String val = this.as != null ? this.as.value().trim() : this.value.trim();

        return provFactory.newEntity(qualifierMethod.apply(val), desc);
    }
}
