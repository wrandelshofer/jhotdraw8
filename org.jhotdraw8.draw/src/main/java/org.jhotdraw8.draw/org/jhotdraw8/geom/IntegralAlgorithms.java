package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;

import java.util.function.ToDoubleFunction;

import static java.lang.Math.abs;

/**
 * Provides algorithms for estimating the integral of a function.
 */
public class IntegralAlgorithms {
    /**
     * Don't let anyone instantiate this class.
     */
    private IntegralAlgorithms() {
    }

    /**
     * Romberg Quadrature.
     * <p>
     * References:
     * <dl>
     *     <dt>waruyama</dt>
     *     <dd><a href="https://github.com/Pomax/BezierInfo-2/issues/77">github.com</a></a></dd>
     *     <dt>Wikipedia. Romberg's method.</dt>
     *     <dd><a href="https://en.wikipedia.org/wiki/Romberg%27s_method">wikipedia.org</a></a></dd>
     * </dl>
     *
     * @param f       the function
     * @param t0      the lower bound of the integral
     * @param t1      the upper bound of the integral
     * @param epsilon the desired precision
     * @return the estimated integral
     */
    static double rombergQuadrature(ToDoubleFunction<Double> f, double t0, double t1, double epsilon) {
        int maxSteps = 5;
        double h = t1 - t0;

        double[] Rp = new double[maxSteps];
        double[] Rc = new double[maxSteps];
        Rp[0] = (f.applyAsDouble(t0) + f.applyAsDouble(t1)) * h * 0.5;

        for (int i = 1; i < maxSteps; i++) {
            h /= 2;
            double c = 0;
            int ep = 1 << (i - 1);
            for (int j = 1; j <= ep; j++) {
                c += f.applyAsDouble(t0 + (2 * j - 1) * h);
            }
            Rc[0] = h * c + 0.5 * Rp[0]; //R(i,0)
            for (int j = 1; j <= i; j++) {
                double n_k = Math.pow(4, j);
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

        double range = max - min;
        double st = 0.5 * range * (func.applyAsDouble(min) + func.applyAsDouble(max));
        double t = st;
        double s = 4.0 * st / 3.0;
        double os = s;
        double ost = st;

        int it = 1;
        for (int n = 2; n <= 20; n++) {
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
}
