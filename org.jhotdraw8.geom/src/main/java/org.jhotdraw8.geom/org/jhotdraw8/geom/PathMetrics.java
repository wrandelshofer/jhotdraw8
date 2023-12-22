/*
 * @(#)PathMetrics.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.intersect.IntersectPathIteratorPoint;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.intersect.IntersectionStatus;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

/**
 * The {@code PathMetrics} class allows access to the
 * metrics needed to compute points along a path and to
 * create sub-paths of a path.
 * <p>
 * Requirements for the path:
 * <ul>
 *     <li>The path must start with a {@link PathIterator#SEG_MOVETO}</li>
 *     <li>A {@link PathIterator#SEG_MOVETO} must be followed by a
 *     {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
 *     {@link PathIterator#SEG_CUBICTO}.</li>
 *     <li>A {@link PathIterator#SEG_CLOSE} must be the last element,
 *     or it must be followed by a {@link PathIterator#SEG_MOVETO}.</li>
 *     <li>The length of a {@link PathIterator#SEG_LINETO}, {@link PathIterator#SEG_QUADTO},
 *     {@link PathIterator#SEG_CUBICTO} must be greater than 0.</li>
 *     <li>The {@code x, y} coordinates of the {@link PathIterator#SEG_MOVETO}
 *     at the beginning of a closed path must equal to the {@code x, y} coordinates
 *     of the last segment of the closed path.</li>
 * </ul>
 */
public class PathMetrics implements Shape {
    private final byte @NonNull [] commands;
    private final int @NonNull [] offsets;
    private final double @NonNull [] coords;
    private final double @NonNull [] accumulatedLengths;
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = (byte) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = (byte) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = (byte) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;
    private final double minx, miny, maxx, maxy;
    private final int windingRule;

