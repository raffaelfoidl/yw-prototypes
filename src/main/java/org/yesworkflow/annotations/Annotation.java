package org.yesworkflow.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.function.Function;

import org.openprovenance.prov.model.*;
import org.yesworkflow.YWKeywords.Tag;
import org.yesworkflow.exceptions.YWMarkupException;

public abstract class Annotation {

    public final Long id;
    public final Long sourceId;
    public final Long lineNumber;
    public final String keyword;
    protected String value;
    public final String comment;
    public final Tag tag;

    private Desc description = null;

    public Annotation(Long id, Long sourceId, Long lineNumber, String comment, Tag tag) throws YWMarkupException {

        this.id = id;
        this.sourceId = sourceId;
        this.lineNumber = lineNumber;
        this.comment = comment;
        this.tag = tag;

        StringTokenizer commentTokens = new StringTokenizer(comment);

        keyword = commentTokens.nextToken();
        String expectedKeyword = "@" + tag.toString();
        if (!keyword.equalsIgnoreCase(expectedKeyword)) {
            throw new YWMarkupException("Wrong keyword for " + expectedKeyword.toLowerCase() + " annotation: " + keyword);
        }

        try {
            value = commentTokens.nextToken();
        } catch (NoSuchElementException e) {
            throw new YWMarkupException("No argument provided to " + keyword + " keyword on line " + lineNumber);
        }
    }

    public Annotation qualifyWith(Qualification qualification) throws Exception {
        if (qualification instanceof Desc) {
            this.description = (Desc) qualification;
        }
        return this;
    }

    public String value() {
        return value;
    }

    public String description() {
        return (description != null) ? description.value : null;
    }

    public String descriptionClean() {
        return this.description() != null ? this.description().replaceAll("\\\\n", " ") : null;
    }

    public StatementOrBundle getProvenanceInfo(ProvFactory provFactory, Function<String, QualifiedName> qualifierMethod) {
        return provFactory.newActivity(qualifierMethod.apply(this.value().trim()), this.descriptionClean());
    }

}
