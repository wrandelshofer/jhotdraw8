/*
 * @(#)DragTracker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jspecify.annotations.Nullable;

/**
 * A <em>drag tracker</em> provides the behavior for dragging selected figures
 * to the {@link SelectionTool}.
 *
 */
public interface DragTracker extends Tracker {

    void setDraggedFigure(@Nullable Figure f, DrawingView dv);

}
