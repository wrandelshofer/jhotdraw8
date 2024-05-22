package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.intersect.IntersectLinePoint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.List;

public class AwtShapes {
    public static @NonNull <T extends PathBuilder<?>> T buildPathIterator(@NonNull T builder, @NonNull PathIterator iter) {
        double[] coords = new double[6];
        for (; !iter.isDone(); iter.next()) {
            switch (iter.currentSegment(coords)) {
                case PathIterator.SEG_CLOSE:
                    builder.closePath();
                    break;
                case PathIterator.SEG_CUBICTO:
                    builder.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_LINETO:
                    builder.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    builder.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_MOVETO:
                    builder.moveTo(coords[0], coords[1]);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported segment type:" + iter.currentSegment(coords));
            }
        }
        return builder;
    }

    /**
     * Returns true, if the outline of this shape contains the specified point.
     *
     * @param shape     The shape.
     * @param p         The point to be tested.
     * @param tolerance The tolerance for the test.
     * @return true if contained within tolerance
     */
    public static boolean outlineContains(@NonNull Shape shape, Point2D.@NonNull Double p, double tolerance) {
        AwtPathBuilder b = new AwtPathBuilder();

        double[] coords = new double[6];
        double prevX = 0, prevY = 0;
        double moveX = 0, moveY = 0;
        for (PathIterator i = new FlatteningPathIterator(shape.getPathIterator(new AffineTransform(), tolerance), Math.abs(tolerance + 0.1e-4)); !i.isDone(); i.next()) {
            switch (i.currentSegment(coords)) {
                case PathIterator.SEG_CLOSE -> {
                    if (IntersectLinePoint.lineContainsPoint(
                            prevX, prevY, moveX, moveY,
                            p.x, p.y, tolerance)) {
                        return true;
                    }
                }
                case PathIterator.SEG_LINETO -> {
                    if (IntersectLinePoint.lineContainsPoint(
                            prevX, prevY, coords[0], coords[1],
                            p.x, p.y, tolerance)) {
                        return true;
                    }
                }
                case PathIterator.SEG_MOVETO -> {
                    moveX = coords[0];
                    moveY = coords[1];
                }
                default -> {
                }
            }
            prevX = coords[0];
            prevY = coords[1];
        }
        return false;
    }

    public static @NonNull PathIterator emptyPathIterator() {
        return new PathIterator() {
            @Override
            public int getWindingRule() {
                return PathIterator.WIND_EVEN_ODD;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public void next() {
                // empty
            }

            @Override
            public int currentSegment(float[] coords) {
                return PathIterator.SEG_CLOSE;
            }

            @Override
            public int currentSegment(double[] coords) {
                return PathIterator.SEG_CLOSE;
            }
        };
    }

    public static @NonNull PathIterator pointCoordsToPathIterator(@NonNull List<Double> coordsList, boolean closed, int windingRule, @Nullable AffineTransform tx) {
        return new PathIterator() {
            private final int size = coordsList.size();
            int index = 0;
            final float[] srcf = tx == null ? null : new float[2];
            final double[] srcd = tx == null ? null : new double[2];

            @Override
            public int currentSegment(float[] coords) {
                if (index < size) {
                    double x = coordsList.get(index);
                    double y = coordsList.get(index + 1);
                    if (tx == null) {
                        coords[0] = (float) x;
                        coords[1] = (float) y;
                    } else {
                        srcf[0] = (float) x;
                        srcf[1] = (float) y;
                        tx.transform(srcf, 0, coords, 0, 1);
                    }
                    return index == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
                } else if (index == size && closed) {
                    return PathIterator.SEG_CLOSE;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                if (index < size) {
                    double x = coordsList.get(index);
                    double y = coordsList.get(index + 1);
                    if (tx == null) {
                        coords[0] = x;
                        coords[1] = y;
                    } else {
                        srcd[0] = x;
                        srcd[1] = y;
                        tx.transform(srcd, 0, coords, 0, 1);
                    }
                    return index == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
                } else if (index == size && closed) {
                    return PathIterator.SEG_CLOSE;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public int getWindingRule() {
                return windingRule;
            }

            @Override
            public boolean isDone() {
                return index >= size + (closed ? 2 : 0);
            }

            @Override
            public void next() {
                if (index < size + (closed ? 2 : 0)) {
                    index += 2;
                }
            }

        };
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private AwtShapes() {
    }
}
