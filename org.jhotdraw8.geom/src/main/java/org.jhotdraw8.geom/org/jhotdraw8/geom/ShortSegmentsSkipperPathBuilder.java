/*
 * @(#)ShortSegmentsSkipperPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Skips lineTo, quadTo and curveTo segments
 * if the distance to the previous segment is less than epsilon.
 *
 * @param <T> the product type
 */
public class ShortSegmentsSkipperPathBuilder<T> extends AbstractPathBuilder<T> {

    private final @NonNull PathBuilder<T> consumer;


    /**
     * Squared Epsilon for determining whether an element is empty.
     */
    private final double squaredEpsilon;

    public ShortSegmentsSkipperPathBuilder(@NonNull PathBuilder<T> consumer, double epsilon) {
        this.consumer = consumer;
        this.squaredEpsilon = epsilon * epsilon;
    }


    @Override
    protected void doArcTo(double lastX, double lastY, double radiusX, double radiusY, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag) {
        if (shouldntSkip(x, y)) {
            consumer.arcTo(radiusX, radiusY, xAxisRotation, x, y, largeArcFlag, sweepFlag);
        }
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        consumer.closePath();
    }

    @Override
    protected void doPathDone() {
        consumer.pathDone();

    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        if (shouldntSkip(x, y)) {
            consumer.curveTo(x1, y1, x2, y2, x, y);
        }
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        if (shouldntSkip(x, y)) {
            consumer.lineTo(x, y);
        }
    }

    @Override
    protected void doMoveTo(double x, double y) {
        consumer.moveTo(x, y);
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        if (shouldntSkip(x, y)) {
            consumer.quadTo(x1, y1, x, y);
        }
    }


    private boolean shouldntSkip(double x, double y) {
        return Points.squaredDistance(getLastX(), getLastY(), x, y) >= squaredEpsilon;
    }

    @Override
    public @Nullable T build() {
        return consumer.build();
    }
}
