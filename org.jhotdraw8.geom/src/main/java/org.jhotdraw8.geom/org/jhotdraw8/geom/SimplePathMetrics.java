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
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.isNaN;

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
public class SimplePathMetrics implements Shape, PathMetrics {
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
    private final double epsilon = 0.125;

    SimplePathMetrics(byte @NonNull [] commands, int @NonNull [] offsets, double @NonNull [] coords, double @NonNull [] accumulatedLengths, int windingRule) {
        this.commands = commands;
        this.offsets = offsets;
        this.coords = coords;
        this.accumulatedLengths = accumulatedLengths;
        this.windingRule = windingRule;

        double mminx = POSITIVE_INFINITY, mminy = POSITIVE_INFINITY, mmaxx = NEGATIVE_INFINITY, mmaxy = NEGATIVE_INFINITY;
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

        return switch (commands[i]) {
            case SEG_CLOSE -> new PointAndDerivative(0, 0, 1, 0);
            case SEG_MOVETO -> new PointAndDerivative(coords[offset + 2], coords[offset + 3], 1, 0);
            case SEG_LINETO -> Lines.eval(coords, offset, Lines.invArcLength(coords, offset, segmentS));
            case SEG_QUADTO ->
                    QuadCurves.eval(coords, offset, QuadCurves.invArcLength(coords, offset, segmentS, epsilon));
            case SEG_CUBICTO ->
                    CubicCurves.eval(coords, offset, CubicCurves.invArcLength(coords, offset, segmentS, epsilon));
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
        return staticBuildSubPathAtArcLength(accumulatedLengths, commands, coords, epsilon, offsets, s0, s1, b, skipFirstMoveTo);
    }

    private static <T> PathBuilder<T> staticBuildSubPathAtArcLength(double @NonNull [] accumulatedLengths, byte @NonNull [] commands, double @NonNull [] coords, double epsilon, int @NonNull [] offsets, double s0, double s1, @NonNull PathBuilder<T> b, boolean skipFirstMoveTo) {
        double totalArcLength = accumulatedLengths[accumulatedLengths.length - 1];
        if (commands.length == 0 || s0 > totalArcLength || s1 < s0) {
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
        // Make sure that the start segment contains s0+epsilon
        while (i0 < commands.length - 1 && accumulatedLengths[i0] <= s0) {
            i0++;
        }

        // Find the segment on which the sub-path ends
        int search1 = s1 == totalArcLength ? commands.length - 1 : Arrays.binarySearch(accumulatedLengths, s1);
        boolean endsAtSegment = search1 >= 0;
        int i1 = search1 < 0 ? ~search1 : search1;
        if (!endsAtSegment) {
            // Make sure that the end segment contains s1
            while (i1 < commands.length - 1 && accumulatedLengths[i1 + 1] < s1) {
                i1++;
            }
        } else {
            i1++;
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
                    if (i1 > i0) {
                        b.lineTo(splitCoords[2], splitCoords[3]);
                    }
                }
                case SEG_QUADTO -> {
                    QuadCurves.split(coords, offset - 2,
                            QuadCurves.invArcLength(coords, offset - 1, s, epsilon), null, 0, splitCoords, 0);
                    if (!skipFirstMoveTo) {
                        b.moveTo(splitCoords[0], splitCoords[1]);
                    }
                    if (i1 > i0) {
                        b.quadTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5]);
                    }
                }
                case SEG_CUBICTO -> {
                    CubicCurves.split(coords, offset - 2,
                            CubicCurves.invArcLength(coords, offset - 1, s, epsilon), null, 0, splitCoords, 0);
                    if (!skipFirstMoveTo) {
                        b.moveTo(splitCoords[0], splitCoords[1]);
                    }
                    if (i1 > i0) {
                        b.curveTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5], splitCoords[6], splitCoords[7]);
                    }
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

