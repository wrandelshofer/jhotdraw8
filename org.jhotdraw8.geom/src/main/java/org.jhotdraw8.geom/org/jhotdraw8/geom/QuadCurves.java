/*
 * @(#)QuadCurves.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.base.function.DoubleConsumer4;
import org.jhotdraw8.geom.intersect.IntersectRayRay;
import org.jhotdraw8.geom.intersect.IntersectionResultEx;
import org.jspecify.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import static org.jhotdraw8.geom.Lines.lerp;

/**
 * Provides utility methods for quadratic Bézier curves.
 * <p>
 * Quadratic Bezier curves are defined by second order polynomial, and can be written as:
 * <pre>
 * B(t) = (1 − t)*P₀ + 2*(1 − t)*t*P₁ + t²*P₂
 * </pre>
 * where t is real parameter with values in range [0,1]. P’s are respectively curve starting point,
 * anchor point and the end point. Derivative of the quadratic Bézier curve can be written as
 * Quadratic Bézier Curve derivative:
 * <pre>
 * ∂B∂t(t) = 2*t*(P₀ − 2*P₁ + P₂) + 2*P₁ − 2*P₀
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>Quadratic Bezier curves, Copyright malczak</dt>
 *     <dd><a href="https://malczak.info/blog/quadratic-bezier-curve-length">malczak.info</a></dd>
 * </dl>
 *
 */
public class QuadCurves {

    /**
     * Don't let anyone instantiate this class.
     */
    private QuadCurves() {
    }


    /**
     * Computes the arc length s.
     *
     * @param p       points of the curve
     * @param offset  index of the first point in array {@code p}
     * @param epsilon the error tolerance
     * @return the arc length
     */
    public static double arcLength(double[] p, int offset, double epsilon) {
        return arcLength(p, offset, 1, epsilon);
    }


