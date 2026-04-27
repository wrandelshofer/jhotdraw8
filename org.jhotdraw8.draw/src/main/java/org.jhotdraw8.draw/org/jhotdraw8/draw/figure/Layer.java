/*
 * @(#)Layer.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;


/// Defines a _layer_ of a [Drawing].
///
/// The parent of a `Layer` must be a `Drawing` or a `Clipping` . Method
/// [#isSuitableParent(Figure)] must be
/// implementend accordingly.
///
/// A layer does not have handles and is not selectable.
public interface Layer extends Figure {

    /// The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
    String TYPE_SELECTOR = "Layer";

    /// Layer figures always return false for isSelectable.
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
