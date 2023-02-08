package org.jhotdraw8.draw.figure;

import org.jhotdraw8.draw.render.RenderContext;

/**
 * Marker interface for all figures that layout their children, when
 * method {@link Figure#layout(RenderContext)} is called.
 */
public interface ChildLayoutingFigure extends Figure {
}
