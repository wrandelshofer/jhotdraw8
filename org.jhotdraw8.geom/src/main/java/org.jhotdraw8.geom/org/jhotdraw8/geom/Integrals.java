/*
 * @(#)Integrals.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.function.ToFloatFunction;

import java.util.function.ToDoubleFunction;

import static java.lang.Math.abs;

/**
 * Provides algorithms for computing the integral of a function.
 */
public class Integrals {
    /**
     * Don't let anyone instantiate this class.
     */
    private Integrals() {
    }

    /**
     * Romberg Quadrature.
     * <p>
     * References:
     * <dl>
     *     <dt>waruyama</dt>
     *     <dd><a href="https://github.com/Pomax/BezierInfo-2/issues/77">github.com</a></dd>
     *     <dt>Wikipedia. Romberg's method.</dt>
     *     <dd><a href="https://en.wikipedia.org/wiki/Romberg%27s_method">wikipedia.org</a></dd>
     * </dl>
     *
     * @param f  the function
     * @param t0 the lower bound of the integral
     * @param t1 the upper bound of the integral
     * @return the estimated integral
     */
    public static double rombergQuadrature(ToDoubleFunction<Double> f, double t0, double t1) {
        return rombergQuadrature(f, t0, t1, 0.1);
    }

    public static float rombergQuadratureFloat(ToFloatFunction<Float> f, float t0, float t1) {
        return rombergQuadratureFloat(f, t0, t1, 0.1f);
    }

    /**
     * Romberg Quadrature.
     * <p>
     * References:
     * <dl>
     *     <dt>waruyama</dt>
     *     <dd><a href="https://github.com/Pomax/BezierInfo-2/issues/77">github.com</a></dd>
     *     <dt>Wikipedia. Romberg's method.</dt>
     *     <dd><a href="https://en.wikipedia.org/wiki/Romberg%27s_method">wikipedia.org</a></dd>
     * </dl>
     *
     * @param f       the function
     * @param t0      the lower bound of the integral
     * @param t1      the upper bound of the integral
     * @param epsilon the desired precision
     * @return the estimated integral
     */
    public static double rombergQuadrature(ToDoubleFunction<Double> f, double t0, double t1, double epsilon) {
        int maxSteps = 5;
        double h = t1 - t0;

        double[] Rp = new double[maxSteps];
        double[] Rc = new double[maxSteps];
        Rp[0] = (f.applyAsDouble(t0) + f.applyAsDouble(t1)) * h * 0.5;

        for (int i = 1; i < maxSteps; i++) {
            h *= 0.5;
            double c = 0;
            int ep = 1 << (i - 1);
            for (int j = 1; j <= ep; j++) {
                c += f.applyAsDouble(t0 + (2 * j - 1) * h);
            }
            Rc[0] = h * c + 0.5 * Rp[0]; //R(i,0)
            double n_k = 1;
            for (int j = 1; j <= i; j++) {
                n_k *= 4;
                Rc[j] = (n_k * Rc[j - 1] - Rp[j - 1]) / (n_k - 1); // compute R(i,j)
            }
            if (i > 1 && Math.abs(Rp[i - 1] - Rc[i]) < epsilon) {
                return Rc[i - 1];
            }
            double[] tmp = Rp;
            Rp = Rc;
            Rc = tmp;
        }
        return Rp[maxSteps - 1];
    }

    public static float rombergQuadratureFloat(ToFloatFunction<Float> f, float t0, float t1, float epsilon) {
        int maxSteps = 5;
        float h = t1 - t0;

        float[] Rp = new float[maxSteps];
        float[] Rc = new float[maxSteps];
        Rp[0] = (f.applyAsFloat(t0) + f.applyAsFloat(t1)) * h * 0.5f;

        for (int i = 1; i < maxSteps; i++) {
            h *= 0.5f;
            float c = 0;
            int ep = 1 << (i - 1);
            for (int j = 1; j <= ep; j++) {
                c += f.applyAsFloat(t0 + (2 * j - 1) * h);
            }
            Rc[0] = h * c + 0.5f * Rp[0]; //R(i,0)
            float n_k = 1;
            for (int j = 1; j <= i; j++) {
                n_k *= 4;
                Rc[j] = (n_k * Rc[j - 1] - Rp[j - 1]) / (n_k - 1); // compute R(i,j)
            }
            if (i > 1 && Math.abs(Rp[i - 1] - Rc[i]) < epsilon) {
                return Rc[i - 1];
            }
            float[] tmp = Rp;
            Rp = Rc;
            Rc = tmp;
        }
        return Rp[maxSteps - 1];
    }

