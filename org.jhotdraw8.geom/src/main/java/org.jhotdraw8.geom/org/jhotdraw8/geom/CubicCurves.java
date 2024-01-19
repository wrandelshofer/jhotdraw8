/*
 * @(#)CubicCurves.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.DoubleConsumer6;
import org.jhotdraw8.base.function.DoubleConsumer8;
import org.jhotdraw8.base.function.ToFloatFunction;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.function.ToDoubleFunction;

import static org.jhotdraw8.geom.Lines.lerp;

/**
 * Provides utility methods for Bézier curves.
 *
 * @author Werner Randelshofer
 */
public class CubicCurves {

    /**
     * Don't let anyone instantiate this class.
     */
    private CubicCurves() {
    }

    /**
     * Evaluates the given curve at the specified time.
     *
     * @param a       points of the curve
     * @param offsetA index of the first point in array {@code a}
     * @param t       the time
     * @return the point at time t
     */
    public static @NonNull PointAndDerivative eval(double[] a, int offsetA, double t) {
        return eval(
                a[offsetA], a[offsetA + 1],
                a[offsetA + 2], a[offsetA + 3],
                a[offsetA + 4], a[offsetA + 5],
                a[offsetA + 6], a[offsetA + 7],
                t);
    }

    /**
     * Evaluates the given curve at the specified time.
     *
     * @param x0 point P0 of the curve
     * @param y0 point P0 of the curve
     * @param x1 point P1 of the curve
     * @param y1 point P1 of the curve
     * @param x2 point P2 of the curve
     * @param y2 point P2 of the curve
     * @param x3 point P3 of the curve
     * @param y3 point P3 of the curve
     * @param t  the time
     * @return the point at time t
     */
    public static @NonNull PointAndDerivative eval(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
                                                   double t) {

        final double x01, y01, x12, y12, x23, y23, x012, y012, x123, y123, x0123, y0123;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);

        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);

        x23 = lerp(x2, x3, t);
        y23 = lerp(y2, y3, t);

        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        x123 = lerp(x12, x23, t);
        y123 = lerp(y12, y23, t);

        x0123 = lerp(x012, x123, t);
        y0123 = lerp(y012, y123, t);

        return new PointAndDerivative(x0123, y0123, x123 - x012, y123 - y012);
    }

    /**
     * Tries to merge two bézier curves. Returns the new control point.
     *
     * @param tolerance distance (radius) at which the joined point may be off from x0123,y0123.
     * @return the control points of the new curve (x0,y0)(x1,y1)(x2,y2)(x3,y3), null if merging failed
     */
    public static double @Nullable [] merge(double[] a, int offsetA,
                                            double[] b, int offsetB,
                                            double tolerance) {
        if (Points.squaredDistance(a[offsetA + 6], a[offsetA + 7], b[offsetB], b[offsetB + 1]) > tolerance * tolerance) {
            return null;
        }
        return merge(
                a[offsetA], a[offsetA + 1],
                a[offsetA + 2], a[offsetA + 3],
                a[offsetA + 4], a[offsetA + 5],
                a[offsetA + 6], a[offsetA + 7],
                //b[offsetB], b[offsetB + 1],
                b[offsetB + 2], b[offsetB + 3],
                b[offsetB + 4], b[offsetB + 5],
                b[offsetB + 6], b[offsetB + 7],
                tolerance);
    }

    /**
     * Tries to merge two bézier curves. Returns the new control point.
     *
     * @param x0        point P0 of the first curve
     * @param y0        point P0 of the first curve
     * @param x01       point P1 of the first curve
     * @param y01       point P1 of the first curve
     * @param x012      point P2 of the first curve
     * @param y012      point P2 of the first curve
     * @param x0123     point P3 of the first curve or point p0 of the second curve respectively
     * @param y0123     point P3 of the first curve or point p0 of the second curve respectively
     * @param x123      point P1 of the second curve
     * @param y123      point P1 of the second curve
     * @param x23       point P2 of the second curve
     * @param y23       point P2 of the second curve
     * @param x3        point P3 of the second curve
     * @param y3        point P3 of the second curve
     * @param tolerance distance (radius) at which the joined point may be off from x0123,y0123.
     * @return the control points of the new curve (x0,y0)(x1,y1)(x2,y2)(x3,y3), null if merging failed
     */
    public static double @Nullable [] merge(final double x0, final double y0, final double x01, final double y01,
                                            final double x012, final double y012, final double x0123, final double y0123,
                                            final double x123, final double y123,
                                            final double x23, final double y23, final double x3, final double y3,
                                            double tolerance) {

        final double t = (x012 - x123 == 0) ? (y012 - y0123) / (y012 - y123) : (x012 - x0123) / (x012 - x123);
        final Point2D.Double ctrl1, ctrl2;
        if (t == 0 || t == 1) {
            ctrl1 = new Point2D.Double(x01, y01);
            ctrl2 = new Point2D.Double(x23, y23);
        } else {
            ctrl1 = Points2D.add(Points2D.divide(Points2D.subtract(x01, y01, x0, y0), t), x0, y0);
            ctrl2 = Points2D.add(Points2D.divide(Points2D.subtract(x23, y23, x3, y3), 1 - t), x3, y3);
        }

        final Point2D.Double joint0123 = eval(x0, y0, ctrl1.getX(), ctrl1.getY(), ctrl2.getX(), ctrl2.getY(), x3, y3, t).getPoint(Point2D.Double::new);

        return joint0123.distanceSq(x0123, y0123) <= tolerance * tolerance
                ? new double[]{x0, y0, ctrl1.getX(), ctrl1.getY(), ctrl2.getX(), ctrl2.getY(), x3, y3} : null;
    }

    /**
     * Splits the provided bezier curve into two parts at the specified
     * parameter value {@code t}.
     * <p>
     * Reference:
     * <dl>
     *     <dt>Stackoverflow, Splitting a bezier curve, Copyright Jonathan, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/8369488/splitting-a-bezier-curve">stackoverflow.com</a></dd>
     * </dl>
     * .
     */
    public static SimpleOrderedPair<CubicCurve2D.Double, CubicCurve2D.Double> split(CubicCurve2D.Double source,
                                                                                    double t) {
        CubicCurve2D.Double left = new CubicCurve2D.Double();
        CubicCurve2D.Double right = new CubicCurve2D.Double();
        split(source.x1, source.y1,
                source.ctrlx1, source.ctrly1,
                source.ctrlx2, source.ctrly2,
                source.x2, source.y2,
                t,
                left::setCurve, right::setCurve);
        return new SimpleOrderedPair<>(left, right);
    }

    /**
     * Splits the provided bezier curve into two parts at the specified
     * parameter value {@code t}.
     * <p>
     * Reference:
     * <dl>
     *     <dt>Stackoverflow, Splitting a bezier curve, Copyright Jonathan, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/8369488/splitting-a-bezier-curve">stackoverflow.com</a></dd>
     * </dl>
     */
    public static void split(CubicCurve2D.Double source,
                             double t,
                             CubicCurve2D.Double left,
                             CubicCurve2D.Double right) {
        split(source.x1, source.y1, source.ctrlx1, source.ctrly1, source.ctrlx2, source.ctrly2, source.x2, source.y2,
                t,

                left::setCurve, right::setCurve);
    }

    /**
     * Splits the provided bezier curve into two parts at the specified
     * parameter value {@code t}.
     * <p>
     * Reference:
     * <dl>
     *     <dt>Stackoverflow, Splitting a bezier curve, Copyright Jonathan, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/8369488/splitting-a-bezier-curve">stackoverflow.com</a></dd>
     * </dl>
     *
     * @param x0    point P0 of the curve
     * @param y0    point P0 of the curve
     * @param x1    point P1 of the curve
     * @param y1    point P1 of the curve
     * @param x2    point P2 of the curve
     * @param y2    point P2 of the curve
     * @param x3    point P3 of the curve
     * @param y3    point P3 of the curve
     * @param t     where to split
     * @param left  if not null, accepts the curve from x1,y1 to t
     * @param right if not null, accepts the curve from t to x4,y4
     */
    public static void splitCubicCurveTo(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
                                         double t,
                                         @Nullable DoubleConsumer6 left,
                                         @Nullable DoubleConsumer6 right) {
        split(x0, y0, x1, y1, x2, y2, x3, y3, t,
                left == null ? null : (lx, ly, lx1, ly1, lx2, ly2, lx3, ly3) -> left.accept(lx1, ly1, lx2, ly2, lx3, ly3),
                right == null ? null : (lx, ly, lx1, ly1, lx2, ly2, lx3, ly3) -> right.accept(lx1, ly1, lx2, ly2, lx3, ly3)
        );
    }

    /**
     * Splits the provided bezier curve into two parts at the specified
     * parameter value {@code t}.
     * <p>
     * Reference:
     * <dl>
     *     <dt>Stackoverflow, Splitting a bezier curve, Copyright Jonathan, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/8369488/splitting-a-bezier-curve">stackoverflow.com</a></dd>
     * </dl>
     *
     * @param x0    point P0 of the curve
     * @param y0    point P0 of the curve
     * @param x1    point P1 of the curve
     * @param y1    point P1 of the curve
     * @param x2    point P2 of the curve
     * @param y2    point P2 of the curve
     * @param x3    point P3 of the curve
     * @param y3    point P3 of the curve
     * @param t     where to split
     * @param first  if not null, accepts the curve from x1,y1 to t
     * @param second if not null, accepts the curve from t to x4,y4
     */
    public static void split(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
                             double t,
                             @Nullable DoubleConsumer8 first,
                             @Nullable DoubleConsumer8 second) {
        final double x01, y01, x12, y12, x23, y23, x012, y012, x123, y123, x0123, y0123;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);
        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);
        x23 = lerp(x2, x3, t);
        y23 = lerp(y2, y3, t);
        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);
        x123 = lerp(x12, x23, t);
        y123 = lerp(y12, y23, t);
        x0123 = lerp(x012, x123, t);
        y0123 = lerp(y012, y123, t);

        if (first != null) {
            first.accept(x0, y0, x01, y01, x012, y012, x0123, y0123);
        }
        if (second != null) {
            second.accept(x0123, y0123, x123, y123, x23, y23, x3, y3);
        }
    }

    public static void split(double[] p, int o,
                             double t,
                             double[] f, int fo,
                             double[] s, int so) {
        final double x0 = p[o], y0 = p[o + 1], x1 = p[o + 2], y1 = p[o + 3], x2 = p[o + 4], y2 = p[o + 5], x3 = p[o + 6], y3 = p[o + 7];
        final double x01, y01, x12, y12, x23, y23, x012, y012, x123, y123, x0123, y0123;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);
        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);
        x23 = lerp(x2, x3, t);
        y23 = lerp(y2, y3, t);
        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);
        x123 = lerp(x12, x23, t);
        y123 = lerp(y12, y23, t);
        x0123 = lerp(x012, x123, t);
        y0123 = lerp(y012, y123, t);

        if (f != null) {
            f[fo + 0] = x0;
            f[fo + 1] = y0;
            f[fo + 2] = x01;
            f[fo + 3] = y01;
            f[fo + 4] = x012;
            f[fo + 5] = y012;
            f[fo + 6] = x0123;
            f[fo + 7] = y0123;
        }
        if (s != null) {
            s[so + 0] = x0123;
            s[so + 1] = y0123;
            s[so + 2] = x123;
            s[so + 3] = y123;
            s[so + 4] = x23;
            s[so + 5] = y23;
            s[so + 6] = x3;
            s[so + 7] = y3;
        }
    }

    /**
     * Splits the provided bezier curve into two parts.
     * <p>
     * Reference:
     * <dl>
     *     <dt>Stackoverflow, Splitting a bezier curve, Copyright Jonathan, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/8369488/splitting-a-bezier-curve">stackoverflow.com</a></dd>
     * </dl>
     *
     * @param x0    point P0 of the curve
     * @param y0    point P0 of the curve
     * @param x1    point P1 of the curve
     * @param y1    point P1 of the curve
     * @param x2    point P2 of the curve
     * @param y2    point P2 of the curve
     * @param x3    point P3 of the curve
     * @param y3    point P3 of the curve
     * @param t     where to split
     * @param left  if not null, accepts the curve from x1,y1 to t
     * @param right if not null, accepts the curve from t to x4,y4
     */
    public static void split(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double t,
                             double[] left,
                             double[] right) {
        split(x0, y0, x1, y1, x2, y2, x3, y3, t,
                left == null ? null : (x, y, a, b, c, d, e, f) -> {
                    left[0] = a;
                    left[1] = b;
                    left[2] = c;
                    left[3] = d;
                    left[4] = e;
                    left[5] = f;
                },
                right == null ? null : (x, y, a, b, c, d, e, f) -> {
                    right[0] = a;
                    right[1] = b;
                    right[2] = c;
                    right[3] = d;
                    right[4] = e;
                    right[5] = f;
                });

    }

    /**
     * Extracts the specified segment [ta,tb] from the given cubic curve.
     *
     * @param q             the cubic bezier curve
     * @param qOffset       the index of the first curve parameter in q
     * @param ta            where to split from
     * @param tb            where to split to
     * @param segment       the output array
     * @param segmentOffset the index of the first curve parameter in segment
     */

    public static void subCurve(double @NonNull [] q, int qOffset,
                                double ta, double tb,
                                double @NonNull [] segment, int segmentOffset) {
        double tab = ta / tb;
        split(q, qOffset,
                tb, null, 0, segment, segmentOffset);
        split(segment, segmentOffset, tab,
                null, 0, segment, segmentOffset);
    }

    /**
     * From paper.js, src/path/Curve.js, MIT License.
     * <p>
     * References:
     * <dl>
     *     <dt>paper.js. Copyright (c) 2011 - 2020 Jürg Lehni &amp; Jonathan Puckey. MIT License.</dt>
     *     <dd><a href="https://github.com/paperjs/paper.js/blob/develop/src/path/Curve.js">github.com</a></dd>
     * </dl>
     *
     * @param v      a cubic bezier curve
     * @param offset offset into array v
     * @return the arc length integrand of a cubic Bézier curve
     */
    public static ToDoubleFunction<Double> getArcLengthIntegrand(double[] v, int offset) {
        // Instead of the code below, we could evaluate the magnitude of the derivative
        /*
        return (t)-> {
            PointAndDerivative p = eval(v, offset, t);
            return Math.hypot(p.dx(),p.dy());
            //return Math.sqrt(p.dx()*p.dx()+p.dy()*p.dy());
        };
        */

        // Calculate the coefficients of a Bezier derivative.
        double x0 = v[offset], y0 = v[offset + 1],
                x1 = v[offset + 2], y1 = v[offset + 3],
                x2 = v[offset + 4], y2 = v[offset + 5],
                x3 = v[offset + 6], y3 = v[offset + 7],

                ax = 9 * (x1 - x2) + 3 * (x3 - x0),
                bx = 6 * (x0 + x2) - 12 * x1,
                cx = 3 * (x1 - x0),

                ay = 9 * (y1 - y2) + 3 * (y3 - y0),
                by = 6 * (y0 + y2) - 12 * y1,
                cy = 3 * (y1 - y0);

        return (t) -> {
            // Calculate quadratic equations of derivatives for x and y
            double dx = Math.fma(Math.fma(ax, t, bx), t, cx),
                    dy = Math.fma(Math.fma(ay, t, by), t, cy);
            //return Math.hypot(dx, dy);// hypot does not run into intermediate overflows and underflows
            return Math.sqrt(dx * dx + dy * dy);
        };
    }

    public static ToFloatFunction<Float> getArcLengthIntegrandFloat(double[] v, int offset) {
        // Instead of the code below, we could evaluate the magnitude of the derivative
        /*
        return (t)-> {
            PointAndDerivative p = eval(v, offset, t);
            return Math.hypot(p.dx(),p.dy());
            //return Math.sqrt(p.dx()*p.dx()+p.dy()*p.dy());
        };
        */

        // Calculate the coefficients of a Bezier derivative.
        double x0 = v[offset], y0 = v[offset + 1],
                x1 = v[offset + 2], y1 = v[offset + 3],
                x2 = v[offset + 4], y2 = v[offset + 5],
                x3 = v[offset + 6], y3 = v[offset + 7];

        float
                ax = (float) (9 * (x1 - x2) + 3 * (x3 - x0)),
                bx = (float) (6 * (x0 + x2) - 12 * x1),
                cx = (float) (3 * (x1 - x0)),

                ay = (float) (9 * (y1 - y2) + 3 * (y3 - y0)),
                by = (float) (6 * (y0 + y2) - 12 * y1),
                cy = (float) (3 * (y1 - y0));

        return (t) -> {
            // Calculate quadratic equations of derivatives for x and y
            float dx = Math.fma(Math.fma(ax, t, bx), t, cx),
                    dy = Math.fma(Math.fma(ay, t, by), t, cy);
            //return Math.hypot(dx, dy);// hypot does not run into intermediate overflows and underflows
            return (float) Math.sqrt(dx * dx + dy * dy);
        };
    }

    public static double[] toArray(CubicCurve2D.Double c) {
        return new double[]{c.x1, c.y1, c.ctrlx1, c.ctrly1, c.ctrlx2, c.ctrly2, c.x2, c.y2};
    }

    /**
     * Computes the arc length s.
     *
     * @param p       points of the curve
     * @param offset  index of the first point in array {@code p}
     * @param epsilon
     * @return the arc length
     */
    public static double arcLength(double @NonNull [] p, int offset, double epsilon) {
        return arcLength(p, offset, 1, epsilon);
    }

    /**
     * Computes the arc length s from time 0 to time t.
     *
     * @param p       points of the curve
     * @param offset  index of the first point in array {@code p}
     * @param t       the time
     * @param epsilon the error tolerance
     * @return the arc length
     */
    public static double arcLength(double @NonNull [] p, int offset, double t, double epsilon) {
        return arcLengthIntegrated(p, offset, t, epsilon);
    }

    public static float arcLengthFloat(double @NonNull [] p, int offset, double t, double epsilon) {
        return arcLengthIntegratedFloat(p, offset, t, epsilon);
    }

    /**
     * Computes the arc length s from time 0 to time t using an integration method.
     *
     * @param p       points of the curve
     * @param offset  index of the first point in array {@code p}
     * @param t       the time
     * @param epsilon the error tolerance
     * @return the arc length
     */
    public static double arcLengthIntegrated(double @NonNull [] p, int offset, double t, double epsilon) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(p, offset);
        return Integrals.rombergQuadrature(f, 0, t, epsilon);
    }

    /**
     * Computes the arc length s from time 0 to time t using an integration method.
     *
     * @param p       points of the curve
     * @param offset  index of the first point in array {@code p}
     * @param t       the time
     * @param epsilon the error tolerance
     * @return the arc length
     */
    public static float arcLengthIntegratedFloat(double @NonNull [] p, int offset, double t, double epsilon) {
        ToFloatFunction<Float> f = getArcLengthIntegrandFloat(p, offset);
        return Integrals.rombergQuadratureFloat(f, 0, (float) t, (float) epsilon);
    }



    /**
     * Computes time t at the given arc length s.
     *
     * @param p       points of the curve
     * @param offset  index of the first point in array {@code p}
     * @param s       arc length
     * @param epsilon
     * @return t at s
     */
    public static double invArcLength(double @NonNull [] p, int offset, double s, double epsilon) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(p, offset);
        return Solvers.hybridNewtonBisectionMethod(Integrals::rombergQuadrature, f, s, 0, 1, s / arcLength(p, offset, 1, epsilon), epsilon);
    }

    public static float invArcLengthFloat(double @NonNull [] p, int offset, float s, float epsilon) {
        ToFloatFunction<Float> f = getArcLengthIntegrandFloat(p, offset);
        return Solvers.hybridNewtonBisectionMethodFloat(Integrals::rombergQuadratureFloat, f, s, 0, 1, s / arcLengthFloat(p, offset, 1, epsilon), epsilon);
    }
}
