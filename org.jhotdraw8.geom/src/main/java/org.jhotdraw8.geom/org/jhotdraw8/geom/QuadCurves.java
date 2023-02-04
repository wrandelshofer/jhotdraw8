/*
 * @(#)BezierCurves.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.Double4Consumer;
import org.jhotdraw8.geom.intersect.IntersectRayRay;
import org.jhotdraw8.geom.intersect.IntersectionResultEx;

import java.awt.geom.Point2D;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.*;
import static org.jhotdraw8.geom.Lines.lerp;

/**
 * Provides utility methods for quadratic Bézier curves.
 * <p>
 * Quadratic Bezier curves are defined by second order polynominal, and can be written as:
 * <pre>
 * B(t) = (1 − t)*P₀ + 2*(1 − t)*t*P₁ + t²*P₂
 * </pre>
 * where t is real parameter with values in range [0,1]. P’s are respectively curve starting point,
 * anchor point and the end point. Derivative of the quadratic Bezier curve can be written as
 * Quadratic Bezier Curve derivative:
 * <pre>
 * ∂B∂t(t) = 2*t*(P₀ − 2*P₁ + P₂) + 2*P₁ − 2*P₀
 * </pre>
 * </p>
 * References:
 * <dl>
 *     <dt>Quadratic Bezier curves, Copyright malczak</dt>
 *     <dd><a href="https://malczak.info/blog/quadratic-bezier-curve-length">malczak.info</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class QuadCurves {

    /**
     * Don't let anyone instantiate this class.
     */
    private QuadCurves() {
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
     * @param t  the time
     * @return the point at time t
     */
    public static @NonNull PointAndTangent eval(double x0, double y0, double x1, double y1, double x2, double y2, double t) {
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);

        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);

        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        return new PointAndTangent(x012, y012, x12 - x01, y12 - y01);
    }

    public static @NonNull PointAndTangent eval(double[] p, int offset, double t) {
        double x0 = p[offset], y0 = p[offset + 1], x1 = p[offset + 2], y1 = p[offset + 3], x2 = p[offset + 4], y2 = p[offset + 5];
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);

        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);

        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        return new PointAndTangent(x012, y012, x12 - x01, y12 - y01);
    }


    /**
     * Tries to join two bézier curves. Returns the new control point.
     *
     * @param x0        point P0 of the first curve
     * @param y0        point P0 of the first curve
     * @param x01       point P1 of the first curve
     * @param y01       point P1 of the first curve
     * @param x012      point P2 of the first curve or point p0 of the second curve respectively
     * @param y012      point P2 of the first curve or point p0 of the second curve respectively
     * @param x12       point P1 of the second curve
     * @param y12       point P1 of the second curve
     * @param x2        point P2 of the second curve
     * @param y2        point P2 of the second curve
     * @param tolerance distance (radius) at which the joined point may be off from x012,y012.
     * @return the control points of the new curve (x0,y0)(x1,y1)(x2,y2), null if joining failed
     */
    public static @Nullable double[] merge(final double x0, final double y0, final double x01, final double y01,
                                           final double x012, final double y012, final double x12, final double y12, final double x2, final double y2,
                                           double tolerance) {
        final Point2D.Double start = new Point2D.Double(x0, y0);
        @NonNull Point2D b0 = new Point2D.Double(x2, y2);

        final IntersectionResultEx isect = IntersectRayRay.intersectRayRayEx(start, Points2D.subtract(new Point2D.Double(x01, y01), start),
                b0, Points2D.subtract(new Point2D.Double(x12, y12), b0));
        if (isect.isEmpty()) {
            return null;
        }
        final Point2D.Double ctrl = isect.getLast();

        final double t = start.distance(x01, y01) / start.distance(ctrl);
        final Point2D.Double joint01 = eval(x0, y0, ctrl.getX(), ctrl.getY(), x2, y2, t).getPoint(Point2D.Double::new);

        return (joint01.distance(x012, y012) <= tolerance) ?
                new double[]{x0, y0, ctrl.getX(), ctrl.getY(), x2, y2} : null;
    }

    public static void split(double x0, double y0, double x1, double y1, double x2, double y2, double t,
                             double[] left,
                             double[] right) {
        split(x0, y0, x1, y1, x2, y2, t,
                (a, b, c, d) -> {
                    left[0] = a;
                    left[1] = b;
                    left[2] = c;
                    left[3] = d;
                },
                (a, b, c, d) -> {
                    right[0] = a;
                    right[1] = b;
                    right[2] = c;
                    right[3] = d;
                });

    }

    /**
     * Splits the provided bezier curve into two parts.
     *
     * @param x0           point 1 of the curve
     * @param y0           point 1 of the curve
     * @param x1           point 2 of the curve
     * @param y1           point 2 of the curve
     * @param x2           point 3 of the curve
     * @param y2           point 3 of the curve
     * @param t            where to split
     * @param leftCurveTo  if not null, accepts the curve from x1,y1 to t
     * @param rightCurveTo if not null, accepts the curve from t to x3,y3
     */
    public static void split(double x0, double y0, double x1, double y1, double x2, double y2, double t,
                             @Nullable Double4Consumer leftCurveTo,
                             @Nullable Double4Consumer rightCurveTo) {
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);
        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);
        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        if (leftCurveTo != null) {
            leftCurveTo.accept(x01, y01, x012, y012);
        }
        if (rightCurveTo != null) {
            rightCurveTo.accept(x12, y12, x2, y2);
        }
    }

    /**
     * Extracts the specified segment [ta,tb] from the given quad curve.
     *
     * @param x0 point P0 of the curve
     * @param y0 point P0 of the curve
     * @param x1 point P1 of the curve
     * @param y1 point P1 of the curve
     * @param x2 point P2 of the curve
     * @param y2 point P2 of the curve
     * @param ta where to split
     * @param tb where to split
     */
    public static double[] subCurve(double x0, double y0,
                                    double x1, double y1,
                                    double x2, double y2,
                                    double ta, double tb) {
        double[] left = new double[4];
        double[] right = new double[4];
        double tab = ta / tb;
        split(x0, y0,
                x1, y1, x2, y2, tb, left, null);
        split(x0, y0,
                left[0], left[1], left[2], left[3], tab, left, right);
        return new double[]{left[2], left[3],
                right[0], right[1], right[2], right[3]};
    }


    /**
     * Calculates the arc-length of a quadratic bézier curve using a closed form solution.
     * <p>
     * References:
     * <dl>
     *     <dt>Stackoverflow, Calculate the length of a segment of a quadratic bezier,
     *     Copyright Michael Anderson, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/11854907/calculate-the-length-of-a-segment-of-a-quadratic-bezier">
     *        stackoverflow.com </a></dd>
     * </dl>
     *
     * @param b      the coordinates of the control points of the bézier curve
     * @param offset the offset of the first control point in {@code b}
     */
    public static double arcLength(double[] b, int offset) {
        return arcLength(b, offset, 1.0);
    }

    /**
     * Calculates the arc-length {@code s} of a segment from [0, t] of a
     * quadratic bézier curve using a closed form solution.
     * <p>
     * Length of any parametric (in general length of any well defined curve) curve can be computated
     * using curve length integral. In case of 2nd order Bezier curve, using its derivatives,
     * this integral can be written as Quadratic Bezier Curve integral
     * <pre>
     * s = integrate ( √( Bx'(t)² + By′(t)² ) ,  d:t, from:0, to:t)
     * </pre>
     * To simplify this integral we can make some substitutions. In this case it we will look like this
     * <pre>
     * a = P₀ − 2*P₁ + P₂
     * b = 2*P₁ − 2*P₀
     * ∂B∂t(t) = 2*t*a + b
     * </pre>
     * Next after doing some algebra and grouping elements in order to parameter t we will do another
     * substitutions (to make this integral easier):
     * <pre>
     * A = 4*(ax² + ay²)
     * B = 4*(ax*bx + ay*by)
     * C = bx² + by²
     * </pre>
     * <p>
     * The arc-length {@code s} can now be computed with the following equation:
     * <pre>
     *     s = integrate( √( A*t² + B*t +C ), d:t, from:0, to:t)
     * </pre>
     * Which we can write as:
     * <pre>
     *     s = √(A) * integrate( √( t² + 2*b*t +c ), d:t, from:0, to:t)
     *     where b = B/(2*A)
     *           c = C/A
     * </pre>
     * Then we get:
     * <pre>
     *     s = √(A) * integrate( √(u²+k), d:t, from:b, to:u)
     *     where u = t + b
     *           k = c - b²
     * </pre>
     * Now we can use the integral identity from the link to obtain:
     * <pre>
     *     s = √(A)/2 * ( u * √(u²+k) - b * √(b²+k) + k * log( (u+√(u²+k))/ (b+√(b²+k)) ) )
     * </pre>
     * <p>
     * References:
     * <dl>
     *     <dt>Stackoverflow. Calculate the length of a segment of a quadratic bezier.
     *     Copyright Michael Anderson. CC BY-SA 4.0 license.</dt>
     *     <dd><a href="https://stackoverflow.com/questions/11854907/calculate-the-length-of-a-segment-of-a-quadratic-bezier">
     *        stackoverflow.com</a></dd>
     *
     *     <dt>Quadratic Bezier curves, Copyright malczak</dt>
     *     <dd><a href="https://malczak.info/blog/quadratic-bezier-curve-length">malczak.info</a></dd>
     * </dl>
     *
     * @param q      the coordinates of the control points of the bézier curve
     * @param t      the value of t in range [0,1]
     * @param offset the offset of the first control point in {@code b}
     */
    public static double arcLength(double[] q, int offset, double t) {
        double x0 = q[offset],
                y0 = q[offset + 1],
                x1 = q[offset + 2],
                y1 = q[offset + 3],
                x2 = q[offset + 4],
                y2 = q[offset + 5];
        double ax, ay, bx, by, A, B, C, b, c, u, k, s;
        ax = x0 - x1 - x1 + x2;
        ay = y0 - y1 - y1 + y2;
        bx = x1 + x1 - x0 - x0;
        by = y1 + y1 - y0 - y0;
        A = 4.0 * (ax * ax + ay * ay);
        B = 4.0 * (ax * bx + ay * by);
        C = (bx * bx) + (by * by);
        b = B / (2.0 * A);
        c = C / A;
        u = t + b;
        k = c - (b * b);
        s = ((u * sqrt((u * u) + k))
                - (b * sqrt((b * b) + k))
                + (k * log(abs((u + sqrt((u * u) + k)) / (b + sqrt((b * b) + k)))))
        ) * 0.5 * sqrt(A);
        return s;
    }

    /**
     * Calculates the time {@code t} at a given arc-length {@code s} of a
     * quadratic bézier curve using a closed form solution.
     *
     * @param p      the coordinates of the control points of the bézier curve
     * @param s      the arc-length value where {@literal s >= 0}
     * @param offset the offset of the first control point in {@code b}
     */
    public static double invArcLength(double[] p, int offset, double s) {
        ToDoubleFunction<Double> f = t -> arcLength(p, offset, t);
        ToDoubleFunction<Double> fd = getArcLengthIntegrand(p, offset);
        return Solvers.hybridNewtonBisectionMethod(f, fd, s, 0, 1, s / arcLength(p, offset, 1));
    }

    /**
     * Gets the integrand function for the arc-length of a quadratic bézier curve.
     * <p>
     * The arc-length {@code s} can be computed with the following equation:
     * <pre>
     *     s = integrate( √( A*t² + B*t +C ), d:t, from:0, to:t)
     * </pre>
     * The integrand function is therefore:
     * <pre>
     *     s = √( A*t² + B*t +C )
     * </pre>
     * <p>
     * References:
     * <dl>
     *     <dt>Calculate the length of a segment of a quadratic bezier, Copyright Michael Anderson, CC BY-SA 4.0 license </dt>
     *     <dd><a href="https://stackoverflow.com/questions/11854907/calculate-the-length-of-a-segment-of-a-quadratic-bezier">
     *        stackoverflow.com</a></dd>
     * </dl>
     *
     * @param p      the coordinates of the control points of the bézier curve
     * @param offset the offset of the first control point in {@code p}
     */
    public static ToDoubleFunction<Double> getArcLengthIntegrand(double[] p, int offset) {
        double x0 = p[offset],
                y0 = p[offset + 1],
                x1 = p[offset + 2],
                y1 = p[offset + 3],
                x2 = p[offset + 4],
                y2 = p[offset + 5];
        double ax, ay, bx, by, A, B, C;
        ax = x0 - x1 - x1 + x2;
        ay = y0 - y1 - y1 + y2;
        bx = x1 + x1 - x0 - x0;
        by = y1 + y1 - y0 - y0;
        A = 4.0 * (ax * ax + ay * ay);
        B = 4.0 * (ax * bx + ay * by);
        C = (bx * bx) + (by * by);

        // Derivative:
        return (t) -> sqrt(A * t * t + B * t + C);
    }
}
