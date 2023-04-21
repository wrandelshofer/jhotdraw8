/*
 * @(#)LockedFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

/**
 * Interface for figures which are always locked.
 *
 * @author Werner Randelshofer
 */
public interface LockedFigure extends Figure {

    /**
     * Whether this figure is not locked and all its parents are editable.
     *
     * @return false
     */
    @Override
    default boolean isEditable() {
        return false;
    }

    /**
     * Whether the figure is not locked and all its parents are editable.
     *
     * @return false
     */
    @Override
    default boolean isDeletable() {
        return false;
    }

    /**
     * Whether the figure is not locked and all its parents are editable.
     *
     * @return false
     */
    @Override
    default boolean isSelectable() {
        return false;
    }
}
