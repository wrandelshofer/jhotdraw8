package org.jhotdraw8.geom.shape;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.SvgPaths;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

public interface PathMetrics extends Shape {
    /**
     * Gets the length of the path.
     *
     * @return the length of the path in [0,Double.MAX_VALUE].
     */
    double arcLength();

    /**
     * Returns a path iterator of the entire path.
     *
     * @param tx an optional transformation for the path iterator
     * @return the path iterator
     */
    default PathIterator getPathIterator(final @Nullable AffineTransform tx) {
        return getSubPathIterator(0, arcLength(), tx);
    }

    /**
     * Evaluates the path at time t.
     *
     * @param t the time in the range [0,1]
     * @return point and tangent at t
     */
    default @NonNull PointAndDerivative eval(double t) {
        return evalAtArcLength(t * arcLength());
    }

    /**
     * Evaluates the path at the specified arc length
     *
     * @param s the arc length
     * @return point and tangent at s
     */
    @NonNull PointAndDerivative evalAtArcLength(double s);

    /**
     * Returns a path iterator of the specified sub-path.
     *
     * @param t0 the start time in [0,1].
     * @param t1 the end time in [0,1].
     * @param tx an optional transformation for the path iterator
     * @return the path iterator
     */
    default @NonNull PathIterator getSubPathIterator(double t0, double t1, final @Nullable AffineTransform tx) {
        double totalArcLength = arcLength();
        double s0 = t0 * totalArcLength;
        double s1 = t1 * totalArcLength;
        return getSubPathIteratorAtArcLength(s0, s1, tx);
    }

    /**
     * Returns a path iterator of the specified sub-path.
     *
     * @param s0 the arc length at which the sub-path starts, in [0,arcLength()].
     * @param s1 the arc length at which the sub-path ends, in [0,arcLength()].
     * @param tx an optional transformation for the path iterator
     * @return the path iterator
     */
    @NonNull PathIterator getSubPathIteratorAtArcLength(double s0, double s1, final @Nullable AffineTransform tx);

    /**
     * Gets path metrics for the reversed path.
     *
     * @return reverse path metrics
     */
    @NonNull PathMetrics reverse();

    /**
     * Implementations of PathMetrics should implement their {@code toString}
     * as follows:
     * <pre>
     * public @NonNull toString() {
     *     return PathMetrics.pathMetricsToString(this);
     * }
     * </pre>
     *
     * @param pm the path metrics implementation
     * @return a string representation
     */
    static String pathMetricsToString(@NonNull PathMetrics pm) {
        return "PathMetrics{" + pm.arcLength() + "px, \"" + SvgPaths.awtPathIteratorToDoubleSvgString(pm.getPathIterator(null)) + "\"}";
    }

    /**
     * Returns true if this path metrics is empty.
     *
     * @return true if empty
     */
    boolean isEmpty();
}