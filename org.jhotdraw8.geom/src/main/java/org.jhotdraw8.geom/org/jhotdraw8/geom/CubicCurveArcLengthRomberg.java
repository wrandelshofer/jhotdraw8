package org.jhotdraw8.geom;

import java.util.function.ToDoubleFunction;

import static org.jhotdraw8.geom.CubicCurves.getArcLengthIntegrand;

public class CubicCurveArcLengthRomberg {

    /**
     * Don't let anyone instantiate this class.
     */
    public CubicCurveArcLengthRomberg() {
    }

    public static double arcLength(double[] b, int offset, double eps) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(b, offset);
        return Integrals.rombergQuadrature(f, 0, 1, eps);
    }
}
