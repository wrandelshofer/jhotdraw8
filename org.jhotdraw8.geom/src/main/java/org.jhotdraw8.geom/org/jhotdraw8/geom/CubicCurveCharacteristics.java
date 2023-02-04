package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.primitive.DoubleArrayList;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

import static java.lang.Math.sqrt;
import static org.jhotdraw8.geom.Lines.isCollinear;
import static org.jhotdraw8.geom.Points.almostZero;

public class CubicCurveCharacteristics {


    /**
     * Don't let anyone instantiate this class.
     */
    public CubicCurveCharacteristics() {
    }

    public enum Characteristics {
        PLAIN_CURVE,
        /**
         * The curve has 1 inflection point.
         */
        SINGLE_INFLECTION,
        /** The curve has 2 inflection points. */
        DOUBLE_INFLECTION,
        /** The curve has 1 cusp at the singular point. */
        CUSP,
        /** The curve has a loop that intersects neither at t0 nor at t1 with the curve. */
        LOOP,
        /**
         * The curve has a loop that intersects at t0 with the curve.
         */
        LOOP_AT_T_0,
        /**
         * The curve has a loop that intersects at t1 with the curve.
         */
        LOOP_AT_T_1,
        /**
         * The curve is (almost) a line.
         */
        COLLINEAR
    }


    public static Characteristics characteristics(double[] b, int o) {
        return characteristics(b[o + 0], b[o + 1],
                b[o + 2], b[o + 3],
                b[o + 4], b[o + 5],
                b[o + 6], b[o + 7]);
    }

    /**
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @return
     */
    public static Characteristics characteristics(double x0, double y0,
                                                  double x1, double y1,
                                                  double x2, double y2,
                                                  double x3, double y3) {

        Point2D.Double can = canonicalForm(x0, y0, x1, y1, x2, y2, x3, y3);
        if (can == null) {
            return Characteristics.COLLINEAR;
        }
        double x = can.x, y = can.y;

        if (y > 1) {
            return Characteristics.SINGLE_INFLECTION;
        }
        if (y <= 1 && x <= 1) {
            double cusp = (-x * x + 2 * x + 3) / 4;

            if (x <= 0) {
                double l1 = (-x * x + 3 * x) / 3;
                if (Points.almostEqual(y, l1, 0.06)) {
                    return Characteristics.LOOP_AT_T_0;
                }
                if (l1 < y && y < cusp) {
                    return Characteristics.LOOP;
                }
            }

            if (0 <= x) {
                double l0 = (sqrt(3) * sqrt(4 * x - x * x) - x) / 2;
                if (Points.almostEqual(y, l0, 0.06)) {
                    return Characteristics.LOOP_AT_T_1;
                }
                if (l0 < y && y < cusp) {
                    return Characteristics.LOOP;
                }
            }

            if (Points.almostEqual(y, cusp, 0.06)) {
                return Characteristics.CUSP;
            }

            if (y > cusp) {
                return Characteristics.DOUBLE_INFLECTION;
            }
            if (y < cusp) {
                return Characteristics.PLAIN_CURVE;
            }

        }
        return Characteristics.PLAIN_CURVE;
    }


    /**
     * Transforms the given cubic curve into canonical form.
     * <p>
     * Where:
     * <pre>
     *   B0: x0 = 0, y0 = 0
     *   B1: x1 = 0, y1 = 1
     *   B2: x2 = 1, y2 = 1
     *   B3: x3 = ..., y3 = ...
     * </pre>
     * References:
     * <dl>
     *     <dt>Calculating the Extremal Points of a Cubic Bezier Curve</dt>
     *     <dd><a href="https://pomax.github.io/bezierinfo/#canonical">pomax.github.io</a></dd>
     * </dl>
     * <dl>
     *     <dt>Pomax. A Primer on Bézier Curves. The canonical form (for cubic curves).</dt>
     *     <dd><a href="https://pomax.github.io/bezierinfo/#canonical">pomax.github.io</a></dd>
     * </dl>
     *
     * @return returns the coordinates of x3,y3 or null if the mapping
     * failed
     */
    public static Point2D.Double canonicalForm(double x0, double y0,
                                               double x1, double y1,
                                               double x2, double y2,
                                               double x3, double y3
    ) {

        // Handle degenerate forms
        if (isCollinear(x0, y0, x1, y1, x2, y2)) {
            if (isCollinear(x1, y1, x2, y2, x3, y3)) {
                return null;// collinear
            } else {
                // Map B3 =(0,0); B2=(0,1); B1=(1,1) and keep B0 moving.
                double swapx, swapy;
                swapx = x0;
                swapy = y0;
                x0 = x3;
                y0 = y3;
                x3 = swapx;
                y3 = swapy;
                swapx = x1;
                swapy = y1;
                x1 = x2;
                y1 = y2;
                x2 = swapx;
                y2 = swapy;
            }
        }

        double xn = -x0 + x3 - (-x0 + x1) * (-y0 + y3) / (-y0 + y1);
        double xd = -x0 + x2 - (-x0 + x1) * (-y0 + y2) / (-y0 + y1);
        double np4x = xn / xd;

        double yt1 = (-y0 + y3) / (-y0 + y1);
        double yt2 = 1 - ((-y0 + y2) / (-y0 + y1));
        double yp = yt2 * xn / xd;
        double np4y = yt1 + yp;

        return new Point2D.Double(np4x, np4y);
    }

