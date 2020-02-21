package org.yesworkflow.extract;

import org.yesworkflow.annotations.*;

import java.util.ArrayList;
import java.util.List;

public class AnnotationBlockBuilder {

    private enum NewAnnotationDestination {
        /**
         * A new block is started for the new annotation (= first element of first line of new block)
         */
        BLOCK,

        /**
         * A new line is started for the new annotation (= first element of last line of last block)
         */
        LINE,

        /**
         * The new annotation is simply appended (= last element of last line of last block)
         */
        APPEND,

        /**
         * Do not add the new annotation
         */
        SKIP
    }

    public static final String EOL = System.getProperty("line.separator");

    private List<AnnotationBlock> blocks;
    private boolean lastAnnotationWasEnd = false;

    public static AnnotationBlockBuilder build(List<Annotation> annotations) {
        AnnotationBlockBuilder abb = new AnnotationBlockBuilder();

        abb.blocks = new ArrayList<>();
        for (Annotation annotation : annotations) {
            abb.add(annotation);
        }

        return abb;
    }

    private void add(Annotation annotation) {
        NewAnnotationDestination type = NewAnnotationDestination.BLOCK;

        // append annotation to last line if on the same line
        // add annotation to a new block if this annotation starts a new one
        // otherwise: add annotation to a new line (= last line of last block)
        if (this.blocks.size() > 0) {
            if (annotation instanceof Qualification) {
                type = NewAnnotationDestination.APPEND;
            } else {
                type = NewAnnotationDestination.LINE;
                if (annotation instanceof Begin)
                    type = NewAnnotationDestination.BLOCK;
            }
        }

        // skip second end annotation between consecutive End comments
        if (annotation instanceof End && lastAnnotationWasEnd) {
            type = NewAnnotationDestination.SKIP;
        }


        // insert the comment source for the annotation
        addAnnotation(type, annotation);

        lastAnnotationWasEnd = annotation instanceof End;
    }

    private void addAnnotation(NewAnnotationDestination destination, Annotation annotation) {
        if (this.blocks == null) {
            this.blocks = new ArrayList<>();
        }

        switch (destination) {
            case BLOCK:
                this.blocks.add(new AnnotationBlock());
                this.blocks.get(this.blocks.size() - 1).getLines().add(new AnnotationLine());
                this.blocks.get(this.blocks.size() - 1).getLastLine().getAnnotations().add(annotation);
                break;

            case LINE:
                this.blocks.get(this.blocks.size() - 1).getLines().add(new AnnotationLine());
                this.blocks.get(this.blocks.size() - 1).getLastLine().getAnnotations().add(annotation);
                break;

            case APPEND:
                this.blocks.get(this.blocks.size() - 1).getLastLine().getAnnotations().add(annotation);
                break;
        }
    }

    public List<AnnotationBlock> get() {
        return this.blocks;
    }

}
