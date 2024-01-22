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
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

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
public class SimplePathMetrics extends AbstractShape implements PathMetrics {
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = (byte) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = (byte) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = (byte) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;
    private final byte @NonNull [] commands;
    private final int @NonNull [] offsets;
    private final double @NonNull [] coords;
    private final double @NonNull [] lengths;
    private final int windingRule;
    private final double epsilon;

    SimplePathMetrics(byte @NonNull [] commands, int @NonNull [] offsets, double @NonNull [] coords, double @NonNull [] lengths, int windingRule, double epsilon) {
        this.commands = commands;
        this.offsets = offsets;
        this.coords = coords;
        this.lengths = lengths;
        this.windingRule = windingRule;

        this.epsilon = epsilon;
    }

    public SimplePathMetrics(@NonNull Shape shape) {
        this(shape.getPathIterator(null), 0.125);
    }

    public SimplePathMetrics(@NonNull PathIterator pathIterator) {
        this(pathIterator, 0.125);
    }

    public SimplePathMetrics(@NonNull PathIterator pathIterator, double epsilon) {
        PathMetricsBuilder b = AwtShapes.buildFromPathIterator(new PathMetricsBuilder(), pathIterator);
        this.commands = b.commands.toByteArray();
        this.offsets = b.offsets.toIntArray();
        this.coords = b.coords.toDoubleArray();
        this.lengths = b.lengths.toDoubleArray();
        this.windingRule = b.windingRule;
        this.epsilon = epsilon;
    }

