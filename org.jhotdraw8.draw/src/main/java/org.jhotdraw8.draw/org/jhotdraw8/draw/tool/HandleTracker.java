/*
 * @(#)HandleTracker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.scene.Node;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.handle.Handle;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/// A _handle tracker_ provides the behavior for manipulating a
/// [Handle] of a figure to the [SelectionTool].
///
/// A [HandleTracker] should set the [Node#accessibleTextProperty()]
/// and [Node#accessibleHelpProperty()] of its own [Node], to the
/// values of the corresponding properties in the [Node] provided
/// by the current [Handle].
public interface HandleTracker extends Tracker {

    void setHandles(@Nullable Handle handle, Collection<Figure> compatibleFigures);

}
