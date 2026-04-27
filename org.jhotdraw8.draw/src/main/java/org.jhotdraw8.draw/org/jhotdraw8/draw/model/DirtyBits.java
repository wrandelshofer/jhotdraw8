/*
 * @(#)DirtyBits.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.model;

/// `DirtyBits` describes how changing a property value of a `Figure`
/// affects dependent objects.
public enum DirtyBits {

    /// Affects the state of the figure.
    ///
    /// All objects which depend on the state of the figure need to be updated.
    STATE,
    /// Affects the JavaFX `Node` created by the figure.
    ///
    /// All cached JavaFX `Node`s created by the figure need to be updated.
    NODE,
    /// Affects the layout of the figure, the layout of its ancestors and the
    /// layout of layout observing figures.
    ///
    /// Method `Figure#layoutNotify` must be called on the figure, then in
    /// ascending order on all its ancestors which perform layout, and then on
    /// all dependent figures and their ancestors.
    LAYOUT,
    /// Affects the layout of layout observing figures.
    ///
    /// Method `Figure#layoutNotify` must be called on all dependent
    /// figures and their ancestors.
    LAYOUT_OBSERVERS,
    /// Affects the style of the figure.
    ///
    /// Method `Figure#stylesheetNotify` must be called on the figure and
    /// all its descendants.
    STYLE,
    /// Affects the layout subject(s) of the figure.
    ///
    /// Method `Figure#layoutSubjectChangeNotify` must be called on the figure.
    LAYOUT_SUBJECT,
    /// Affects a figure which is layout subject of other figures.
    ///
    /// Method `Figure#layoutObserverChangeNotify` must be called on the figure.
    LAYOUT_OBSERVERS_ADDED_OR_REMOVED,
    /// Affects the transform of the figure and all descendant figures.
    ///
    /// Method `Figure#transformNotify` must be called on the figure and all its descendant figures.
    TRANSFORM,
    /// This is internally used by DrawingModel for marking figures which need
    /// transformNotify.
    ///
    /// Method `Figure#transformNotify` must be called on the figure.
    TRANSFORM_NOTIFY;

    private final int mask;

    DirtyBits() {
        mask = 1 << ordinal();
    }

    /// Interface for DirtyMask.
    final int getMask() {
        return mask;
    }

}