        for (int i = startsAtSegment ? i0 : i0 + 1; i < i1; i++) {
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
            double s = s1 - accumulatedLengths[i1 - 1];
            double arcLength = accumulatedLengths[i1] - accumulatedLengths[i1 - 1];
            switch (commands[i1]) {
                case SEG_CLOSE -> b.closePath();
                case SEG_MOVETO -> {
                }
                case SEG_LINETO -> {
                    Lines.split(coords, offset - 2,
                            s / arcLength, splitCoords, 0, null, 0);
                    b.lineTo(splitCoords[2], splitCoords[3]);
                }
                case SEG_QUADTO -> {
                    QuadCurves.split(coords, offset - 2,
                            QuadCurves.invArcLength(coords, offset - 1, s, epsilon), splitCoords, 0, null, 0);
                    b.quadTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5]);
                }
                case SEG_CUBICTO -> {
                    CubicCurves.split(coords, offset - 2,
                            CubicCurves.invArcLength(coords, offset - 1, s, epsilon), splitCoords, 0, null, 0);
                    b.curveTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5], splitCoords[6], splitCoords[7]);
                }
                default -> throw new IllegalStateException("unexpected command=" + commands[i1] + " at index=" + i1);
            }
        }
        return b;
    }


    public @NonNull <T> PathBuilder<T> buildReverseSubPathAtArcLength(double s0, double s1, @NonNull PathBuilder<T> b, boolean skipFirstMoveTo) {
        double totalArcLength = accumulatedLengths[accumulatedLengths.length - 1];
        if (commands.length == 0 || s0 > totalArcLength || s1 < s0) {
            return b;
        }
        if (s0 < 0) {
            s0 = 0;
        }
        if (s1 > totalArcLength) {
            s1 = totalArcLength;
        }

        byte @NonNull [] reverseCommands = new byte[commands.length];
        int @NonNull [] reverseOffsets = new int[offsets.length];
        double @NonNull [] reverseCoords = new double[coords.length];
        double @NonNull [] reverseAccumulatedLengths = new double[accumulatedLengths.length];

        // reverse coordinates
        for (int i = 0, n = coords.length; i < n; i += 2) {
            reverseCoords[n - i - 2] = coords[i];
            reverseCoords[n - i - 1] = coords[i + 1];
        }
        // reverse commands, offsets and accumulated lengths
        boolean pendingClose = false;
        int sr = 0;
        reverseCommands[sr++] = SEG_MOVETO;
        int offset = 0;
        for (int s = commands.length - 1; s > 0; --s) {
            switch (commands[s]) {
                case SEG_MOVETO:
                    if (pendingClose) {
                        pendingClose = false;
                        reverseOffsets[sr] = offset;
                        reverseCommands[sr++] = SEG_CLOSE;
                    }
                    reverseOffsets[sr] = offset;
                    reverseCommands[sr] = SEG_MOVETO;
                    sr++;
                    offset += 2;
                    break;

                case SEG_CLOSE:
                    pendingClose = true;
                    break;

                default:
                    reverseOffsets[sr] = offset;
                    reverseCommands[sr++] = commands[s];
                    offset += switch (commands[s]) {
                        default -> 2;
                        case SEG_QUADTO -> 4;
                        case SEG_CUBICTO -> 6;
                    };
                    break;
            }
        }
        if (pendingClose) {
            reverseCommands[sr] = SEG_CLOSE;
            reverseOffsets[sr] = offset;
        }

        return staticBuildSubPathAtArcLength(reverseAccumulatedLengths, reverseCommands, reverseCoords, epsilon, reverseOffsets, s0, s1, b, skipFirstMoveTo);
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

    /**
     * See {@link #contains(double, double)}.
     *
     * @param p the specified {@code Point2D}
     * @return true if this shape contains p
     */
    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    /**
     * This implementation checks if the bounding box
     * of this shape intersects with the specified rectangle.
     *
     * @param x the X coordinate of the upper-left corner
     *          of the specified rectangular area
     * @param y the Y coordinate of the upper-left corner
     *          of the specified rectangular area
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return true if the interior of the bounding box of this shape intersects
     * with the interior of the specified rectangle.
     */
    @Override
    public boolean intersects(double x, double y, double w, double h) {
        if (isNaN(x + w) || isNaN(y + h) || w <= 0 || h <= 0
                || !(maxx >= x) || !(x + w >= minx) || !(maxy >= y) || !(y + h >= miny)) {
            return false;
        }
        // FIXME implement me
        return true;
    }

    /**
     * See {@link #intersects(double, double, double, double)}.
     *
     * @param r the specified {@code Rectangle2D}
     * @return true if this shape intersects with r
     */
    @Override
    public boolean intersects(@NonNull Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * This implementation checks if the bounding box
     * of this shape contains the specified rectangle.
     *
     * @param x the X coordinate of the upper-left corner
     *          of the specified rectangular area
     * @param y the Y coordinate of the upper-left corner
     *          of the specified rectangular area
     * @param w the width of the specified rectangular area
     * @param h the height of the specified rectangular area
     * @return true if the interior of the bounding box of this shape contains
     * the interior of the specified rectangle.
     */
    @Override
    public boolean contains(double x, double y, double w, double h) {
        if (isNaN(x + w) || isNaN(y + h) || w <= 0 || h <= 0
                || !(minx <= x) || !(x + w <= maxx) || !(miny <= y) || !(y + h <= maxy)) {
            return false;
        }
        // FIXME implement me
        return true;
    }

    /**
     * See {@link #contains(double, double, double, double)}.
     *
     * @param r the specified {@code Rectangle2D}
     * @return true if this shape contains r
     */
    @Override
    public boolean contains(@NonNull Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }


    @Override
    public PathIterator getSubPathIteratorAtArcLength(double s0, double s1, @Nullable AffineTransform tx) {
        double totalArcLength = getArcLength();
        if (s0 > totalArcLength || s1 < s0) {
            return new EmptyPathIterator();
        }

        boolean startsAtFirstSegment = s0 <= 0;
        boolean endsAtLastSegment = s1 >= totalArcLength;

        if (startsAtFirstSegment && endsAtLastSegment) {
            return getPathIterator(tx);
        }

        // XXX this is quite inefficient, we should directly implement a PathIterator.
        return buildSubPathAtArcLength(s0, s1, new AwtPathBuilder()).build().getPathIterator(tx);

        /*
        // The iterator is a state machine with n-states in the range [-2,commands.length]
        // The state machine in the code below is not correct.
        int startState;//inclusive
        int firstMoveToStartOfSegmentState;//a value less than startState indicates that we do not need a first move to
        int firstMoveToSubSegmentState;//a value less than startState indicates that we do not need a first move to
        int firstSubSegmentState;//a value less than startState indicates that we do not need a sub-segment
        int lastSubSegmentState;//a value less than startState indicates that we do not need a sub-segment
        int endState;// exclusive
        int firstSegmentIndex;
        int lastSegmentIndex;
        int i0;
        boolean startsAtSegment;
        if (startsAtFirstSegment) {
            i0=0;
            startsAtSegment=true;
            startState = 0;
            firstMoveToStartOfSegmentState = startState - 1;
            firstMoveToSubSegmentState = startState - 1;
            firstSubSegmentState = startState - 1;
            firstSegmentIndex = 0;
        } else {
            // Find the segment on which the sub-path starts
            int search0 =  Arrays.binarySearch(accumulatedLengths, s0);
            startsAtSegment = search0 >= 0;
            i0 = search0 < 0 ? ~search0 : search0;
            // Make sure that the start segment contains s0+epsilon
            while (i0 < commands.length - 1 && accumulatedLengths[i0 ] <= s0) {
                i0++;
            }
            if (startsAtSegment) {
                startState = i0 - 1;
                firstMoveToStartOfSegmentState = startState;
                firstMoveToSubSegmentState = startState - 1;
                firstSubSegmentState = startState - 1;
            } else {
                startState = i0 - 1;
                firstMoveToStartOfSegmentState = startState - 1;
                firstMoveToSubSegmentState = startState;
                firstSubSegmentState = startState + 1;
            }
            firstSegmentIndex = i0;
        }

        if (endsAtLastSegment) {
            endState = commands.length;
            lastSubSegmentState = startState - 1;
            lastSegmentIndex = commands.length - 1;
        } else {
            // Find the segment on which the sub-path ends
            int search1 = s1 == totalArcLength ? commands.length - 1 : Arrays.binarySearch(accumulatedLengths, s1);
            boolean endsAtSegment = search1 >= 0;
            int i1 = search1 < 0 ? ~search1 : search1;
            if (endsAtSegment) {
                endState = i1+1;
                lastSubSegmentState = startState - 1;
            } else {
                // Make sure that the end segment contains s1
                while (i1 < commands.length - 1 && accumulatedLengths[i1 + 1] < s1) {
                    i1++;
                }
                endState = (!startsAtSegment&&i0==i1-1)?i1:i1 + 1;
                lastSubSegmentState = i1;
            }
            lastSegmentIndex = i1;
        }
        final AffineTransform tt = tx == null ? AffineTransform.getTranslateInstance(0, 0) : tx;

        return new PathIterator() {
            private final double[] splitCoords = new double[8];
            enum State {

            }
            private int current = startState;

            @Override
            public int getWindingRule() {
                return windingRule;
            }

            @Override
            public boolean isDone() {
                return current >= endState;
            }

            @Override
            public void next() {
                if (!isDone()) {
                    current++;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                int command = currentSegment(splitCoords);
                for (int i = 0; i < 6; i++) {
                    coords[i] = (float) splitCoords[i];
                }
                return command;
            }

            @Override
            public int currentSegment(double[] coords) {
                if (current == firstMoveToStartOfSegmentState) return firstMoveToStartOfSegment(coords);
                if (current == firstMoveToSubSegmentState) return firstMoveToSubSegment(coords);
                if (current == firstSubSegmentState) return firstSubSegment(coords);
                if (current == lastSubSegmentState) return lastSubSegment(coords);
                return middleSegment(coords);
            }

            private int middleSegment(double[] coords) {
                final int offset = offsets[current];
                switch (commands[current]) {
                    case SimplePathMetrics.SEG_MOVETO, SimplePathMetrics.SEG_LINETO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 1);
                    }
                    case SimplePathMetrics.SEG_QUADTO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 2);
                    }
                    case SimplePathMetrics.SEG_CUBICTO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 3);
                    }
                    default -> {//SEG CLOSE

                    }
                }
                return commands[current];
            }

            private int lastSubSegment(double[] coords) {
                int offset = offsets[lastSegmentIndex];
                double s = s1 - accumulatedLengths[lastSegmentIndex - 1];
                double arcLength = accumulatedLengths[lastSegmentIndex] - accumulatedLengths[lastSegmentIndex - 1];
                switch (commands[lastSegmentIndex]) {
                    case SEG_CLOSE, SEG_MOVETO -> {
                    }
                    case SEG_LINETO -> {
                        Lines.split(SimplePathMetrics.this.coords, offset - 2,
                                s / arcLength, splitCoords, 0, null, 0);
                        tt.transform(splitCoords, 2, coords, 0, 1);
                    }
                    case SEG_QUADTO -> {
                        QuadCurves.split(SimplePathMetrics.this.coords, offset - 2,
                                QuadCurves.invArcLength(coords, offset - 1, s, epsilon), splitCoords, 0, null, 0);
                        tt.transform(splitCoords, 2, coords, 0, 2);
                    }
                    case SEG_CUBICTO -> {
                        CubicCurves.split(SimplePathMetrics.this.coords, offset - 2,
                                CubicCurves.invArcLength(coords, offset - 1, s, epsilon), splitCoords, 0, null, 0);
                        tt.transform(splitCoords, 2, coords, 0, 3);
                    }
                    default ->
                            throw new IllegalStateException("unexpected command=" + commands[lastSegmentIndex] + " at index=" + lastSegmentIndex);
                }
                return commands[lastSegmentIndex];
            }

            private int firstMoveToStartOfSegment(double[] coords) {
                int offset = offsets[firstSegmentIndex];
                tt.transform(SimplePathMetrics.this.coords, offset - 2, coords, 0, 1);
                return SEG_MOVETO;
            }

            private int firstMoveToSubSegment(double[] coords) {
                doFirstSubSegment(coords);
                return SEG_MOVETO;
            }

            private int firstSubSegment(double[] coords) {
                doFirstSubSegment(coords);
                return commands[firstSegmentIndex];
            }

            private void doFirstSubSegment(double[] coords) {
                int offset = offsets[firstSegmentIndex];
                double s = s0 - accumulatedLengths[firstSegmentIndex - 1];
                double arcLength = accumulatedLengths[firstSegmentIndex] - accumulatedLengths[firstSegmentIndex - 1];
                switch (commands[firstSegmentIndex]) {
                    case SEG_CLOSE, SEG_MOVETO -> {
                    }
                    case SEG_LINETO -> {
                        Lines.split(SimplePathMetrics.this.coords, offset - 2,
                                s / arcLength, null, 0, splitCoords, 0);
                        tt.transform(splitCoords, 0, coords, 0, 1);
                    }
                    case SEG_QUADTO -> {
                        QuadCurves.split(SimplePathMetrics.this.coords, offset - 2,
                                QuadCurves.invArcLength(coords, offset - 1, s, epsilon), null, 0, splitCoords, 0);
                        tt.transform(splitCoords, 0, coords, 0, 2);
                    }
                    case SEG_CUBICTO -> {
                        CubicCurves.split(SimplePathMetrics.this.coords, offset - 2,
                                CubicCurves.invArcLength(coords, offset - 1, s, epsilon), null, 0, splitCoords, 0);
                        tt.transform(splitCoords, 0, coords, 0, 3);
                    }
                    default ->
                            throw new IllegalStateException("unexpected command=" + commands[firstSegmentIndex] + " at index=" + firstSegmentIndex);
                }
            }
        };

         */
    }

    @Override
    public PathMetrics reverse() {
        throw new UnsupportedOperationException();
    }


    public PathIterator getReverseSubPathIteratorAtArcLength(double s0, double s1, @Nullable AffineTransform tx) {
        double totalArcLength = getArcLength();
        if (s0 > totalArcLength || s1 < s0) {
            return new EmptyPathIterator();
        }

        boolean startsAtFirstSegment = s0 <= 0;
        boolean endsAtLastSegment = s1 >= totalArcLength;

        if (startsAtFirstSegment && endsAtLastSegment) {
            return getReversePathIterator(tx);
        }

        // XXX this is quite inefficient, we should directly implement a reverse sub-path iterator
        return new ReversePathIterator(getSubPathIteratorAtArcLength(totalArcLength - s1, totalArcLength - s0, tx));
    }

    /**
     * Returns a path iterator of the entire path.
     *
     * @param tx an optional transformation for the path iterator
     * @return the path iterator
     */
    @Override
    public PathIterator getPathIterator(final @Nullable AffineTransform tx) {
        final AffineTransform tt = tx == null ? AffineTransform.getTranslateInstance(0, 0) : tx;
        return new PathIterator() {
            int current = 0;

            @Override
            public int getWindingRule() {
                return windingRule;
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
                    case SEG_MOVETO, SEG_LINETO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 1);
                    }
                    case SEG_QUADTO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 2);
                    }
                    case SEG_CUBICTO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 3);
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
                    case SEG_MOVETO, SEG_LINETO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 1);
                    }
                    case SEG_QUADTO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 2);
                    }
                    case SEG_CUBICTO -> {
                        tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 3);
                    }
                    default -> {//SEG CLOSE

                    }
                }
                return commands[current];
            }
        };
    }

    /**
     * Returns a path iterator of the entire path.
     *
     * @param tx an optional transformation for the path iterator
     * @return the path iterator
     */

    public PathIterator getReversePathIterator(final @Nullable AffineTransform tx) {
        final AffineTransform tt = tx == null ? AffineTransform.getTranslateInstance(0, 0) : tx;
        return new PathIterator() {
            int current = commands.length - 1;
            int index = commands.length - 1;

            enum State {
                NEEDS_CLOSE,
                NEEDS_MOVE_TO,
                NEEDS_CLOSE_THEN_MOVETO,
                NEEDS_PATH_SEGMENT,
                NEEDS_PATH_SEGMENT_THEN_CLOSE
            }

            State state = State.NEEDS_MOVE_TO;

            @Override
            public int getWindingRule() {
                return windingRule;
            }

            @Override
            public boolean isDone() {
                return current < 0;
            }

            @Override
            public void next() {
                if (!isDone()) {
                    int command = commands[index];
                    switch (state) {
                        case NEEDS_CLOSE -> {
                            state = State.NEEDS_MOVE_TO;
                        }
                        case NEEDS_MOVE_TO -> {
                            if (command == SEG_CLOSE) {
                                state = State.NEEDS_PATH_SEGMENT_THEN_CLOSE;
                                index--;
                            } else {
                                state = State.NEEDS_PATH_SEGMENT;
                            }
                        }
                        case NEEDS_CLOSE_THEN_MOVETO -> {
                            state = State.NEEDS_MOVE_TO;
                        }
                        case NEEDS_PATH_SEGMENT -> {
                            index--;
                        }
                        case NEEDS_PATH_SEGMENT_THEN_CLOSE -> {
                            if (index == 1) {
                                state = State.NEEDS_CLOSE;
                            } else {
                                index--;
                            }
                        }
                    }

                    current--;
                }
            }

            @Override
            public int currentSegment(float[] coords) {
                return commands[current];
            }

            @Override
            public int currentSegment(double[] coords) {
                byte command = commands[index];
                int offset = offsets[index];
                return switch (state) {
                    case NEEDS_CLOSE, NEEDS_CLOSE_THEN_MOVETO -> {
                        yield SimplePathMetrics.SEG_CLOSE;
                    }
                    case NEEDS_MOVE_TO -> {
                        switch (command) {
                            case SEG_MOVETO, SEG_LINETO -> {
                                tt.transform(SimplePathMetrics.this.coords, offset, coords, 0, 1);
                            }
                            case SEG_QUADTO -> {
                                tt.transform(SimplePathMetrics.this.coords, offset + 2, coords, 0, 1);
                            }
                            case SEG_CUBICTO -> {
                                tt.transform(SimplePathMetrics.this.coords, offset + 4, coords, 0, 1);
                            }
                            default -> {//SEG CLOSE
                                tt.transform(SimplePathMetrics.this.coords, offset - 2, coords, 0, 1);
                            }
                        }
                        yield SimplePathMetrics.SEG_MOVETO;
                    }
                    case NEEDS_PATH_SEGMENT, NEEDS_PATH_SEGMENT_THEN_CLOSE -> {
                        switch (command) {
                            case SEG_LINETO, SEG_MOVETO -> {
                                tt.transform(SimplePathMetrics.this.coords, offset - 2, coords, 0, 1);
                            }
                            case SEG_QUADTO -> {
                                tt.transform(SimplePathMetrics.this.coords, offset - 2, coords, 0, 2);
                            }
                            case SEG_CUBICTO -> {
                                tt.transform(SimplePathMetrics.this.coords, offset - 2, coords, 0, 3);
                            }
                            default -> {//SEG CLOSE
                                throw new IllegalStateException();
                            }
                        }
                        yield command;
                    }
                };
            }
        };
    }

    @Override
    public PathIterator getPathIterator(@Nullable AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
}
