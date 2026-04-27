/*
 * @(#)LayeredDrawing.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;


/// A layered drawing only accepts [Layer]s as children.
public interface LayeredDrawing extends Drawing {
    /// Only returns true if newChild is a [Layer].
    ///
    /// @param newChild The new child figure.
    /// @return true if instanceof Layer
    @Override
    default boolean isSuitableChild(Figure newChild) {
        return newChild instanceof Layer;
    }
}