    /**
     * Evaluates the path at the specified arc length
     *
     * @param s the arc length
     * @return point and tangent at s
     */
    public @NonNull PointAndDerivative evalAtArcLength(double s) {
        int search = Arrays.binarySearch(lengths, s);
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
        double start = (i == 0 ? 0 : lengths[i - 1]);
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
    public double arcLength() {
        return lengths.length == 0 ? 0 : lengths[lengths.length - 1];
    }

    @Override
    public @NonNull PathMetrics reverse() {
        if (commands.length == 0) {
            return this;
        }

        byte @NonNull [] rCommands = new byte[commands.length];
        int @NonNull [] rOffsets = new int[offsets.length];
        double @NonNull [] rCoords = new double[coords.length];
        double @NonNull [] rLengths = new double[lengths.length];

        // reverse coordinates
        for (int i = 0, n = coords.length; i < n; i += 2) {
            rCoords[n - i - 2] = coords[i];
            rCoords[n - i - 1] = coords[i + 1];
        }

        // reverse commands, offsets and accumulated lengths
        double arcLength = lengths[lengths.length - 1];
        boolean needsClose = false;
        boolean needsMoveTo = true;

        // reverse commands
        int j = 0;
        int offset = 0;
        for (int i = commands.length - 1; i > 0; --i) {
            switch (commands[i]) {
                case SEG_MOVETO -> {
                    if (needsClose) {
                        needsClose = false;
                        rOffsets[j] = offset;
                        rLengths[j] = arcLength - lengths[i - 1];
                        rCommands[j++] = SEG_CLOSE;
                    }
                    rOffsets[j] = offset;
                    rLengths[j] = arcLength - lengths[i - 1];
                    rCommands[j] = SEG_MOVETO;
                    j++;
                    offset += 2;
                }
                case SEG_CLOSE -> {
                    needsClose = true;
                }
                default -> {
                    if (needsMoveTo) {
                        needsMoveTo = false;
                        rOffsets[j] = offset;
                        rLengths[j] = j == 0 ? 0 : rLengths[j - 1];//same as last rLength
                        rCommands[j] = SEG_MOVETO;
                        offset += 2;
                        j++;
                    }
                    rOffsets[j] = offset;
                    rLengths[j] = arcLength - lengths[i - 1];
                    rCommands[j++] = commands[i];
                    offset += switch (commands[i]) {
                        default -> 2;
                        case SEG_QUADTO -> 4;
                        case SEG_CUBICTO -> 6;
                    };
                }
            }
        }
        if (needsClose) {
            rCommands[j] = SEG_CLOSE;
            rLengths[j] = j == 0 ? 0 : rLengths[j - 1];//same as last rLength
            rOffsets[j] = offset;
        }
        return new SimplePathMetrics(rCommands, rOffsets, rCoords, rLengths, windingRule, epsilon);
    }

    @Override
    public boolean isEmpty() {
        return commands.length == 0;
    }


    @Override
    public Rectangle2D getBounds2D() {
        double minx = POSITIVE_INFINITY, miny = POSITIVE_INFINITY, maxx = NEGATIVE_INFINITY, maxy = NEGATIVE_INFINITY;
        for (int i = 0; i < coords.length; i += 2) {
            minx = Math.min(minx, coords[i]);
            maxx = Math.max(maxx, coords[i]);
            miny = Math.min(miny, coords[i + 1]);
            maxy = Math.max(maxy, coords[i + 1]);
        }

        return new Rectangle2D.Double(minx, miny, maxx - minx, maxy - miny);
    }

    @Override
    public boolean contains(double x, double y) {
        IntersectionResult result = IntersectPathIteratorPoint.intersectPathIteratorPoint(getPathIterator(null), x, y, 0);
        return result.getStatus() == IntersectionStatus.NO_INTERSECTION_INSIDE || result.getStatus() == IntersectionStatus.INTERSECTION;
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
        return getBounds2D().contains(x, y, w, h);
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
        double totalArcLength = arcLength();
        if (s0 > totalArcLength || s1 < s0) {
            return new EmptyPathIterator();
        }

        boolean startsAtFirstSegment = s0 <= 0;
        boolean endsAtLastSegment = s1 >= totalArcLength;

        if (startsAtFirstSegment && endsAtLastSegment) {
            return new FullPathIterator(this, tx);
        }

        return new SubPathIterator(s0, s1, this, tx);
    }

    /**
     * Returns a path iterator of the entire path.
     *
     * @param tx an optional transformation for the path iterator
     * @return the path iterator
     */
    @Override
    public PathIterator getPathIterator(final @Nullable AffineTransform tx) {
        return new FullPathIterator(this, tx);
    }

    @Override
    public String toString() {
        return PathMetrics.pathMetricsToString(this);
    }

    /**
     * This {@link PathIterator} iterates over the entire path of the given {@link SimplePathMetrics}
     * object.
     */
    private static class FullPathIterator implements PathIterator {
        private final @NonNull SimplePathMetrics m;
        private final @NonNull AffineTransform tt;
        int current = 0;

        public FullPathIterator(@NonNull SimplePathMetrics m, @Nullable AffineTransform tt) {
            this.m = m;
            this.tt = tt == null ? AffineTransform.getTranslateInstance(0, 0) : tt;
        }

        @Override
        public int getWindingRule() {
            return m.windingRule;
        }

        @Override
        public boolean isDone() {
            return current >= m.commands.length;
        }

        @Override
        public void next() {
            if (!isDone()) {
                current++;
            }
        }

        @Override
        public int currentSegment(float[] coords) {
            final int offset = m.offsets[current];
            switch (m.commands[current]) {
                case SEG_MOVETO, SEG_LINETO -> {
                    tt.transform(m.coords, offset, coords, 0, 1);
                }
                case SEG_QUADTO -> {
                    tt.transform(m.coords, offset, coords, 0, 2);
                }
                case SEG_CUBICTO -> {
                    tt.transform(m.coords, offset, coords, 0, 3);
                }
                default -> {//SEG CLOSE

                }
            }
            return m.commands[current];
        }

        @Override
        public int currentSegment(double[] coords) {
            final int offset = m.offsets[current];
            switch (m.commands[current]) {
                case SEG_MOVETO, SEG_LINETO -> {
                    tt.transform(m.coords, offset, coords, 0, 1);
                }
                case SEG_QUADTO -> {
                    tt.transform(m.coords, offset, coords, 0, 2);
                }
                case SEG_CUBICTO -> {
                    tt.transform(m.coords, offset, coords, 0, 3);
                }
                default -> {//SEG CLOSE

                }
            }
            return m.commands[current];
        }
    }

    /**
     * This {@link PathIterator} iterates over the entire path of the given {@link SimplePathMetrics}
     * object.
     */
    private static class SubPathIterator implements PathIterator {
        private final double s0, s1;
        private final @NonNull SimplePathMetrics m;
        private final @NonNull AffineTransform tt;
        int current = 0;
        /**
         * The index of the first command that ends inside the sub-path.
         */
        int i0;
        /**
         * The index of the first command that ends outside the sub-path.
         */
        int i1;
        double[] splitCoords = new double[8];
        private final double @NonNull [] segCoords = new double[6];
        private int segType;

        enum State {
            CLIP_FIRST_AND_LAST_SEGMENT,
            CLIP_FIRST_SEGMENT,
            INNER_SEGMENT,
            CLIP_LAST_SEGMENT,
            FINAL_SEGMENT,
            DONE
        }

        private final double epsilon = 0.125;
        private @NonNull State state;
        private boolean startsAtSegment, endsAtSegment;

        public SubPathIterator(double s0, double s1, @NonNull SimplePathMetrics m, @Nullable AffineTransform tt) {
            double totalArcLength = m.arcLength();
            this.s0 = s0 = Math.max(0, s0);
            this.s1 = s1 = Math.min(totalArcLength, Math.max(this.s0, s1));
            this.m = m;
            this.tt = tt == null ? AffineTransform.getTranslateInstance(0, 0) : tt;

            // Find the segment on which the sub-path starts
            int search0 = s0 == 0 ? 0 : Arrays.binarySearch(m.lengths, s0);
            startsAtSegment = search0 >= 0;
            i0 = search0 < 0 ? ~search0 : search0;
            // Make sure that the start segment contains s0+epsilon
            while (i0 < m.commands.length - 1 && m.lengths[i0] <= s0) {
                i0++;
            }

            // Find the segment on which the sub-path ends
            int search1 = s1 == totalArcLength ? m.commands.length - 1 : Arrays.binarySearch(m.lengths, s1);
            endsAtSegment = search1 >= 0;
            i1 = search1 < 0 ? ~search1 : search1;
            // Make sure that the end segment contains s1
            while (i1 > 0 && m.lengths[i1 - 1] >= s1) {
                i1--;
            }

            // Set the initial state
            segType = SimplePathMetrics.SEG_MOVETO;
            current = i0;
            if (startsAtSegment) {
                System.arraycopy(m.coords, m.offsets[i0 - 1], segCoords, 0, 2);
                if (endsAtSegment || i0 < i1) {
                    state = State.INNER_SEGMENT;
                } else {
                    state = State.CLIP_LAST_SEGMENT;
                }
            } else {
                int offset = m.offsets[i0];
                double ss0 = s0 - m.lengths[i0 - 1];
                double arcLength = m.lengths[i0] - m.lengths[i0 - 1];
                switch (m.commands[i0]) {
                    case SEG_LINETO -> {
                        Lines.split(m.coords, offset - 2,
                                ss0 / arcLength, null, 0, splitCoords, 0);
                    }
                    case SEG_QUADTO -> {
                        QuadCurves.split(m.coords, offset - 2,
                                QuadCurves.invArcLength(m.coords, offset - 2, ss0, arcLength, epsilon), null, 0, splitCoords, 0);
                    }
                    case SEG_CUBICTO -> {
                        CubicCurves.split(m.coords, offset - 2,
                                CubicCurves.invArcLength(m.coords, offset - 2, ss0, arcLength, epsilon), null, 0, splitCoords, 0);
                    }
                    default -> throw new AssertionError("unexpected command=" + m.commands[i0] + " at index=" + i0);
                }
                System.arraycopy(splitCoords, 0, segCoords, 0, 2);
                if (i0 == i1 && !endsAtSegment) {
                    double ss1 = s1 - m.lengths[i0 - 1];

                    switch (m.commands[i0]) {
                        case SEG_LINETO -> {
                            Lines.split(m.coords, offset - 2,
                                    ss1 / arcLength, splitCoords, 0, null, 0);
                        }
                        case SEG_QUADTO -> {
                            QuadCurves.split(splitCoords, 0,
                                    QuadCurves.invArcLength(splitCoords, 0, ss1 - ss0, arcLength, epsilon), splitCoords, 0, null, 0);
                        }
                        case SEG_CUBICTO -> {
                            CubicCurves.split(splitCoords, 0,
                                    CubicCurves.invArcLength(splitCoords, 0, ss1 - ss0, arcLength, epsilon), splitCoords, 0, null, 0);
                        }
                        default -> throw new AssertionError("unexpected command=" + m.commands[i0] + " at index=" + i0);
                    }
                    state = State.CLIP_FIRST_AND_LAST_SEGMENT;
                } else {
                    state = State.CLIP_FIRST_SEGMENT;
                }
            }
        }

        @Override
        public int getWindingRule() {
            return m.windingRule;
        }

        @Override
        public boolean isDone() {
            return state == State.DONE;
        }

        @Override
        public void next() {
            switch (state) {
                case CLIP_FIRST_SEGMENT -> {
                    segType = m.commands[current];
                    switch (segType) {
                        case SEG_LINETO -> {
                            System.arraycopy(splitCoords, 2, segCoords, 0, 2);
                        }
                        case SEG_QUADTO -> {
                            System.arraycopy(splitCoords, 2, segCoords, 0, 4);
                        }
                        case SEG_CUBICTO -> {
                            System.arraycopy(splitCoords, 2, segCoords, 0, 6);
                        }
                        default -> throw new AssertionError("unexpected command=" + segType);
                    }
                    current++;
                    if (current < i1) {
                        state = State.INNER_SEGMENT;
                    } else if (endsAtSegment) {
                        if (i1 == i0) {
                            state = State.FINAL_SEGMENT;
                        } else {
                            state = State.INNER_SEGMENT;
                        }
                    } else {
                        state = State.CLIP_LAST_SEGMENT;
                    }
                }
                case INNER_SEGMENT -> {
                    segType = m.commands[current];
                    final int offset = m.offsets[current];
                    switch (m.commands[current]) {
                        case SEG_MOVETO, SEG_LINETO -> {
                            tt.transform(m.coords, offset, segCoords, 0, 1);
                        }
                        case SEG_QUADTO -> {
                            tt.transform(m.coords, offset, segCoords, 0, 2);
                        }
                        case SEG_CUBICTO -> {
                            tt.transform(m.coords, offset, segCoords, 0, 3);
                        }
                        default -> {//SEG CLOSE

                        }
                    }
                    current++;
                    if (endsAtSegment && current > i1) {
                        state = State.FINAL_SEGMENT;
                    } else if (current >= i1) {
                        state = State.CLIP_LAST_SEGMENT;
                    }
                }
                case CLIP_LAST_SEGMENT -> {
                    int offset = m.offsets[i1];
                    double startLength = i1 > 0 ? m.lengths[i1 - 1] : 0;
                    double s = s1 - startLength;
                    double arcLength = m.lengths[i1] - startLength;
                    segType = m.commands[i1];
                    switch (segType) {
                        case SEG_LINETO -> {
                            Lines.split(m.coords, offset - 2,
                                    s / arcLength, splitCoords, 0, null, 0);
                        }
                        case SEG_QUADTO -> {
                            QuadCurves.split(m.coords, offset - 2,
                                    QuadCurves.invArcLength(m.coords, offset - 2, s, arcLength, epsilon), splitCoords, 0, null, 0);
                        }
                        case SEG_CUBICTO -> {
                            CubicCurves.split(m.coords, offset - 2,
                                    CubicCurves.invArcLength(m.coords, offset - 2, s, arcLength, epsilon), splitCoords, 0, null, 0);
                        }
                        default -> throw new AssertionError("unexpected command=" + segType);
                    }
                    System.arraycopy(splitCoords, 2, segCoords, 0, 6);
                    state = State.FINAL_SEGMENT;
                }
                case CLIP_FIRST_AND_LAST_SEGMENT -> {
                    segType = m.commands[i1];
                    System.arraycopy(splitCoords, 2, segCoords, 0, 6);
                    state = State.FINAL_SEGMENT;
                }
                default -> {
                    state = State.DONE;
                }
            }
        }

        @Override
        public int currentSegment(double[] coords) {
            switch (segType) {
                case SEG_MOVETO, SEG_LINETO -> tt.transform(segCoords, 0, coords, 0, 1);
                case SEG_QUADTO -> tt.transform(segCoords, 0, coords, 0, 2);
                case SEG_CUBICTO -> tt.transform(segCoords, 0, coords, 0, 3);
                case SEG_CLOSE -> {
                }
                default -> throw new NoSuchElementException();
            }
            return segType;
        }

        @Override
        public int currentSegment(float[] coords) {
            switch (segType) {
                case SEG_MOVETO, SEG_LINETO -> tt.transform(segCoords, 0, coords, 0, 1);
                case SEG_QUADTO -> tt.transform(segCoords, 0, coords, 0, 2);
                case SEG_CUBICTO -> tt.transform(segCoords, 0, coords, 0, 3);
                case SEG_CLOSE -> {
                }
                default -> throw new NoSuchElementException();
            }
            return segType;
        }
    }
}
