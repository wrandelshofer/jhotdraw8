/*
 * @(#)HandleTracker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.handle.Handle;

import java.util.Collection;

/**
 * A <em>handle tracker</em> provides the behavior for manipulating a
 * {@link Handle} of a figure to the {@link SelectionTool}.
 * <p>
 * A {@link HandleTracker} should set the {@link Node#accessibleTextProperty()}
 * and {@link Node#accessibleHelpProperty()} of its own {@link Node}, to the
 * values of the corresponding properties in the {@link Node} provided
 * by the current {@link Handle}.
 *
 * @author Werner Randelshofer
 */
public interface HandleTracker extends Tracker {

    void setHandles(@Nullable Handle handle, Collection<Figure> compatibleFigures);

}
