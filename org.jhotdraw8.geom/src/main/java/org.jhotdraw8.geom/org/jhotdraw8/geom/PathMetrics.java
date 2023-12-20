/*
 * @(#)PathMetrics.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
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
public class PathMetrics {
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


    PathMetrics(byte @NonNull [] commands, int @NonNull [] offsets, double @NonNull [] coords, double @NonNull [] accumulatedLengths) {
        this.commands = commands;
        this.offsets = offsets;
        this.coords = coords;
        this.accumulatedLengths = accumulatedLengths;
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
        int search0 = t0 == 0 ? 0 : Arrays.binarySearch(accumulatedLengths, s0);
        boolean startsAtSegment = search0 >= 0;
        int i0 = search0 < 0 ? ~search0 : search0;
        if (i0 == 0 && skipFirstMoveTo) {
            i0++;
        }
        while (i0 < commands.length && commands[i0] == SEG_CLOSE) {
            i0++;
        }

        int search1 = t1 == 1 ? commands.length - 1 : Arrays.binarySearch(accumulatedLengths, s1);
        boolean endsAtSegment = search1 >= 0;
        int i1 = search1 < 0 ? ~search1 : search1;
        while (i1 >= 0 && commands[i1] == SEG_MOVETO) {
            i1--;
        }
        if (i1 < i0) return b;

        double[] splitCoords = new double[8];
        if (i0 == i1 && !startsAtSegment && !endsAtSegment) {
            int offset = offsets[i0];
            double sStart = s0 - accumulatedLengths[i0 - 1];
            double sEnd = s1 - accumulatedLengths[i0 - 1];
            double arcLength = accumulatedLengths[i0] - accumulatedLengths[i0 - 1];
            switch (commands[i0]) {
                case SEG_CLOSE -> b.closePath();
                case SEG_MOVETO -> b.moveTo(coords[offset], coords[offset + 1]);
                case SEG_LINETO -> {
                    Lines.subLine(coords, offset - 2,
                            sStart / arcLength, sEnd / arcLength, splitCoords, 0);
                    b.moveTo(splitCoords[0], splitCoords[1]);
                    b.lineTo(splitCoords[2], splitCoords[3]);
                }
                case SEG_QUADTO -> {
                    QuadCurves.subCurve(coords, offset - 2,
                            QuadCurves.invArcLength(coords, offset - 1, sStart),
                            QuadCurves.invArcLength(coords, offset - 1, sEnd),
                            splitCoords, 0);
                    b.moveTo(splitCoords[0], splitCoords[1]);
                    b.quadTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5]);
                }
                case SEG_CUBICTO -> {
                    CubicCurves.subCurve(coords, offset - 2,
                            QuadCurves.invArcLength(coords, offset - 1, sStart),
                            QuadCurves.invArcLength(coords, offset - 1, sEnd),
                            splitCoords, 0);
                    b.moveTo(splitCoords[0], splitCoords[1]);
                    b.curveTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5], splitCoords[6], splitCoords[7]);
                }
                default -> throw new IllegalStateException("unexpected command=" + commands[i0] + " at index=" + i0);
            }
            return b;
        }

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
                    b.moveTo(splitCoords[0], splitCoords[1]);
                    b.lineTo(splitCoords[2], splitCoords[3]);
                }
                case SEG_QUADTO -> {
                    QuadCurves.split(coords, offset - 2,
                            QuadCurves.invArcLength(coords, offset - 1, s), null, 0, splitCoords, 0);
                    b.moveTo(splitCoords[0], splitCoords[1]);
                    b.quadTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5]);
                }
                case SEG_CUBICTO -> {
                    CubicCurves.split(coords, offset - 2,
                            CubicCurves.invArcLength(coords, offset - 1, s), null, 0, splitCoords, 0);
                    b.moveTo(splitCoords[0], splitCoords[1]);
                    b.curveTo(splitCoords[2], splitCoords[3], splitCoords[4], splitCoords[5], splitCoords[6], splitCoords[7]);
                }
                default -> throw new IllegalStateException("unexpected command=" + commands[i0] + " at index=" + i0);
            }
        } else {
            if (commands[i0] != SEG_MOVETO) {
                int offset = offsets[i0];
                if (offset > 0) {
                    b.moveTo(coords[offset - 2], coords[offset - 1]);
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


    public PathIterator getPathIterator(final @Nullable AffineTransform tx) {
        final AffineTransform tt = tx == null ? AffineTransform.getTranslateInstance(0, 0) : tx;
        return new PathIterator() {
            int current = -1;

            @Override
            public int getWindingRule() {
                return PathIterator.WIND_EVEN_ODD;
            }

            @Override
            public boolean isDone() {
                return current < commands.length - 1;
            }

            @Override
            public void next() {
                if (!isDone()) current++;
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
        };
    }
}