    PathMetrics(byte @NonNull [] commands, int @NonNull [] offsets, double @NonNull [] coords, double @NonNull [] accumulatedLengths, int windingRule) {
        this.commands = commands;
        this.offsets = offsets;
        this.coords = coords;
        this.accumulatedLengths = accumulatedLengths;
        this.windingRule = windingRule;

        double mminx = Double.POSITIVE_INFINITY, mminy = Double.POSITIVE_INFINITY, mmaxx = Double.NEGATIVE_INFINITY, mmaxy = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < coords.length; i += 2) {
            mminx = Math.min(mminx, coords[i]);
            mmaxx = Math.max(mmaxx, coords[i]);
            mminy = Math.min(mminy, coords[i + 1]);
            mmaxy = Math.min(mmaxy, coords[i + 1]);
        }
        this.minx = mminx;
        this.maxx = mmaxx;
        this.miny = mminy;
        this.maxy = mmaxy;
    }

    /**
     * Evaluates the path at time t.
     *
     * @param t the time in the range [0,1]
     * @return point and tangent at t
     */
    public @NonNull PointAndDerivative eval(double t) {
        return evalAtArcLength(t * getArcLength());
    }

    /**
     * Evaluates the path at the specified arc length
     *
     * @param s the arc length
     * @return point and tangent at s
     */
    public @NonNull PointAndDerivative evalAtArcLength(double s) {
        int search = Arrays.binarySearch(accumulatedLengths, s);
        int i = search < 0 ? Math.min(commands.length - 1, ~search) : search;

        if (commands[i] == SEG_CLOSE) {
            // Use the last segment of the shape that is being closed
            while (i > 0 && commands[i] == SEG_CLOSE) {
                i--;
            }
        } else if (commands[i] == SEG_MOVETO) {
            // Use the first segment of the shape that is being opened
            while (i < commands.length - 1 && commands[i] == SEG_MOVETO) {
                i++;
            }
        }

        int offset = offsets[i] - 2;// at offset - 2 we have the x,y coordinates of the previous command
        double start = (i == 0 ? 0 : accumulatedLengths[i - 1]);
        final double segmentS = s - start;// the s value inside the segment
        // the
        return switch (commands[i]) {
            case SEG_CLOSE -> new PointAndDerivative(0, 0, 1, 0);
            case SEG_MOVETO -> new PointAndDerivative(coords[offset + 2], coords[offset + 3], 1, 0);
            case SEG_LINETO -> Lines.eval(coords, offset, Lines.invArcLength(coords, offset, segmentS));
            case SEG_QUADTO -> QuadCurves.eval(coords, offset, QuadCurves.invArcLength(coords, offset, segmentS));
            case SEG_CUBICTO -> CubicCurves.eval(coords, offset, CubicCurves.invArcLength(coords, offset, segmentS));
            default -> throw new IllegalStateException("unexpected command=" + commands[i] + " at index=" + i);
        };
    }

    /**
     * Gets the length of the path.
     *
     * @return the length of the path in [0,Double.MAX_VALUE].
     */
    public double getArcLength() {
        return accumulatedLengths[accumulatedLengths.length - 1];
    }

    /**
     * Builds a sub-path from t0 to t1.
     * <p>
     * This method does not call {@link PathBuilder#build()}.
     *
     * @param t0 the start time in [0,1].
     * @param t1 the end time in [0,1].
     * @param b  the builder
     * @return the same builder that was passed as an argument
     */
    public <T> PathBuilder<T> buildSubPath(double t0, double t1, @NonNull PathBuilder<T> b, boolean skipFirstMoveTo) {
        double totalArcLength = getArcLength();
        double s0 = t0 * totalArcLength;
        double s1 = t1 * totalArcLength;
        return buildSubPathAtArcLength(s0, s1, b, skipFirstMoveTo);
    }

    /**
     * Builds a sub-path from arc-lengths s0 to s1.
     * <p>
     * This method does not call {@link PathBuilder#build()}.
     *
     * @param s0 the arc length at which the sub-path starts, in [0,getArcLength()].
     * @param s1 the arc length at which the sub-path ends, in [0,getArcLength()].
     * @param b  the builder
     * @return the same builder that was passed as an argument
     */
    public <T> @NonNull PathBuilder<T> buildSubPathAtArcLength(double s0, double s1, @NonNull PathBuilder<T> b, boolean skipFirstMoveTo) {
        double totalArcLength = getArcLength();
        if (s0 > totalArcLength || s1 < s0) {
            return b;
        }
        if (s0 < 0) {
            s0 = 0;
        }
        if (s1 > totalArcLength) {
            s1 = totalArcLength;
        }

        // Find the segment on which the sub-path starts
        int search0 = s0 == 0 ? 0 : Arrays.binarySearch(accumulatedLengths, s0);
        boolean startsAtSegment = search0 >= 0;
        int i0 = search0 < 0 ? ~search0 : search0;
        // Make sure that the start segment contains s0
        if (!startsAtSegment) {
            while (i0 < commands.length - 1 && accumulatedLengths[i0 + 1] < s0) {
                i0++;
            }
        }

        // Find the segment on which the sub-path ends
        int search1 = s1 == totalArcLength ? commands.length - 1 : Arrays.binarySearch(accumulatedLengths, s1);
        boolean endsAtSegment = search1 >= 0;
        int i1 = search1 < 0 ? ~search1 : search1;
        // Make sure that the end segment contains s1
        if (!endsAtSegment) {
            while (i0 < commands.length - 1 && accumulatedLengths[i0 + 1] < s1) {
                i0++;
            }
        }

        double[] splitCoords = new double[8];

        if (!startsAtSegment) {
            int offset = offsets[i0];
            double s = s0 - accumulatedLengths[i0 - 1];
            double arcLength = accumulatedLengths[i0] - accumulatedLengths[i0 - 1];
            switch (commands[i0]) {
                case SEG_CLOSE, SEG_MOVETO -> {
                }
                case SEG_LINETO -> {
                    Lines.split(coords, offset - 2,
                            s / arcLength, null, 0, splitCoords, 0);
                    if (!skipFirstMoveTo) {
                        b.moveTo(splitCoords[0], splitCoords[1]);
                    }
                    b.lineTo(splitCoords[2], splitCoords[3]);
                }
                case SEG_QUADTO -> {
                    QuadCurves.split(coords, offset - 2,
                            QuadCurves.invArcLength(coords, offset - 1, s), null, 0, splitCoords, 0);
                    if (!skipFirstMoveTo) {
                        b.moveTo(splitCoords[0], splitCoords[1]);
                    }
                    b.quadTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5]);
                }
                case SEG_CUBICTO -> {
                    CubicCurves.split(coords, offset - 2,
                            CubicCurves.invArcLength(coords, offset - 1, s), null, 0, splitCoords, 0);
                    if (!skipFirstMoveTo) {
                        b.moveTo(splitCoords[0], splitCoords[1]);
                    }
                    b.curveTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5], splitCoords[6], splitCoords[7]);
                }
                default -> throw new IllegalStateException("unexpected command=" + commands[i0] + " at index=" + i0);
            }
        } else {
            if (commands[i0] != SEG_MOVETO) {
                int offset = offsets[i0];
                if (offset > 0) {
                    if (!skipFirstMoveTo) {
                        b.moveTo(coords[offset - 2], coords[offset - 1]);
                    }
                }
            }
        }

        for (int i = startsAtSegment ? i0 : i0 + 1, n = endsAtSegment ? i1 : i1 - 1; i < n; i++) {
            int offset = offsets[i];
            switch (commands[i]) {
                case SEG_CLOSE -> b.closePath();
                case SEG_MOVETO -> b.moveTo(coords[offset], coords[offset + 1]);
                case SEG_LINETO -> b.lineTo(coords[offset], coords[offset + 1]);
                case SEG_QUADTO -> b.quadTo(coords[offset], coords[offset + 1], coords[offset + 2], coords[offset + 3]);
                case SEG_CUBICTO ->
                        b.curveTo(coords[offset], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4], coords[offset + 5]);
                default -> throw new IllegalStateException("unexpected command=" + commands[i] + " at index=" + i);
            }
        }

        if (!endsAtSegment) {
            int offset = offsets[i1];
            double s = s0 - accumulatedLengths[i1 - 1];
            double arcLength = accumulatedLengths[i1] - accumulatedLengths[i1 - 1];
            switch (commands[i1]) {
                case SEG_CLOSE -> b.closePath();
                case SEG_MOVETO -> {
                }
                case SEG_LINETO -> {
                    Lines.split(coords, offset - 2,
                            (s0 - accumulatedLengths[i0 - 1]) / arcLength, splitCoords, 0, null, 0);
                    b.lineTo(splitCoords[2], splitCoords[3]);
                }
                case SEG_QUADTO -> {
                    QuadCurves.split(coords, offset - 2,
                            QuadCurves.invArcLength(coords, offset - 1, s), splitCoords, 0, null, 0);
                    b.quadTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5]);
                }
                case SEG_CUBICTO -> {
                    CubicCurves.split(coords, offset - 2,
                            CubicCurves.invArcLength(coords, offset - 1, s), splitCoords, 0, null, 0);
                    b.curveTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5], splitCoords[6], splitCoords[7]);
                }
                default -> throw new IllegalStateException("unexpected command=" + commands[i1] + " at index=" + i1);
            }
        }
        return b;
    }


    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) minx, (int) miny, (int) (maxx - minx), (int) (maxy - miny));
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(minx, miny, maxx - minx, maxy - miny);
    }

    @Override
    public boolean contains(double x, double y) {
        if (minx <= x && x <= maxx && miny <= y && y <= maxy) {
            IntersectionResult result = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, 0);
            return result.getStatus() == IntersectionStatus.NO_INTERSECTION_INSIDE || result.getStatus() == IntersectionStatus.INTERSECTION;
        }
        return false;
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return false;
    }

    public PathIterator getPathIterator(final @Nullable AffineTransform tx) {
        final AffineTransform tt = tx == null ? AffineTransform.getTranslateInstance(0, 0) : tx;
        return new PathIterator() {
            int current = 0;

            @Override
            public int getWindingRule() {
                return PathIterator.WIND_EVEN_ODD;
            }

            @Override
            public boolean isDone() {
                return current >= commands.length;
            }

            @Override
            public void next() {
                if (!isDone()) {
                    current++;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                final int offset = offsets[current];
                switch (commands[current]) {
                    case PathMetrics.SEG_MOVETO, PathMetrics.SEG_LINETO -> {
                        tt.transform(PathMetrics.this.coords, offset, coords, 0, 2);
                    }
                    case PathMetrics.SEG_QUADTO -> {
                        tt.transform(PathMetrics.this.coords, offset, coords, 0, 4);
                    }
                    case PathMetrics.SEG_CUBICTO -> {
                        tt.transform(PathMetrics.this.coords, offset, coords, 0, 6);
                    }
                    default -> {//SEG CLOSE

                    }
                }
                return commands[current];
            }

            @Override
            public int currentSegment(double[] coords) {
                final int offset = offsets[current];
                switch (commands[current]) {
                    case PathMetrics.SEG_MOVETO, PathMetrics.SEG_LINETO -> {
                        tt.transform(PathMetrics.this.coords, offset, coords, 0, 1);
                    }
                    case PathMetrics.SEG_QUADTO -> {
                        tt.transform(PathMetrics.this.coords, offset, coords, 0, 2);
                    }
                    case PathMetrics.SEG_CUBICTO -> {
                        tt.transform(PathMetrics.this.coords, offset, coords, 0, 3);
                    }
                    default -> {//SEG CLOSE

                    }
                }
                return commands[current];
            }
        };
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return null;
    }
}
