package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.primitive.ByteArrayList;
import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.collection.primitive.IntArrayList;

import java.awt.geom.PathIterator;
import java.util.DoubleSummaryStatistics;

public class PathMetricsBuilder extends AbstractPathBuilder<PathMetrics> {
    private ByteArrayList commands = new ByteArrayList();
    private IntArrayList offsets = new IntArrayList();
    private DoubleArrayList coords = new DoubleArrayList();
    private DoubleArrayList lengths = new DoubleArrayList();
    private DoubleSummaryStatistics acc = new DoubleSummaryStatistics();
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = (int) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = (int) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = (int) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (int) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;
    double lastMoveToX, lastMoveToY;

    @Override
    protected void doClosePath() {
        if (!commands.isEmpty()
                && commands.getLastAsByte() != SEG_MOVETO
                && commands.getLastAsByte() != SEG_CLOSE) {
            // Only add SEG_CLOSE if it actually closes a path

            // Add a missing SEG_LINETO if necessary 
            double length = Points.distance(lastMoveToX, lastMoveToY, getLastX(), getLastY());
            if (length > 0) {
                commands.addAsByte(SEG_LINETO);
                offsets.addAsInt(offsets.size() + 2);
                coords.addAsDouble(lastMoveToX);
                coords.addAsDouble(lastMoveToY);
                acc.accept(length);
                lengths.addAsDouble(acc.getSum());
            }

            commands.addAsByte(SEG_CLOSE);
            offsets.addAsInt(offsets.size());
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    protected void doPathDone() {

    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        double length = CubicCurveArcLengthSimpson.arcLength(coords.getArray(), coords.size() - 8, 0.1);
        if (length > 0) {
            commands.addAsByte(SEG_CUBICTO);
            offsets.addAsInt(offsets.size() + 6);
            coords.addAsDouble(x1);
            coords.addAsDouble(y1);
            coords.addAsDouble(x2);
            coords.addAsDouble(y2);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
            acc.accept(length);
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    protected void doLineTo(double x, double y) {
        double length = Points.distance(getLastX(), getLastY(), x, y);
        if (length > 0) {
            commands.addAsByte(SEG_LINETO);
            offsets.addAsInt(offsets.size() + 2);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
            acc.accept(length);
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    protected void doMoveTo(double x, double y) {
        lastMoveToX = x;
        lastMoveToY = y;

        if (!commands.isEmpty() && commands.getLastAsByte() == SEG_MOVETO) {
            // Coalesce multiple consecutive SEG_MOVETO into one
            coords.setAsDouble(coords.size() - 2, x);
            coords.setAsDouble(coords.size() - 1, y);
        } else {
            commands.addAsByte(SEG_MOVETO);
            offsets.addAsInt(offsets.size() + 2);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x, double y) {
        double length = QuadCurves.arcLength(coords.getArray(), coords.size() - 6);
        if (length > 0) {
            commands.addAsByte(SEG_QUADTO);
            offsets.addAsInt(offsets.size() + 4);
            coords.addAsDouble(x1);
            coords.addAsDouble(y1);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
            acc.accept(length);
            lengths.addAsDouble(acc.getSum());
        }
    }

    @Override
    public @Nullable PathMetrics build() {
        return new PathMetrics(commands.toByteArray(),
                offsets.toIntArray(),
                coords.toDoubleArray(),
                lengths.toDoubleArray());
    }
}
