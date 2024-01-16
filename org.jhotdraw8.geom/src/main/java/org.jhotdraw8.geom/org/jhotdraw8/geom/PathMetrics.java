package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

public interface PathMetrics {
    /**
     * Gets the length of the path.
     *
     * @return the length of the path in [0,Double.MAX_VALUE].
     */
    double getArcLength();

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
    default <T> PathBuilder<T> buildSubPath(double t0, double t1, @NonNull PathBuilder<T> b, boolean skipFirstMoveTo) {
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
    default <T> @NonNull PathBuilder<T> buildSubPathAtArcLength(double s0, double s1, @NonNull PathBuilder<T> b) {
        return buildSubPathAtArcLength(s0, s1, b, false);
    }

    /**
     * Builds a sub-path from arc-lengths s0 to s1.
     * <p>
     * This method does not call {@link PathBuilder#build()}.
     *
     * @param s0              the arc length at which the sub-path starts, in [0,getArcLength()].
     * @param s1              the arc length at which the sub-path ends, in [0,getArcLength()].
     * @param b               the builder
     * @param skipFirstMoveTo whether to skip the first moveTo
     * @return the same builder that was passed as an argument
     */
    <T> @NonNull PathBuilder<T> buildSubPathAtArcLength(double s0, double s1, @NonNull PathBuilder<T> b, boolean skipFirstMoveTo);

    /**
     * Returns a path iterator of the entire path.
     *
     * @param tx an optional transformation for the path iterator
     * @return the path iterator
     */
    default PathIterator getPathIterator(final @Nullable AffineTransform tx) {
        return getSubPathIterator(0, getArcLength(), tx);
    }

    /**
     * Evaluates the path at time t.
     *
     * @param t the time in the range [0,1]
     * @return point and tangent at t
     */
    default @NonNull PointAndDerivative eval(double t) {
        return evalAtArcLength(t * getArcLength());
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
        double totalArcLength = getArcLength();
        double s0 = t0 * totalArcLength;
        double s1 = t1 * totalArcLength;
        return getSubPathIteratorAtArcLength(s0, s1, tx);
    }

    /**
     * Returns a path iterator of the specified sub-path.
     *
     * @param s0 the arc length at which the sub-path starts, in [0,getArcLength()].
     * @param s1 the arc length at which the sub-path ends, in [0,getArcLength()].
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
        return "PathMetrics{" + pm.getArcLength() + "px, \"" + SvgPaths.doubleSvgStringFromAwt(pm.getPathIterator(null)) + "\"}";
    }
}