    /**
     * Estimates the integral of the given function in the given interval using
     * Simpsons's rule.
     * <p>
     * simpson Based on trapzd in "Numerical Recipes in C", page 139
     *
     * @param func the function
     * @param min  the lower bound of the interval
     * @param max  the upper bound of the interval
     * @return the area under the curve
     */
    public static double simpson(@NonNull ToDoubleFunction<Double> func, double min, double max, double eps) {
        int maxSteps = 20;

        double range = max - min;
        double st = 0.5 * range * (func.applyAsDouble(min) + func.applyAsDouble(max));
        double t = st;
        double s = 4.0 * st / 3.0;
        double os = s;
        double ost = st;

        int it = 1;
        for (int n = 2; n <= maxSteps; n++) {
            double delta = range / it;
            double x = min + 0.5 * delta;
            double sum = 0;

            for (double i = 1; i <= it; i++) {
                sum += func.applyAsDouble(x);
                x += delta;
            }

            t = 0.5 * (t + range * sum / it);
            st = t;
            s = (4.0 * st - ost) / 3.0;

            if (abs(s - os) < eps * abs(os)) {
                break;
            }

            os = s;
            ost = st;
            it <<= 1;
        }

        return s;
    }

    /**
     * Gauss-Legendre quadrature integration from a to b with n=3.
     * <p>
     * See <a href="https://pomax.github.io/bezierinfo/legendre-gauss.html">pomax</a>
     * for more values.
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     *
     * @param func the function
     * @param a    the lower bound of the interval
     * @param b    the upper bound of the interval
     * @return the area under the curve
     */

    public static double gaussLegendre3(@NonNull ToDoubleFunction<Double> func, double a, double b) {
        double c = (b - a) * 0.5;
        double d = (a + b) * 0.5;
        double Qd1 = func.applyAsDouble(-0.774596669 * c + d);
        double Qd2 = func.applyAsDouble(d);
        double Qd3 = func.applyAsDouble(0.774596669 * c + d);
        return c * ((5.0 / 9.0) * (Qd1 + Qd3) + (8.0 / 9.0) * Qd2);
    }

    /**
     * Gauss-Legendre quadrature integration from a to b with n=5.
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     *
     * @param func the function
     * @param a    the lower bound of the interval
     * @param b    the upper bound of the interval
     * @return the area under the curve
     */
    public static double gaussLegendre5(@NonNull ToDoubleFunction<Double> func, double a, double b) {
        double c = (b - a) * 0.5;
        double d = (a + b) * 0.5;
        double Qd1 = func.applyAsDouble(-0.90618 * c + d);
        double Qd2 = func.applyAsDouble(-0.538469 * c + d);
        double Qd3 = func.applyAsDouble(d);
        double Qd4 = func.applyAsDouble(0.538469 * c + d);
        double Qd5 = func.applyAsDouble(0.90618 * c + d);
        return c * (0.236927 * (Qd1 + Qd5) + 0.478629 * (Qd2 + Qd4) + 0.568889 * Qd3);
    }

    /**
     * Gauss-Legendre quadrature integration from a to b with n=7.
     * <p>
     * References:
     * <dl>
     *     <dt>Canvas. Copyright (c) 2015 Taco de Wolff, MIT License.</dt>
     *     <dd><a href="https://github.com/tdewolff/canvas/blob/master/util.go#L609">github.com</a></dd>
     * </dl>
     *
     * @param func the function
     * @param a    the lower bound of the interval
     * @param b    the upper bound of the interval
     * @return the area under the curve
     */
    public static double gaussLegendre7(@NonNull ToDoubleFunction<Double> func, double a, double b) {
        double c = (b - a) * 0.5;
        double d = (a + b) * 0.5;
        double Qd1 = func.applyAsDouble(-0.949108 * c + d);
        double Qd2 = func.applyAsDouble(-0.741531 * c + d);
        double Qd3 = func.applyAsDouble(-0.405845 * c + d);
        double Qd4 = func.applyAsDouble(d);
        double Qd5 = func.applyAsDouble(0.405845 * c + d);
        double Qd6 = func.applyAsDouble(0.741531 * c + d);
        double Qd7 = func.applyAsDouble(0.949108 * c + d);
        return c * (0.129485 * (Qd1 + Qd7) + 0.279705 * (Qd2 + Qd6) + 0.381830 * (Qd3 + Qd5) + 0.417959 * Qd4);
    }

}
