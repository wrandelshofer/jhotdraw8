/*
 * @(#)Clipping.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;


/// Defines a _clipping_ of a [Drawing].
///
/// A clipping is used to hold a selection of figures, so that they can be read
/// or written to the clipboard.
///
/// A clipping can not have a parent, and thus returns false in
/// isSuitableParent(parent) for all parents except null.
public interface Clipping extends Figure {

    /// The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
    String TYPE_SELECTOR = "Clipping";

    /// Clipping figures always return false for isSelectable.
    @Override
    default boolean isSelectable() {
        return false;
    }

    @Override
    default String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
