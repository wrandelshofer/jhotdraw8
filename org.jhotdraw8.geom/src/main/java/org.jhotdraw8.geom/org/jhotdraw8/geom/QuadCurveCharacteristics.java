package org.jhotdraw8.geom;

import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.awt.geom.QuadCurve2D;

public class QuadCurveCharacteristics {
    /**
     * Don't let anyone instantiate this class.
     */
    private QuadCurveCharacteristics() {
    }

    /**
     * Computes the extreme points of the given quadratic curve.
     */
    public static DoubleArrayList extremePoints(QuadCurve2D.Double c) {
        return extremePoints(c.x1, c.y1, c.ctrlx, c.ctrly, c.x2, c.y2);
    }

    /**
     * Computes the extreme points of the given quadratic curve.
     * <p>
     * References:
     * <dl>
     *     <dt>Extremes for Bézier curves</dt>
     *     <dd><a href="https://github.polettix.it/ETOOBUSY/2020/07/09/bezier-extremes/">github.polettix.it</a></dd>
     * </dl>
     */
    public static DoubleArrayList extremePoints(double x0, double y0,
                                                         double x1, double y1,
                                                         double x2, double y2) {
        double qx, qy, mx, my, t0, t1;

        qx = x1 - x0;
        qy = y1 - y0;
        mx = x0 - 2 * x1 + x2;
        my = y0 - 2 * y1 + y2;

        t0 = -qx / mx;
        t1 = -qy / my;

        var list = new DoubleArrayList();
        if (0 <= t0 && t0 <= 1) {
            list.add(t0);
        }
        if (0 <= t1 && t1 <= 1) {
            list.add(t1);
        }
        return list;
    }
}
