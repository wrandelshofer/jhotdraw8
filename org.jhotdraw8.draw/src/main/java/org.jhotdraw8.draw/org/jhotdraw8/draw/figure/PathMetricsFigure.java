/*
 * @(#)PathIterableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.figure;

import org.jhotdraw8.geom.shape.PathMetrics;

/**
 * PathMetricsFigure.
 *
 * @author Werner Randelshofer
 */
public interface PathMetricsFigure extends Figure {
    /**
     * Gets the path metrics of this figure in local coordinates.
     */
    PathMetrics getPathMetrics();
}
