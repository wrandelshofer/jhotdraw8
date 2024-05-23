/*
 * @(#)Layer.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;


/**
 * Defines a <i>layer</i> of a {@link Drawing}.
 * <p>
 * The parent of a {@code Layer} must be a {@code Drawing} or a {@code Clipping} . Method
 * {@link #isSuitableParent(Figure)} must be
 * implementend accordingly.
 * <p>
 * A layer does not have handles and is not selectable.
 *
 * @author Werner Randelshofer
 */
public interface Layer extends Figure {

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    String TYPE_SELECTOR = "Layer";

    /**
     * Layer figures always return false for isSelectable.
     */
    @Override
    default boolean isSelectable() {
        return false;
    }

    @Override
    default String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    default boolean isAllowsChildren() {
        return true;
    }

    @Override
    default boolean isSuitableParent(Figure newParent) {
        return (newParent instanceof LayeredDrawing) || (newParent instanceof Clipping);
    }
}
