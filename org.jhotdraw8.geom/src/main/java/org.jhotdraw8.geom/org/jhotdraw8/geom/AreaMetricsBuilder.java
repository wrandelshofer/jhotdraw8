/*
 * @(#)AreaMetricsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;

import java.util.DoubleSummaryStatistics;

/**
 * Computes the area of a path. This only makes sense for closed paths.
 * <p>
 * FIXME implement doCurveTo and doQuadTo
 * <p>
 * XXX Only works with consolidated inputs, for example from a {@link java.awt.geom.Area} object
 */
public class AreaMetricsBuilder extends AbstractPathBuilder<Double> {
    private final DoubleSummaryStatistics areaTimesTwo = new DoubleSummaryStatistics();
    double lastMoveToX, lastMoveToY;

    @Override
    protected void doClosePath() {
        addToArea(lastMoveToX, lastMoveToY, getLastX(), getLastY());
    }

    private void addToArea(double x0, double y0, double x1, double y1) {
        areaTimesTwo.accept((x0 + x1) * (y0 - y1));
    }

    @Override
    protected void doPathDone() {

    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        addToArea(getLastX(), getLastY(), x, y);
    }

    @Override
    protected void doLineTo(double x, double y) {
        addToArea(getLastX(), getLastY(), x, y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        lastMoveToX = x;
        lastMoveToY = y;
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x, double y) {
        addToArea(lastMoveToX, lastMoveToY, getLastX(), getLastY());
    }

    @Override
    public @NonNull Double build() {
        return Math.abs(areaTimesTwo.getSum()) * 0.5;
    }
}
