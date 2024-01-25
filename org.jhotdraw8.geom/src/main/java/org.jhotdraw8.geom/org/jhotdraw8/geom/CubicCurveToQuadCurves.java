package org.jhotdraw8.geom;

import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.awt.geom.CubicCurve2D;

/**
 * Approximates a cubic Bézier curve with quadratic Bézier curves.
 * <p>
 * References:
 * <dl>
 *     <dt>Proc. ACM Comput. Graph. Interact. Tech., Vol. 3, No. 2, Article 16. Publication date: August 2020.
 *     Quadratic Approximation of Cubic Curves.
 *     NGHIA TRUONG, University of Utah, CEM YUKSEL, University of Utah, LARRY SEILER, Facebook Reality Labs.
 *     Copyright 2020 held by the owner/author(s). Publication rights licensed to ACM.
 *     </dt>
 *     <dd><a href="https://ttnghia.github.io/pdf/QuadraticApproximation.pdf">ttnghia.github.io</a>
 *     </dd>
 * </dl>
 */
public class CubicCurveToQuadCurves {
    /**
     * Approximates a cubic curve with up to 8 quadratic curves.
     *
     * @param p       the points of the cubic curve
     * @param offsetP the index of the first point in p
     * @param q       the points of the quadratic curves (on output, must space for up to 8*6=48 coords).
     * @param offsetQ the index of the first point in q
     * @return the number of quadratic curves
     */
    public int approximateCubicCurve(double[] p, int offsetP, double[] q, int offsetQ, double tolerance) {
        return approximateCubicCurve(p, offsetP, q, offsetQ, tolerance, 2);
    }

    private int approximateCubicCurve(double[] p, int offsetP, double[] q, int offsetQ, double tolerance, int maxDepth) {
        double errorSquared = maxDepth == 0 ? 0 : estimateCubicCurveApproximationErrorSquared(p, offsetP);
        if (errorSquared > tolerance * tolerance) {
            // we should split

            DoubleArrayList list = CubicCurveCharacteristics.inflectionPoints(p, offsetP);
            Double singularPoint = CubicCurveCharacteristics.singularPoint(p, offsetP);
            if (singularPoint != null) {
                list.add(singularPoint);
                list.sort();
            }
            final double epsilon = 1e-6;
            for (double t : list) {
                if (!Points.almostEqual(t, 0, epsilon) && !Points.almostEqual(t, 1, epsilon)) {
                    return approximateCubicCurveSplitCase(p, offsetP, t, q, offsetQ, tolerance, maxDepth);
                }
            }
            return approximateCubicCurveSplitCase(p, offsetP, 0.5, q, offsetQ, tolerance, maxDepth);
        } else {
            // we only need to split once or not at all
            return approximateCubicCurveBaseCase(p, offsetP, q, offsetQ, tolerance);
        }
    }

    private int approximateCubicCurveBaseCase(double[] p, int offsetP, double[] q, int offsetQ, double tolerance) {
        double x0 = p[offsetP];
        double y0 = p[offsetP + 1];
        double x1 = p[offsetP + 2];
        double y1 = p[offsetP + 3];
        double x2 = p[offsetP + 4];
        double y2 = p[offsetP + 5];
        double x3 = p[offsetP + 6];
        double y3 = p[offsetP + 7];

        // The quadratic curve always starts at the same point as the cubic curve
        int qq = offsetQ;
        q[qq] = x0;
        q[qq + 1] = y0;

        if (CubicCurve2D.getFlatnessSq(p, offsetP) <= tolerance * tolerance) {
            // p1 and p2 coincide or
            // the curve is almost flat
            q[qq + 2] = (x0 + x3) * 0.5;
            q[qq + 3] = (y0 + y3) * 0.5;
            q[qq + 4] = x3;
            q[qq + 5] = y3;
            return 1;
        }
        double gamma = 0.5;
        double x2i = x0 + (3.0 * 0.5 * gamma) * (x1 - x0);
        double y2i = y0 + (3.0 * 0.5 * gamma) * (y1 - y0);
        double x2iplus1 = x3 + (3.0 * 0.5 * (1 - gamma)) * (x2 - x3);
        double y2iplus1 = y3 + (3.0 * 0.5 * (1 - gamma)) * (y2 - y3);
        q[qq + 2] = x2i;
        q[qq + 3] = y2i;
        q[qq + 4] = q[qq + 6] = (1 - gamma) * x2i + gamma * x2iplus1;
        q[qq + 5] = q[qq + 7] = (1 - gamma) * y2i + gamma * y2iplus1;
        q[qq + 8] = x2iplus1;
        q[qq + 9] = y2iplus1;
        q[qq + 10] = x3;
        q[qq + 11] = y3;
        return 2;
    }

    private int approximateCubicCurveSplitCase(double[] p, int offsetP, double t, double[] q, int offsetQ, double tolerance, int maxDepth) {
        double[] pp = new double[8 * 2];
        CubicCurves.split(p, offsetP, t, pp, 0, pp, 8);
        int count = approximateCubicCurve(pp, 0, q, offsetQ, tolerance, maxDepth - 1);
        return count + approximateCubicCurve(pp, 8, q, offsetQ + count * 6, tolerance, maxDepth - 1);
    }

    private double estimateCubicCurveApproximationErrorSquared(double[] p, int offsetP) {
        double x0 = p[offsetP];
        double y0 = p[offsetP + 1];
        double x1 = p[offsetP + 2];
        double y1 = p[offsetP + 3];
        double x2 = p[offsetP + 4];
        double y2 = p[offsetP + 5];
        double x3 = p[offsetP + 6];
        double y3 = p[offsetP + 7];

        double ex = -x0 + 3 * x1 - 3 * x2 + x3;
        double ey = -y0 + 3 * y1 - 3 * y2 + y3;
        return (ex * ex + ey * ey) * (1.0 / (54 * 54));
    }

}