    /**
     * Computes the arc length s from time 0 to time t
     * using an integration method.
     *
     * @param p       the coordinates of the control points of the bézier curve
     * @param offset  the offset of the first control point in {@code b}
     * @param t       the time value
     * @param epsilon the error tolerance
     * @return the arc length
     */
    public static double arcLength(double[] p, int offset, double t, double epsilon) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(p, offset);
        return Integrals.rombergQuadrature(f, 0, t, epsilon);
    }

    /**
     * Evaluates the given curve at the specified time.
     *
     * @param p0 point P0 of the curve
     * @param p1 point P1 of the curve
     * @param p2 point P2 of the curve
     * @param t  the time
     * @return the point at time t
     */
    public static PointAndDerivative eval(Point2D p0, Point2D p1, Point2D p2, double t) {
        return eval(p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY(), t);
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
    public static PointAndDerivative eval(double x0, double y0, double x1, double y1, double x2, double y2, double t) {
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);

        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);

        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        return new PointAndDerivative(x012, y012, x12 - x01, y12 - y01);
    }

    public static PointAndDerivative eval(double[] p, int offset, double t) {
        double x0 = p[offset], y0 = p[offset + 1], x1 = p[offset + 2], y1 = p[offset + 3], x2 = p[offset + 4], y2 = p[offset + 5];
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);

        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);

        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        return new PointAndDerivative(x012, y012, x12 - x01, y12 - y01);
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
        // Instead of the code below, we could evaluate the magnitude of the derivative
        /*
        return (t)-> {
            PointAndDerivative p = eval(v, offset, t);
            //return Math.hypot(p.dx(),p.dy());
            return Math.sqrt(p.dx()*p.dx()+p.dy()*p.dy());
        };
        */

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
        return (t) -> sqrt(Math.fma(A, t * t, Math.fma(B, t, C)));
    }


    /**
     * Calculates the time {@code t} at a given arc-length {@code s} of a
     * quadratic bézier curve using an integration method.
     *
     * @param p       the coordinates of the control points of the bézier curve
     * @param offset  the offset of the first control point in {@code b}
     * @param s       the arc-length value where {@literal s >= 0}
     * @param epsilon
     */
    public static double invArcLength(double[] p, int offset, double s, double epsilon) {
        return invArcLength(p, offset, s, arcLength(p, offset, 1, epsilon), epsilon);
    }

    public static double invArcLength(double[] p, int offset, double s, double totalArcLength, double epsilon) {
        ToDoubleFunction<Double> f = t -> arcLength(p, offset, t, epsilon);
        ToDoubleFunction<Double> fd = getArcLengthIntegrand(p, offset);
        return Solvers.hybridNewtonBisectionMethod(f, fd, s, 0, 1, s / totalArcLength, epsilon);
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
    public static double @Nullable [] merge(final double x0, final double y0, final double x01, final double y01,
                                            final double x012, final double y012, final double x12, final double y12, final double x2, final double y2,
                                            double tolerance) {
        final Point2D.Double start = new Point2D.Double(x0, y0);
        Point2D b0 = new Point2D.Double(x2, y2);

        final IntersectionResultEx isect = IntersectRayRay.intersectRayRayEx(start, Points2D.subtract(new Point2D.Double(x01, y01), start),
                b0, Points2D.subtract(new Point2D.Double(x12, y12), b0));
        if (isect.intersections().isEmpty()) {
            return null;
        }
        final Point2D.Double ctrl = isect.intersections().getLast();

        final double t = start.distance(x01, y01) / start.distance(ctrl);
        final Point2D.Double joint01 = eval(x0, y0, ctrl.getX(), ctrl.getY(), x2, y2, t).getPoint(Point2D.Double::new);

        return (joint01.distance(x012, y012) <= tolerance) ?
                new double[]{x0, y0, ctrl.getX(), ctrl.getY(), x2, y2} : null;
    }

    public static void split(double x0, double y0, double x1, double y1, double x2, double y2, double t,
                             double[] first,
                             double[] second) {
        split(x0, y0, x1, y1, x2, y2, t,
                (a, b, c, d) -> {
                    first[0] = a;
                    first[1] = b;
                    first[2] = c;
                    first[3] = d;
                },
                (a, b, c, d) -> {
                    second[0] = a;
                    second[1] = b;
                    second[2] = c;
                    second[3] = d;
                });

    }

    /**
     * Splits the provided Bézier curve into two parts.
     *
     * @param x0     point 1 of the curve
     * @param y0     point 1 of the curve
     * @param x1     point 2 of the curve
     * @param y1     point 2 of the curve
     * @param x2     point 3 of the curve
     * @param y2     point 3 of the curve
     * @param t      where to split
     * @param first  if not null, accepts the curve from x1,y1 to t
     * @param second if not null, accepts the curve from t to x3,y3
     */
    public static void split(double x0, double y0, double x1, double y1, double x2, double y2,
                             double t,
                             @Nullable DoubleConsumer4 first,
                             @Nullable DoubleConsumer4 second) {
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);
        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);
        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        if (first != null) {
            first.accept(x01, y01, x012, y012);
        }
        if (second != null) {
            second.accept(x12, y12, x2, y2);
        }
    }

    /**
     * Splits the provided Bézier curve into two parts.
     *
     * @param t      where to split
     * @param first  if not null, accepts the curve from x1,y1 to t
     * @param second if not null, accepts the curve from t to x3,y3
     */
    public static void split(double[] p, int o, double t,
                             double @Nullable [] first, int offsetFirst,
                             double @Nullable [] second, int offsetSecond) {
        double x0 = p[o], y0 = p[o + 1], x1 = p[o + 2], y1 = p[o + 3], x2 = p[o + 4], y2 = p[o + 5];
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);
        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);
        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        if (first != null) {
            first[offsetFirst] = x0;
            first[offsetFirst + 1] = y0;
            first[offsetFirst + 2] = x01;
            first[offsetFirst + 3] = y01;
            first[offsetFirst + 4] = x012;
            first[offsetFirst + 5] = y012;
        }
        if (second != null) {
            second[offsetSecond] = x012;
            second[offsetSecond + 1] = y012;
            second[offsetSecond + 2] = x12;
            second[offsetSecond + 3] = y12;
            second[offsetSecond + 4] = x2;
            second[offsetSecond + 5] = y2;
        }
    }

    /**
     * Extracts the specified segment [ta,tb] from the given quad curve.
     *
     * @param ta where to split
     * @param tb where to split
     */
    public static void subCurve(double[] q, int offset,
                                double ta, double tb,
                                double[] first, int offsetFirst) {
        double tab = ta / tb;
        split(q, offset,
                tb, null, 0, first, offsetFirst);
        split(first, offsetFirst, tab,
                null, 0, first, offsetFirst);
    }
}
