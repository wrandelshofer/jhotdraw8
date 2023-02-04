package org.jhotdraw8.geom;

import java.util.function.ToDoubleFunction;

import static org.jhotdraw8.geom.CubicCurves.getArcLengthIntegrand;

public class CubicCurveArcLengthGaussLegendre {

    /**
     * Don't let anyone instantiate this class.
     */
    public CubicCurveArcLengthGaussLegendre() {
    }

    public static double arcLength(double[] b, int offset) {
        ToDoubleFunction<Double> f = getArcLengthIntegrand(b, offset);
        return Integrals.gaussLegendre7(f, 0, 1);
    }
}
