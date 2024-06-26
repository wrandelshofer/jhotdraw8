/*
 * @(#)ContourPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.geom.contour.ContourBuilder;
import org.jhotdraw8.geom.contour.PlinePath;
import org.jhotdraw8.geom.contour.PlinePathBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Builds a contour path around a path.
 *
 * @param <T> the product type
 */
public class ContourPathBuilder<T> extends AbstractPathBuilder<T> {
    private final double offset;

    private final PathBuilder<T> consumer;
    private final PlinePathBuilder papb = new PlinePathBuilder();
    private final double eps2;

    public ContourPathBuilder(PathBuilder<T> consumer, double offset) {
        this(consumer, offset, 0);
    }

    public ContourPathBuilder(PathBuilder<T> consumer, double offset, double epsilon) {
        this.offset = offset;
        this.consumer = consumer;
        this.eps2 = epsilon * epsilon;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        papb.closePath();
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        papb.curveTo(x1, y1, x2, y2, x, y);
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        if (eps2 == 0 || (lastX - x) * (lastX - x) + (lastY - y) * (lastY - y) >= eps2) {
            papb.lineTo(x, y);
        }
    }

    @Override
    protected void doMoveTo(double x, double y) {
        papb.moveTo(x, y);
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        papb.quadTo(x1, y1, x, y);
    }

    @Override
    public @Nullable T build() {
        ContourBuilder contourBuilder = new ContourBuilder();
        for (PlinePath path : papb.build()) {
            for (PlinePath contourPath : contourBuilder.parallelOffset(path, -offset)) {
                AwtShapes.buildPathIterator(consumer, contourPath.getPathIterator(null));
            }
        }
        return consumer.build();
    }
}
