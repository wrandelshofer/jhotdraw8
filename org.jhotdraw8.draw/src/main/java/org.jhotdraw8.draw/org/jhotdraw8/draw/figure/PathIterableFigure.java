/*
 * @(#)PathIterableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.figure;

import org.jhotdraw8.draw.render.RenderContext;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * PathIterableFigure.
 *
 * @author Werner Randelshofer
 */
public interface PathIterableFigure extends Figure {
    PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx);
}
