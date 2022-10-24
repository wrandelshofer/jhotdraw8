package org.jhotdraw8.geom;

import java.util.function.ToDoubleFunction;

import static org.jhotdraw8.geom.BezierCurves.getLengthIntegrand;

public class BezierArcLengthRomberg {

    /**
     * Don't let anyone instantiate this class.
     */
    public BezierArcLengthRomberg() {
    }

    public static double arcLength(double[] b, double eps) {
        ToDoubleFunction<Double> f = getLengthIntegrand(b);
        return IntegralAlgorithms.rombergQuadrature(f, 0, 1, eps);
    }
}
