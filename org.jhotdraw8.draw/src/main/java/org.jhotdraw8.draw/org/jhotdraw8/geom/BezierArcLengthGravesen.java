package org.jhotdraw8.geom;

import java.awt.geom.CubicCurve2D;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class BezierArcLengthGravesen {
    /**
     * Estimates the arc-length of a bézier curve using adaptive subdivision.
     * <p>
     * References:
     * <dl>
     *     <dt>Jens Gravesen. Adaptive subdivision and the length and energy of Bézier curves</dt>
     *     <dd><a href="https://www.sciencedirect.com/science/article/pii/0925772195000542/pdf?md5=36afa8ee3e26e8d5802e8dd0f994d3d8&pid=1-s2.0-0925772195000542-main.pdf">
     *        sciencedirect.com </a></dd>
     * </dl>
     *
     * @param b   the coordinates of the control points of the bézier curve
     * @param eps the tolerated error
     * @return an estimate of the arc length ± eps
     */
    public static double arcLength(double[] b, double eps) {
        double l0 = estimateLength(b);
        return arcLengthGravesen(b, eps, l0, 14);
    }

    /**
     * @param b        the coordinates of the control points of the bézier curve
     * @param eps      the tolerated error
     * @param l0       the estimated length of the bézier curve
     * @param maxDepth the maximal recursion depth
     * @return an estimate of the arc length ± eps
     */
    private static double arcLengthGravesen(double[] b, double eps, double l0, int maxDepth) {
        double[] b1 = new double[b.length];
        double[] b2 = new double[b.length];
        CubicCurve2D.subdivide(b, 0, b1, 0, b2, 0);
        double lb1 = estimateLength(b1);
        double lb2 = estimateLength(b2);
        double l1 = lb1 + lb2;
        double correction = (l0 - l1) * (1.0 / 15);
        double err = abs(correction);//Error estimate

        if (err < eps || maxDepth <= 0) {
            return l1 - correction;
        } else {
            double epsNext = eps * 0.5;
            return arcLengthGravesen(b1, epsNext, lb1, maxDepth - 1)
                    + arcLengthGravesen(b2, epsNext, lb2, maxDepth - 1);
        }
    }

    /**
     * Estimates the length of the given bézier curve.
     */
    private static double estimateLength(double[] b) {
        double lp = polygonLength(b);//The length of the control polygon.
        double lc = chordLength(b);//The length of the chord.
        int n = b.length / 2;//The degree of b.
        return (2 * lc + (n - 1) * lp) / (n + 1);
    }

    /**
     * Computes the length of the given polygon.
     * <p>
     * Where {@code b} is an array with {@code (x,y)}
     * coordinates:
     * {@code b={x0,y0, x1,y1, …}}.
     *
     * @param b the coordinates of polygon
     * @return length of the control polygon
     */
    private static double polygonLength(double[] b) {
        double sum = 0;
        double x0 = b[0];
        double y0 = b[1];
        for (int i = 2, n = b.length - 1; i < n; i += 2) {
            double x1 = b[i];
            double y1 = b[i + 1];
            double dx = x1 - x0;
            double dy = y1 - y0;
            sum += sqrt(dx * dx + dy * dy);
            x0 = x1;
            y0 = y1;
        }
        return sum;
    }

    /**
     * Computes the length of the chord.
     *
     * @param b the coordinates of the control points of a bézier curve
     * @return length of the control polygon
     */
    private static double chordLength(double[] b) {
        double x0 = b[0];
        double y0 = b[1];
        double x1 = b[b.length - 2];
        double y1 = b[b.length - 1];
        double dx = x1 - x0;
        double dy = y1 - y0;
        return sqrt(dx * dx + dy * dy);
    }
}
