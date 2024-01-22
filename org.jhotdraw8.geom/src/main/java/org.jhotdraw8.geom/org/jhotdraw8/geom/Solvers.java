/*
 * @(#)Solvers.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;

import java.util.function.ToDoubleFunction;

/**
 * Provides algorithms for computing the inverse of a function.
 */
public class Solvers {
    /**
     * Don't let anyone instantiate this class.
     */
    private Solvers() {
    }


    /**
     * Returns a function y(x) that maps the parameter x [xmin,xmax] to the integral of fp.
     * For a circle tmin and tmax would be 0 and 2PI respectively for example.
     * It also returns the total length of the curve.
     * <p>
     * Implemented using M. Walter, A. Fournier,
     * Approximate Arc Length Parametrization, Anais do IX SIBGRAPHI, p. 143--150, 1996, see
     * <a href="https://www.visgraf.impa.br/sibgrapi96/trabs/pdf/a14.pdf">visgraf.impa.br</a>.
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     */
    public static @NonNull OrderedPair<ToDoubleFunction<Double>, Double> polynomialApprox3(@NonNull Function3<ToDoubleFunction<Double>, Double, Double, Double> quadratureFunction,
                                                                                           @NonNull ToDoubleFunction<Double> fp,
                                                                                           double xmin, double xmax) {
        double y1 = quadratureFunction.apply(fp, xmin, xmin + (xmax - xmin) * 1.0 / 3.0);
        double y2 = quadratureFunction.apply(fp, xmin, xmin + (xmax - xmin) * 2.0 / 3.0);
        double y3 = quadratureFunction.apply(fp, xmin, xmax);

        // We have four points on the y(x) curve at x0=0, x1=1/3, x2=2/3 and x3=1
        // now obtain a polynomial that goes through these four points by solving the system of linear equations
        // y(x) = a*x^3 + b*x^2 + c*x + d  (NB: y0 = d = 0)
        // [y1; y2; y3] = [1/27, 1/9, 1/3;
        //                 8/27, 4/9, 2/3;
        //                    1,   1,   1] * [a; b; c]
        //
        // After inverting:
        // [a; b; c] = 0.5 * [ 27, -27,  9;
        //                    -45,  36, -9;
        //                     18,  -9,  2] * [y1; y2; y3]
        // NB: y0 = d = 0

        double a = 13.5 * y1 - 13.5 * y2 + 4.5 * y3;
        double b = -22.5 * y1 + 18.0 * y2 - 4.5 * y3;
        double c = 9.0 * y1 - 4.5 * y2 + y3;
        return new SimpleOrderedPair<>(
                (x) -> {
                    x = (x - xmin) / (xmax - xmin);
                    return a * x * x * x + b * x * x + c * x;
                },
                Math.abs(y3));//  total length
    }

    /**
     * invPolynomialApprox does the opposite of {@link #polynomialApprox3}, it returns a function x(y) that maps the
     * parameter y [f(xmin),f(xmax)] to x [xmin,xmax].
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     */
    public static @NonNull OrderedPair<ToDoubleFunction<Double>, Double> invPolynomialApprox3(
            @NonNull Function3<ToDoubleFunction<Double>, Double, Double, Double> quadratureFunction,
            @NonNull ToDoubleFunction<Double> fp,
            double xmin, double xmax) {
        ToDoubleFunction<Double> f = (t) -> Math.abs(quadratureFunction.apply(fp, xmin, xmin + (xmax - xmin) * t));
        double f3 = f.applyAsDouble(1.0);
        double t1 = bisectionMethod(f, (1.0 / 3.0) * f3, 0.0, 1.0, 1e-7);
        double t2 = bisectionMethod(f, (2.0 / 3.0) * f3, 0.0, 1.0, 1e-7);
        double t3 = 1.0;

        // We have four points on the x(y) curve at y0=0, y1=1/3, y2=2/3 and y3=1
        // now obtain a polynomial that goes through these four points by solving the system of linear equations
        // x(y) = a*y^3 + b*y^2 + c*y + d  (NB: x0 = d = 0)
        // [x1; x2; x3] = [1/27, 1/9, 1/3;
        //                 8/27, 4/9, 2/3;
        //                    1,   1,   1] * [a*y3^3; b*y3^2; c*y3]
        //
        // After inverting:
        // [a*y3^3; b*y3^2; c*y3] = 0.5 * [ 27, -27,  9;
        //                                 -45,  36, -9;
        //                                  18,  -9,  2] * [x1; x2; x3]
        // NB: x0 = d = 0

        double a = (27.0 * t1 - 27.0 * t2 + 9.0 * t3) / (2.0 * f3 * f3 * f3);
        double b = (-45.0 * t1 + 36.0 * t2 - 9.0 * t3) / (2.0 * f3 * f3);
        double c = (18.0 * t1 - 9.0 * t2 + 2.0 * t3) / (2.0 * f3);
        return new SimpleOrderedPair<>(
                (g) -> {
                    double t = a * g * g * g + b * g * g + c * g;
                    return xmin + (xmax - xmin) * t;
                },
                f3);
    }

