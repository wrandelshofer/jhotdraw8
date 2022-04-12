/*
 * @(#)HandleTracker.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.handle.Handle;

import java.util.Collection;

/**
 * A <em>handle tracker</em> provides the behavior for manipulating a
 * {@link Handle} of a figure to the {@link SelectionTool}.
 *
 * @author Werner Randelshofer
 */
public interface HandleTracker extends Tracker {

    void setHandles(@Nullable Handle handle, Collection<Figure> compatibleFigures);

}
