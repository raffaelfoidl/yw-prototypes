package org.yesworkflow.annotations;

import org.openprovenance.prov.model.*;
import org.openprovenance.prov.vanilla.Type;
import org.yesworkflow.YWKeywords;
import org.yesworkflow.YWKeywords.Tag;

import java.util.function.Function;

public class Call extends Annotation {

    public Call(Long id, Long sourceId, Long lineNumber, String comment, Tag expectedTag) throws Exception {
        super(id, sourceId, lineNumber, comment, expectedTag);
    }

    public Call(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.CALL);
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(keyword)
                .append("{value=")
                .append(value);

        if (description() != null) {
            sb.append(",description=")
                    .append(description());
        }

        sb.append("}");

        return sb.toString();
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        Entity entity = provFactory.newEntity(qualifierMethod.apply(this.value().trim()));
        String desc = this.descriptionClean();
        if (desc != null)
            entity.getLabel().add(provFactory.newInternationalizedString(desc));

        entity.getType().add(new Type(null, "calledFunction"));
        return entity;
    }
}

