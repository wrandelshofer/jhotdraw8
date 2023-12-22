/*
 * @(#)ChildLayoutingFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.figure;

import org.jhotdraw8.draw.render.RenderContext;

/**
 * Marker interface to all figures that layout their children, when
 * method {@link Figure#layout(RenderContext)} is called.
 */
public interface ChildLayoutingFigure extends Figure {
}
