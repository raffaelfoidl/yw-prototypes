package org.yesworkflow.annotations.util;

import org.yesworkflow.annotations.Annotation;

import java.util.ArrayList;
import java.util.List;

public class AnnotationLine {
    private List<Annotation> annotations;

    public AnnotationLine() {
        this.annotations = new ArrayList<>();
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

}
