package org.jhotdraw8.geom;

import java.util.function.ToDoubleFunction;

import static org.jhotdraw8.geom.CubicCurves.getArcLengthIntegrand;

public class CubicCurveArcLengthSimpson {

    /**
     * Don't let anyone instantiate this class.
     */
    public CubicCurveArcLengthSimpson() {
    }

    public static double arcLength(double[] b, int offset, double eps) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(b, offset);
        return Integrals.simpson(f, 0, 1, eps);
    }

}