    /**
     * Find value x for which f(x) = y in the interval x in [xmin, xmax] using the bisection method.
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     *
     * @param f         a function that grows monotonically
     * @param y         the desired y value
     * @param xmin      the start of the interval
     * @param xmax      the end of the interval
     * @param tolerance
     * @return x the estimated x value
     */
    public static double bisectionMethod(@NonNull ToDoubleFunction<Double> f, double y, double xmin, double xmax, double tolerance) {
        final int maxIterations = 100;

        int n = 0;
        double toleranceX = Math.abs(xmax - xmin) * tolerance;
        double toleranceY = tolerance;//Math.abs(f.applyAsDouble(xmax) - f.applyAsDouble(xmin)) * tolerance;

        double x;
        while (true) {
            x = (xmin + xmax) * 0.5;
            if (n >= maxIterations) {
                return x;
            }

            double dy = f.applyAsDouble(x) - y;
            if (Math.abs(dy) < toleranceY || Math.abs(xmax - xmin) * 0.5 < toleranceX) {
                return x;
            } else if (dy > 0.0) {
                xmax = x;
            } else {
                xmin = x;
            }
            n++;
        }
    }

    /**
     * Find value x for which ∫f(x) = y in the interval x in [xmin, xmax] using a hybrid of Newton's method
     * and the bisection method.
     * <p>
     * We perform iterations using the Newton’s method until the error of the solution becomes acceptable,
     * or the number of iterations performed becomes unacceptably large.
     * <p>
     * There is a potential problem when using only Newton’s method. If the function is said to be convex, the Newton
     * iterations are guaranteed to converge to the root. However, if the function is non-convex, the Newton iterations
     * may converge outside the domain x∈[xmin,xmax]. In such a case, we use the bisection method instead.
     * <p>
     * References:
     * <dl>
     *     <dt>Movement along the curve with constant speed. Copyright (c) 2021 Alexey Karamyshev.</dt>
     *     <dd><a href="https://medium.com/@ommand/movement-along-the-curve-with-constant-speed-4fa383941507">medium.com</a></dd>
     * </dl>
     *
     * @param quadratureFunction the function for computing the integral of f
     * @param f                  a function
     * @param y                  the desired y value
     * @param xmin               the start of the interval
     * @param xmax               the end of the interval
     * @param x0                 the initial approximation
     * @param epsilon the tolerance
     * @return x the estimated x value
     */
    public static double hybridNewtonBisectionMethod(
            @NonNull Function3<ToDoubleFunction<Double>, Double, Double, Double> quadratureFunction,
            @NonNull ToDoubleFunction<Double> f, double y, double xmin, double xmax, double x0, double epsilon) {

        return hybridNewtonBisectionMethod(x -> quadratureFunction.apply(f, 0.0, x), f, y, xmin, xmax, x0, epsilon);
    }


