package org.yesworkflow.annotations;

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
}

