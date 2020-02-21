package org.yesworkflow.annotations;

import org.openprovenance.prov.model.Identifiable;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.yesworkflow.YWKeywords;
import org.yesworkflow.YWKeywords.Tag;

import java.util.function.Function;

public class In extends Flow {

    public In(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.IN);
    }

    public In(Long id, Long sourceId, Long lineNumber, String comment, Tag tag) throws Exception {
        super(id, sourceId, lineNumber, comment, tag);
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        String desc = this.uriAnnotation() != null ? this.uriAnnotation().value() : null;
        if (desc == null && this.description() != null) // uri annotation has precedence over a possible desc annotation
            desc = this.description();

        return provFactory.newEntity(qualifierMethod.apply(escapeForQName(this.value().trim())), desc);
    }
}