    /**
     * Find value x for which f(x) = y in the interval x in [xmin, xmax] using a hybrid of Newton's method
     * and the bisection method.
     * <p>
     * We perform iterations using the Newton’s method until the error of the solution becomes acceptable,
     * or the number of iterations performed becomes unacceptably large.
     * <p>
     * There is a potential problem when using only Newton’s method. If the function is said to be convex, the Newton
     * iterations are guaranteed to converge to the root. However, if the function is non-convex, the Newton iterations
     * may converge outside the domain x∈[xmin,xmax]. In such a case, we use the bisection method instead.
     * <p>
     * References:
     * <dl>
     *     <dt>Movement along the curve with constant speed. Copyright (c) 2021 Alexey Karamyshev.</dt>
     *     <dd><a href="https://medium.com/@ommand/movement-along-the-curve-with-constant-speed-4fa383941507">medium.com</a></dd>
     * </dl>
     *
     * @param f       a function
     * @param df      the derivative of the function
     * @param y       the desired y value
     * @param xmin    the start of the interval
     * @param xmax    the end of the interval
     * @param x0      the initial approximation
     * @param epsilon the tolerance
     * @return x the estimated x value
     */
    public static double hybridNewtonBisectionMethod(
            @NonNull ToDoubleFunction<Double> f,
            @NonNull ToDoubleFunction<Double> df, double y, double xmin, double xmax, double x0, double epsilon) {
        final int maxIterations = 100;

        double x = x0;
        double lowerBound = xmin;
        double upperBound = xmax;

        for (int i = 0; i < maxIterations; ++i) {
            double dy = f.applyAsDouble(x) - y;

            if (Math.abs(dy) < epsilon)
                break;

            double derivative = df.applyAsDouble(x);
            double candidateX = x - dy / derivative;

            if (dy > 0) {
                upperBound = x;
                if (candidateX <= xmin) {
                    x = (upperBound + lowerBound) / 2;
                } else {
                    x = candidateX;
                }
            } else {
                lowerBound = x;
                if (candidateX >= xmax) {
                    x = (upperBound + lowerBound) / 2;
                } else {
                    x = candidateX;
                }
            }
        }
        return x;
    }

    /**
     * Creates a function that computes time t given an arc length s.
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     *
     * @param N
     * @param quadratureFunction
     * @param fp
     * @param tmin
     * @param tmax
     * @return
     */
    public static @NonNull SimpleOrderedPair<ToDoubleFunction<Double>, Double> invPolynomialChebyshevApprox(
            int N,
            @NonNull Function3<ToDoubleFunction<Double>, Double, Double, Double> quadratureFunction,
            @NonNull ToDoubleFunction<Double> fp,
            double tmin, double tmax) {
        // TODO: find better way to determine N. For Arc 10 seems fine, for some Quads 10 is too low,
        //  for Cube depending on inflection points is maybe not the best indicator
        // TODO: track efficiency, how many times is fp called? Does a look-up table make more sense?
        ToDoubleFunction<Double> fLength = (t) -> Math.abs(quadratureFunction.apply(fp, tmin, t));
        double totalLength = fLength.applyAsDouble(tmax);
        ToDoubleFunction<Double> t = (L) -> bisectionMethod(fLength, L, tmin, tmax, 1e-7);
        return new SimpleOrderedPair<>(polynomialChebyshevApprox(N, t, 0.0, totalLength, tmin, tmax), totalLength);
    }

    /**
     * Creates an approximation of the provided function using a Chebyshev polynomial of degree N.
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     */
    public static @NonNull ToDoubleFunction<Double> polynomialChebyshevApprox(
            int N,
            @NonNull ToDoubleFunction<Double> f,
            double xmin, double xmax, double ymin, double ymax) {
        double[] fs = new double[N];
        for (int k = 0; k < N; k++) {
            double u = Math.cos(Math.PI * ((k + 1) - 0.5) / (double) N);
            fs[k] = f.applyAsDouble(xmin + (xmax - xmin) * (u + 1.0) / 2.0);
        }

        double[] c = new double[N];
        for (int j = 0; j < N; j++) {
            double a = 0.0;
            for (int k = 0; k < N; k++) {
                a += fs[k] * Math.cos(j * Math.PI * ((k + 1) - 0.5) / (double) N);
            }
            c[j] = (2.0 / (double) (N)) * a;
        }

        return (x) -> {
            x = Math.min(xmax, Math.max(xmin, x));
            double u = (x - xmin) / (xmax - xmin) * 2.0 - 1.0;
            double a = 0.0;
            for (int j = 0; j < N; j++) {
                a += c[j] * Math.cos(j * Math.acos(u));
            }
            double y = -0.5 * c[0] + a;
            if (!Double.isNaN(ymin) && !Double.isNaN(ymax)) {
                y = Math.min(ymax, Math.max(ymin, y));
            }
            return y;
        };
    }
}