    /**
     * Rotates and translates the provided bezier curve so that 3 coordinates
     * are approximately zero: x0, y0 and y3.
     */
    public static double[] align(double x0, double y0,
                                 double x1, double y1,
                                 double x2, double y2,
                                 double x3, double y3) {
        double[] e = {x0, y0, x1, y1, x2, y2, x3, y3};
        double theta = -Angles.atan2(y3 - y0, x3 - x0);
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double[] n = new double[8];
        for (int i = 0; i < 8; i += 2) {
            double px = e[i];
            double py = e[i + 1];
            n[i] = (px - x0) * cosTheta - (py - y0) * sinTheta;
            n[i + 1] = (px - x0) * sinTheta + (py - y0) * cosTheta;
        }
        return n;
    }

    /**
     * Computes the inflection points of the given cubic curve.
     */
    public @NonNull DoubleArrayList inflectionPoints(CubicCurve2D.Double c) {
        return inflectionPoints(c.x1, c.y1, c.ctrlx1, c.ctrly1, c.ctrlx2, c.ctrly2, c.x2, c.y2);
    }

    public static @NonNull DoubleArrayList inflectionPoints(double[] b, int o) {
        return inflectionPoints(b[o + 0], b[o + 1],
                b[o + 2], b[o + 3],
                b[o + 4], b[o + 5],
                b[o + 6], b[o + 7]);
    }

    public static @Nullable Double singularPoint(double[] b, int o) {
        return singularPoint(b[o + 0], b[o + 1],
                b[o + 2], b[o + 3],
                b[o + 4], b[o + 5],
                b[o + 6], b[o + 7]);
    }

    /**
     * Computes the inflection points of the given cubic curve.
     * <p>
     * If the curve has two inflection points:
     * <ul>
     *     <li>The point in the middle of the two inflection points
     *     {@code (t₀+t₁)/2} has maximal curvature. This point is
     *     called a 'singular point' by Zhiyi Zhang et al.</li>
     *     <li>If the inflection points are the same {@code almostEqual(t₀,t₁)}
     *     value, we have a cusp.</li>
     * </ul>
     * <p>
     * <p>
     * References:
     * <dl>
     *     <dt>Stackoverflow, Calculating the Inflection Point of a Cubic Bezier Curve,
     *     Copyright MBo, CC BY-SA 4.0 license</dt>
     *     <dd><a href="https://stackoverflow.com/questions/35901079/calculating-the-inflection-point-of-a-cubic-bezier-curve">stackoverflow.com</a></dd>
     * </dl>
     * <dl>
     *    <dt>Zhiyi Zhang, Min Chen , Xian Zhang, Zepeng Wang.
     *    Analysis of Inflection Points for Planar Cubic Bé́zier Curve</dt>
     *    <dd><a href="https://cie.nwsuaf.edu.cn/docs/20170614173651207557.pdf">cie.nwsuaf.edu.cn</a></dd>
     * </dl>
     */
    public static @NonNull DoubleArrayList inflectionPoints(double x0, double y0,
                                                            double x1, double y1,
                                                            double x2, double y2,
                                                            double x3, double y3) {

        DoubleArrayList result = new DoubleArrayList(2);
        double[] n = align(x0, y0, x1, y1, x2, y2, x3, y3);
        double
                ax1 = n[2],
                ay1 = n[3],
                ax2 = n[4],
                ay2 = n[5],
                ax3 = n[6];
        double
                i = ax2 * ay1,
                a = ax3 * ay1,
                r = ax1 * ay2,
                o = ax3 * ay2,
                s = -3 * i + 2 * a + 3 * r - o,
                l = 3 * i - a - 3 * r,
                c = r - i;
        if (almostZero(s, 1e-6)) {
            if (!almostZero(l, 1e-6)) {
                double h = -c / l;
                if (0 <= h && h <= 1) {
                    result.add(h);
                }
            }
            return result;
        }
        double det = sqrt(l * l - 4 * s * c);
        double q = 2 * s;
        if (!almostZero(q, 1e-6)) {
            double t1 = (det - l) / q;
            double t2 = -(l + det) / q;
            if (0 <= t1 && t1 <= 1) {
                result.add(t1);
            }
            if (0 <= t2 && t2 <= 1) {
                result.add(t2);
            }
        }
        result.sort();
        return result;
    }


    public static @Nullable Double singularPoint(double x0, double y0,
                                                 double x1, double y1,
                                                 double x2, double y2,
                                                 double x3, double y3) {

        double[] n = align(x0, y0, x1, y1, x2, y2, x3, y3);
        double
                ax1 = n[2],
                ay1 = n[3],
                ax2 = n[4],
                ay2 = n[5],
                ax3 = n[6];
        double
                i = ax2 * ay1,
                a = ax3 * ay1,
                r = ax1 * ay2,
                o = ax3 * ay2,
                s = -3 * i + 2 * a + 3 * r - o,
                l = 3 * i - a - 3 * r,
                c = r - i;
        double q = 2 * s;
        if (!almostZero(q, 1e-6)) {
            double t = -l / q;
            if (0 <= t && t <= 1) {
                return t;
            }
        }
        return null;
    }
}
