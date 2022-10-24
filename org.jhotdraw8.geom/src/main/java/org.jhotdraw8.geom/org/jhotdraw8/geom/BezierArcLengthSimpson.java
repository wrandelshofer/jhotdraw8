package org.jhotdraw8.geom;

import java.util.function.ToDoubleFunction;

import static org.jhotdraw8.geom.BezierCurves.getLengthIntegrand;

public class BezierArcLengthSimpson {

    /**
     * Don't let anyone instantiate this class.
     */
    public BezierArcLengthSimpson() {
    }

    public static double arcLength(double[] b, double eps) {
        ToDoubleFunction<Double> f = getLengthIntegrand(b);
        return IntegralAlgorithms.simpson(f, 0, 1, eps);
    }

}
