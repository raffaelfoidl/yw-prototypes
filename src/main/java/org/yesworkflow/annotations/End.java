package org.yesworkflow.annotations;

import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.StatementOrBundle;
import org.yesworkflow.YWKeywords;

import java.util.function.Function;

public class End extends Delimiter {

    public End(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.END);
    }

    @Override
    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        return null;
    }
}