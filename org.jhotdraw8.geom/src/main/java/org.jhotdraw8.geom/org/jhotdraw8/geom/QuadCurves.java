/*
 * @(#)QuadCurves.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.DoubleConsumer4;
import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.geom.intersect.IntersectRayRay;
import org.jhotdraw8.geom.intersect.IntersectionResultEx;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;
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
 * <p>
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
     * @param p0 point P0 of the curve
     * @param p1 point P1 of the curve
     * @param p2 point P2 of the curve
     * @param t  the time
     * @return the point at time t
     */
    public static @NonNull PointAndDerivative eval(Point2D p0, Point2D p1, Point2D p2, double t) {
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
    public static @NonNull PointAndDerivative eval(double x0, double y0, double x1, double y1, double x2, double y2, double t) {
        final double x01, y01, x12, y12, x012, y012;
        x01 = lerp(x0, x1, t);
        y01 = lerp(y0, y1, t);

        x12 = lerp(x1, x2, t);
        y12 = lerp(y1, y2, t);

        x012 = lerp(x01, x12, t);
        y012 = lerp(y01, y12, t);

        return new PointAndDerivative(x012, y012, x12 - x01, y12 - y01);
    }

    public static @NonNull PointAndDerivative eval(double[] p, int offset, double t) {
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
        @NonNull Point2D b0 = new Point2D.Double(x2, y2);

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
    public static void split(double @NonNull [] p, int o, double t,
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
            first[offsetFirst] = x012;
            first[offsetFirst + 1] = y012;
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
    public static void subCurve(double @NonNull [] q, int offset,
                                double ta, double tb,
                                double @NonNull [] first, int offsetFirst) {
        double tab = ta / tb;
        split(q, offset,
                tb, null, 0, first, offsetFirst);
        split(first, offsetFirst, tab,
                null, 0, first, offsetFirst);
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
    public static double arcLength(double @NonNull [] b, int offset) {
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
     * The arc-length {@code L} can now be computed with the following equation:
     * <pre>
     *     L = integrate( √( A*t² + B*t +C ), d:t, from:0, to:t)
     * </pre>
     * Which we can write as:
     * <pre>
     *     L = √(A) * integrate( √( t² + 2*b*t +c ), d:t, from:0, to:t)
     *     where b = B/(2*A)
     *           c = C/A
     * </pre>
     * Then we get:
     * <pre>
     *     L = √(A) * integrate( √(u²+k), d:t, from:b, to:u)
     *     where u = t + b
     *           k = c - b²
     * </pre>
     * Now we can use the integral identity from the link to obtain:
     * <pre>
     *     L = √(A)/2 * (
     *                   u*√(u²+k) - b*√(b²+k)
     *                   + k*log( (u+√(u²+k)) / (b+√(b²+k)) )
     *                   )
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
     * @param offset the offset of the first control point in {@code q}
     * @param t      the value of t in range [0,1]
     * @return the arc length, a non-negative value
     */
    public static double arcLength(double[] q, int offset, double t) {
        double x0 = q[offset],
                y0 = q[offset + 1],
                x1 = q[offset + 2],
                y1 = q[offset + 3],
                x2 = q[offset + 4],
                y2 = q[offset + 5];
        double ax, ay, bx, by, A, B, C, b, c, u, k, E, F;
        ax = x0 - x1 - x1 + x2;
        ay = y0 - y1 - y1 + y2;
        bx = x1 + x1 - x0 - x0;
        by = y1 + y1 - y0 - y0;
        A = 4.0 * (ax * ax + ay * ay);
        B = 4.0 * (ax * bx + ay * by);
        C = bx * bx + by * by;
        b = B / (2.0 * A);
        c = C / A;
        u = t + b;
        k = c - (b * b);
        E = sqrt((u * u) + k);
        F = sqrt((b * b) + k);

        double arcLength = 0.5 * sqrt(A)
                * (u * E - b * F + (k * log(abs((u + E) / (b + F)))));

        if (arcLength < 0 || Double.isNaN(arcLength)) {
            // the arc is degenerated to a line
            return Lines.arcLength(q[offset], q[offset + 1], q[offset + 4], q[offset + 5]);
        }

        return arcLength;

    }

    /**
     * Calculates the time {@code t} at a given arc-length {@code s} of a
     * quadratic bézier curve using a closed form solution.
     *
     * @param p       the coordinates of the control points of the bézier curve
     * @param offset  the offset of the first control point in {@code b}
     * @param s       the arc-length value where {@literal s >= 0}
     * @param epsilon
     */
    public static double invArcLength(double[] p, int offset, double s, double epsilon) {
        ToDoubleFunction<Double> f = t -> arcLength(p, offset, t);
        ToDoubleFunction<Double> fd = getArcLengthIntegrand(p, offset);
        return Solvers.hybridNewtonBisectionMethod(f, fd, s, 0, 1, s / arcLength(p, offset, 1), epsilon);
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
            return Math.hypot(p.dx(),p.dy());
            //return Math.sqrt(p.dx()*p.dx()+p.dy()*p.dy());
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
        return (t) -> sqrt(A * t * t + B * t + C);
    }

    /**
     * Approximates a cubic curve with up to 16 quadratic curves.
     * <p>
     * References:
     * <dl>
     *     <dt>Proc. ACM Comput. Graph. Interact. Tech., Vol. 3, No. 2, Article 16. Publication date: August 2020.
     *     Quadratic Approximation of Cubic Curves.
     *     NGHIA TRUONG, University of Utah, CEM YUKSEL, University of Utah, LARRY SEILER, Facebook Reality Labs.
     *     Copyright 2020 held by the owner/author(s). Publication rights licensed to ACM.
     *     </dt>
     *     <dd><a href="https://ttnghia.github.io/pdf/QuadraticApproximation.pdf">ttnghia.github.io</a>
     *     </dd>
     * </dl>
     *
     * @param p       the points of the cubic curve
     * @param offsetP the index of the first point in p
     * @param q       the points of the quadratic curves (on output)
     * @param offsetQ the index of the first point in q
     * @return the number of quadratic curves
     */
    public static int approximateCubicCurve(double[] p, int offsetP, double[] q, int offsetQ, double tolerance) {
        return approximateCubicCurve(p, offsetP, q, offsetQ, tolerance, 3);
    }

    private static int approximateCubicCurve(double[] p, int offsetP, double[] q, int offsetQ, double tolerance, int maxDepth) {
        double errorSquared = maxDepth == 0 ? 0 : estimateCubicCurveApproximationErrorSquared(p, offsetP);
        if (errorSquared > tolerance * tolerance) {
            // we should split

            DoubleArrayList list = CubicCurveCharacteristics.inflectionPoints(p, offsetP);
            Double singularPoint = CubicCurveCharacteristics.singularPoint(p, offsetP);
            if (singularPoint != null) {
                list.add(singularPoint);
                list.sort();
            }
            final double epsilon = 1e-6;
            for (double t : list) {
                if (!Points.almostEqual(t, 0, epsilon) && !Points.almostEqual(t, 1, epsilon)) {
                    return approximateCubicCurveSplitCase(p, offsetP, t, q, offsetQ, tolerance, maxDepth);
                }
            }
            return approximateCubicCurveSplitCase(p, offsetP, 0.5, q, offsetQ, tolerance, maxDepth);
        } else {
            // we only need to split once or not at all
            return approximateCubicCurveBaseCase(p, offsetP, q, offsetQ, tolerance);
        }
    }

    private static int approximateCubicCurveSplitCase(double[] p, int offsetP, double t, double[] q, int offsetQ, double tolerance, int maxDepth) {
        double[] pp = new double[8 * 2];
        CubicCurves.split(p, offsetP, t, pp, 0, pp, 8);
        int count = approximateCubicCurve(pp, 0, q, offsetQ, tolerance, maxDepth - 1);
        return count + approximateCubicCurve(pp, 8, q, offsetQ + count * 6, tolerance, maxDepth - 1);
    }

    private static int approximateCubicCurveBaseCase(double[] p, int offsetP, double[] q, int offsetQ, double tolerance) {
        double x0 = p[offsetP];
        double y0 = p[offsetP + 1];
        double x1 = p[offsetP + 2];
        double y1 = p[offsetP + 3];
        double x2 = p[offsetP + 4];
        double y2 = p[offsetP + 5];
        double x3 = p[offsetP + 6];
        double y3 = p[offsetP + 7];

        // The quadratic curve always starts at the same point as the cubic curve
        int qq = offsetQ;
        q[qq] = x0;
        q[qq + 1] = y0;

        if (CubicCurve2D.getFlatnessSq(p, offsetP) <= tolerance * tolerance) {
            // p1 and p2 coincide or
            // the curve is almost flat
            q[qq + 2] = (x0 + x3) * 0.5;
            q[qq + 3] = (y0 + y3) * 0.5;
            q[qq + 4] = x3;
            q[qq + 5] = y3;
            return 1;
        }
        double gamma = 0.5;
        double x2i = x0 + (3.0 * 0.5 * gamma) * (x1 - x0);
        double y2i = y0 + (3.0 * 0.5 * gamma) * (y1 - y0);
        double x2iplus1 = x3 + (3.0 * 0.5 * (1 - gamma)) * (x2 - x3);
        double y2iplus1 = y3 + (3.0 * 0.5 * (1 - gamma)) * (y2 - y3);
        q[qq + 2] = x2i;
        q[qq + 3] = y2i;
        q[qq + 4] = q[qq + 6] = (1 - gamma) * x2i + gamma * x2iplus1;
        q[qq + 5] = q[qq + 7] = (1 - gamma) * y2i + gamma * y2iplus1;
        q[qq + 8] = x2iplus1;
        q[qq + 9] = y2iplus1;
        q[qq + 10] = x3;
        q[qq + 11] = y3;
        return 2;
    }

    private static double estimateCubicCurveApproximationErrorSquared(double[] p, int offsetP) {
        double x0 = p[offsetP];
        double y0 = p[offsetP + 1];
        double x1 = p[offsetP + 2];
        double y1 = p[offsetP + 3];
        double x2 = p[offsetP + 4];
        double y2 = p[offsetP + 5];
        double x3 = p[offsetP + 6];
        double y3 = p[offsetP + 7];

        double ex = -x0 + 3 * x1 - 3 * x2 + x3;
        double ey = -y0 + 3 * y1 - 3 * y2 + y3;
        return (ex * ex + ey * ey) * (1.0 / (54 * 54));
    }
}
