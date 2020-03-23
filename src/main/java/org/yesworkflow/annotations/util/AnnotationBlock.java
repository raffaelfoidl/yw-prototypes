package org.yesworkflow.annotations.util;

import org.yesworkflow.YWKeywords;
import org.yesworkflow.annotations.Annotation;

import java.util.ArrayList;
import java.util.List;

public class AnnotationBlock {

    private List<AnnotationLine> lines;

    public AnnotationBlock() {
        this.lines = new ArrayList<>();
    }

    public List<AnnotationLine> getLines() {
        return lines;
    }

    public AnnotationLine getLastLine() {
        if (this.lines.size() < 1) {
            return null;
        }
        return this.lines.get(this.lines.size() - 1);
    }

    public Annotation getBegin() {
        for (AnnotationLine line : this.lines) {
            for (Annotation annotation : line.getAnnotations()) {
                if (annotation.tag == YWKeywords.Tag.BEGIN)
                    return annotation;
            }
        }

        return null;
    }
}

