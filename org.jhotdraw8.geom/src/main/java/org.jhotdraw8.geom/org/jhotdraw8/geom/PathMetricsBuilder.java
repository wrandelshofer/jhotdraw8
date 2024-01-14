/*
 * @(#)PathMetricsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.awt.geom.PathIterator;
import java.util.DoubleSummaryStatistics;

public class PathMetricsBuilder extends AbstractPathDataBuilder<PathMetrics> {
    private final DoubleArrayList lengths = new DoubleArrayList();
    private final DoubleSummaryStatistics acc = new DoubleSummaryStatistics();
    private final double epsilon = 0.125;
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = (int) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = (int) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = (int) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (int) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;

    public PathMetricsBuilder() {
    }

    @Override
    protected void doClosePath() {
        if (!commands.isEmpty()
                && commands.getLastAsByte() != SEG_MOVETO
                && commands.getLastAsByte() != SEG_CLOSE) {
            // Only add SEG_CLOSE if it actually closes a path

            // Add a missing SEG_LINETO if necessary
            double length = Points.distance(lastMoveToX, lastMoveToY, getLastX(), getLastY());
            if (length > epsilon) {
                commands.addAsByte(SEG_LINETO);
                offsets.addAsInt(coords.size());
                coords.addAsDouble(lastMoveToX);
                coords.addAsDouble(lastMoveToY);
                acc.accept(length);
                lengths.addAsDouble(acc.getSum());
            }

            commands.addAsByte(SEG_CLOSE);
            offsets.addAsInt(coords.size());
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    protected void doPathDone() {

    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        temp[0] = getLastX();
        temp[1] = getLastY();
        temp[2] = x1;
        temp[3] = y1;
        temp[4] = x2;
        temp[5] = y2;
        temp[6] = x;
        temp[7] = y;
        double arcLength = CubicCurves.arcLength(temp, 0, epsilon);
        if (arcLength > epsilon) {
            commands.addAsByte(SEG_CUBICTO);
            offsets.addAsInt(coords.size());
            coords.addAsDouble(x1);
            coords.addAsDouble(y1);
            coords.addAsDouble(x2);
            coords.addAsDouble(y2);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
            acc.accept(arcLength);
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    protected void doLineTo(double x, double y) {
        double length = Points.distance(getLastX(), getLastY(), x, y);
        if (length > epsilon) {
            commands.addAsByte(SEG_LINETO);
            offsets.addAsInt(coords.size());
            coords.addAsDouble(x);
            coords.addAsDouble(y);
            acc.accept(length);
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    protected void doMoveTo(double x, double y) {
        commands.addAsByte(SEG_MOVETO);
        offsets.addAsInt(coords.size());
        coords.addAsDouble(x);
        coords.addAsDouble(y);
        lengths.addAsDouble(acc.getSum());
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x, double y) {
        temp[0] = getLastX();
        temp[1] = getLastY();
        temp[2] = x1;
        temp[3] = y1;
        temp[4] = x;
        temp[5] = y;
        double arcLength = QuadCurves.arcLength(temp, 0);
        if (arcLength > epsilon) {
            commands.addAsByte(SEG_QUADTO);
            offsets.addAsInt(coords.size());
            coords.addAsDouble(x1);
            coords.addAsDouble(y1);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
            acc.accept(arcLength);
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    public @Nullable SimplePathMetrics build() {
        return new SimplePathMetrics(commands.toByteArray(),
                offsets.toIntArray(),
                coords.toDoubleArray(),
                lengths.toDoubleArray(), windingRule);
    }
}
