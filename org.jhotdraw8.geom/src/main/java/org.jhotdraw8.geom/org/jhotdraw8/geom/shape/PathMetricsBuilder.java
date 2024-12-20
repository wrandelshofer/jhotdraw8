/*
 * @(#)PathMetricsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom.shape;

import org.jhotdraw8.collection.primitive.ByteArrayList;
import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.geom.AbstractPathDataBuilder;
import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.QuadCurves;

import java.awt.geom.PathIterator;
import java.util.DoubleSummaryStatistics;

public class PathMetricsBuilder extends AbstractPathDataBuilder<PathMetrics> {
    protected final DoubleArrayList lengths = new DoubleArrayList();
    private final DoubleSummaryStatistics acc = new DoubleSummaryStatistics();
    private final double epsilon;
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = PathIterator.SEG_CLOSE;

    /**
     * Creates a path metrics builder with an accuracy of 1/8 (=0.125) pixels.
     */
    public PathMetricsBuilder() {
        this(0.125);
    }

    /**
     * Creates a path metrics builder with the specified accuracy.
     *
     * @param epsilon accuracy
     */
    public PathMetricsBuilder(double epsilon) {
        this.epsilon = Math.abs(epsilon);
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        if (!commands.isEmpty()
                && commands.getLastAsByte() != SEG_MOVETO
                && commands.getLastAsByte() != SEG_CLOSE) {
            // Only add SEG_CLOSE if it actually closes a path

            // Add a missing SEG_LINETO if necessary
            double length = Points.distance(lastMoveToX, lastMoveToY, lastX, getLastY());
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
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        temp[0] = lastX;
        temp[1] = lastY;
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
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        double length = Points.distance(lastX, lastY, x, y);
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
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        temp[0] = lastX;
        temp[1] = lastY;
        temp[2] = x1;
        temp[3] = y1;
        temp[4] = x;
        temp[5] = y;
        double arcLength = QuadCurves.arcLength(temp, 0, epsilon);
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
    public SimplePathMetrics build() {
        return new SimplePathMetrics(commands.toByteArray(),
                offsets.toIntArray(),
                coords.toDoubleArray(),
                lengths.toDoubleArray(), windingRule, epsilon);
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    protected ByteArrayList getCommands() {
        return commands;
    }

    protected IntArrayList getOffsets() {
        return offsets;
    }

    protected DoubleArrayList getCoords() {
        return coords;
    }
}
