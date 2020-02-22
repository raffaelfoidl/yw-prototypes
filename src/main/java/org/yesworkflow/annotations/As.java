package org.yesworkflow.annotations;

import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.yesworkflow.YWKeywords;

import java.util.function.Function;

public class As extends Qualification {

    public As(Long id, Long sourceId, Long lineNumber, String comment, Annotation primaryAnnotation) throws Exception {
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.AS, primaryAnnotation);
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        return provFactory.newAlternateOf(qualifierMethod.apply(this.value().trim()),
                qualifierMethod.apply(this.primaryAnnotation.value().trim()));
    }
}

