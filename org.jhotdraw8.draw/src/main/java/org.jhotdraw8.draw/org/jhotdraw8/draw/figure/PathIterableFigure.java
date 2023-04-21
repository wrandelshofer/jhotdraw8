/*
 * @(#)PathIterableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.figure;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.render.RenderContext;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * PathIterableFigure.
 *
 * @author Werner Randelshofer
 */
public interface PathIterableFigure extends Figure {
  @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, @Nullable AffineTransform tx);
}
