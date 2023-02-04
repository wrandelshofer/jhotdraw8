package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;

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
    private final byte[] commands;
    private final int[] offsets;
    private final double[] coords;
    private final double[] accumulatedLengths;
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = (byte) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = (byte) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = (byte) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;


    public PathMetrics(byte[] commands, int[] offsets, double[] coords, double[] accumulatedLengths) {
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
    public @NonNull PointAndTangent eval(double t) {
        return evalAtArcLength(t * getLength());
    }

    /**
     * Evaluates the path at the specified arc length
     *
     * @param s the arc length
     * @return point and tangent at s
     */
    public @NonNull PointAndTangent evalAtArcLength(double s) {
        int search = Arrays.binarySearch(accumulatedLengths, s);
        int i = search < 0 ? ~search : search;

        if (commands[i] == SEG_CLOSE) {
            // Use the last segment of the shape that is being closed
            i--;
        } else if (commands[i] == SEG_MOVETO) {
            // Use the first segment of the shape that is being opened
            i++;
        }

        int offset = offsets[i];
        double start = (i == 0 ? 0 : accumulatedLengths[i - 1]);
        final double segmentS = s - start;
        return switch (commands[i]) {
            case SEG_CLOSE -> new PointAndTangent(0, 0, 1, 0);
            case SEG_MOVETO -> new PointAndTangent(coords[offset], coords[offset + 1], 1, 0);
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
    public double getLength() {
        return accumulatedLengths[accumulatedLengths.length - 1];
    }
}
