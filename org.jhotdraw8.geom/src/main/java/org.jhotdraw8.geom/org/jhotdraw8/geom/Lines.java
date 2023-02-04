package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.Double2Consumer;

import java.awt.geom.Point2D;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class Lines {
    /**
     * Evaluates the given curve at the specified time.
     *
     * @param x0 point P0 of the curve
     * @param y0 point P0 of the curve
     * @param x1 point P1 of the curve
     * @param y1 point P1 of the curve
     * @param t  the time
     * @return the point at time t
     */
    public static @NonNull Point2D.Double evalLine(double x0, double y0, double x1, double y1, double t) {
        return new Point2D.Double(lerp(x0, x1, t), lerp(y0, y1, t));
    }


    /**
     * Returns true if the three points are collinear.
     *
     * @param a x-coordinate of point 0
     * @param b y-coordinate of point 0
     * @param m x-coordinate of point 1
     * @param n y-coordinate of point 1
     * @param x x-coordinate of point 2
     * @param y y-coordinate of point 2
     * @return true if collinear
     */
    public static boolean isCollinear(double a, double b, double m, double n, double x, double y) {
        return abs(a * (n - y) + m * (y - b) + x * (b - n)) < 1e-6;
    }

    /**
     * compute distance of point from line segment, or Double.MAX_VALUE if
     * perpendicular projection is outside segment; or If pts on line are same,
     * return distance from point
     *
     * @param xa the x-coordinate of point a on the line
     * @param ya the y-coordinate of point a on the line
     * @param xb the x-coordinate of point b on the line
     * @param yb the y-coordinate of point b on the line
     * @param xc the x-coordinate of the point c
     * @param yc the y-coordinate of the point c
     * @return the distance from the line
     */
    public static double distanceFromLine(double xa, double ya,
                                          double xb, double yb,
                                          double xc, double yc) {
        return Math.sqrt(squaredDistanceFromLine(xa, ya, xb, yb, xc, yc));
    }

    public static double squaredDistanceFromLine(double xa, double ya,
                                                 double xb, double yb,
                                                 double xc, double yc) {

        // from Doug Lea's PolygonFigure
        // source:http://vision.dai.ed.ac.uk/andrewfg/c-g-a-faq.html#q7
        //Let the point be C (XC,YC) and the line be AB (XA,YA) to (XB,YB).
        //The length of the
        //      line segment AB is L:
        //
        //                    ___________________
        //                   |        2         2
        //              L = \| (XB-XA) + (YB-YA)
        //and
        //
        //                  (YA-YC)(YA-YB)-(XA-XC)(XB-XA)
        //              r = -----------------------------
        //                              L**2
        //
        //                  (YA-YC)(XB-XA)-(XA-XC)(YB-YA)
        //              s = -----------------------------
        //                              L**2
        //
        //      Let I be the point of perpendicular projection of C onto AB, the
        //
        //              XI=XA+r(XB-XA)
        //              YI=YA+r(YB-YA)
        //
        //      Distance from A to I = r*L
        //      Distance from C to I = s*L
        //
        //      If r < 0 I is on backward extension of AB
        //      If r>1 I is on ahead extension of AB
        //      If 0<=r<=1 I is on AB
        //
        //      If s < 0 C is left of AB (you can just check the numerator)
        //      If s>0 C is right of AB
        //      If s=0 C is on AB
        double xdiff = xb - xa;
        double ydiff = yb - ya;
        double l2 = xdiff * xdiff + ydiff * ydiff;

        if (l2 == 0) {// Line is a single point
            return Points.squaredDistance(xa, ya, xc, yc);
        }

        double rnum = (ya - yc) * (ya - yb) - (xa - xc) * (xb - xa);
        double r = rnum / l2;

        if (r < 0.0) {
            return Points.squaredDistance(xa, ya, xc, yc);
        }
        if (r > 1.0) {
            return Points.squaredDistance(xb, yb, xc, yc);
        }

        double xi = xa + r * xdiff;
        double yi = ya + r * ydiff;
        double xd = xc - xi;
        double yd = yc - yi;
        return (xd * xd + yd * yd);

        /*
         * for directional version, instead use
         * double snum = (ya-yc) * (xb-xa) - (xa-xc) * (yb-ya);
         * double s = snum / l2;
         *
         * double l = sqrt((double)l2);
         * return = s * l;
         */
    }

    /**
     * Linear interpolation from {@code a} to {@code b} at {@code t}.
     *
     * @param a a
     * @param b b
     * @param t a value in the range [0, 1]
     * @return the interpolated value
     */
    public static double lerp(double a, double b, double t) {
        return (b - a) * t + a;
    }

    /**
     * Gets the distance between to points
     *
     * @param x1 the x coordinate of point 1
     * @param y1 the y coordinate of point 1
     * @param x2 the x coordinate of point 2
     * @param y2 the y coordinate of point 2
     * @return the distance between the two points
     */
    public static double arcLength(double x1, double y1, double x2, double y2) {
        return sqrt(Points.squaredDistance(x1, y1, x2, y2));
    }

    /**
     * Gets the arc length s at the given time t.
     *
     * @param x1 the x coordinate of point 1
     * @param y1 the y coordinate of point 1
     * @param x2 the x coordinate of point 2
     * @param y2 the y coordinate of point 2
     * @param t  the time
     * @return arc length s at time t
     */
    public static double arcLength(double x1, double y1, double x2, double y2, double t) {
        return t * arcLength(x1, y1, x2, y2);
    }

    /**
     * Computes time t at the given arc length s.
     *
     * @param x1 the x coordinate of point 1
     * @param y1 the y coordinate of point 1
     * @param x2 the x coordinate of point 2
     * @param y2 the y coordinate of point 2
     * @param s  arc length
     * @return t at s
     */
    public static double invArcLength(double x1, double y1, double x2, double y2, double s) {
        return s / arcLength(x1, y1, x2, y2);
    }

    /**
     * Computes time t at the given arc length s.
     *
     * @param p      points of the line
     * @param offset index of the first point in array {@code a}
     * @param s      arc length
     * @return t at s
     */
    public static double invArcLength(@NonNull double[] p, int offset, double s) {
        return s / arcLength(p[offset], p[offset + 1], p[offset + 2], p[offset + 3]);
    }

    /**
     * Splits the provided line into two parts.
     *
     * @param x0          point 1 of the line
     * @param y0          point 1 of the line
     * @param x1          point 2 of the line
     * @param y1          point 2 of the line
     * @param t           where to split
     * @param leftLineTo  if not null, accepts the curve from x1,y1 to t1
     * @param rightLineTo if not null, accepts the curve from t1 to x2,y2
     */
    public static void split(double x0, double y0, double x1, double y1, double t,
                             @Nullable Double2Consumer leftLineTo,
                             @Nullable Double2Consumer rightLineTo) {
        final double x12 = (x1 - x0) * t + x0;
        final double y12 = (y1 - y0) * t + y0;

        if (leftLineTo != null) {
            leftLineTo.accept(x12, y12);
        }
        if (rightLineTo != null) {
            rightLineTo.accept(x1, y1);
        }
    }

    public static @NonNull Point2D.Double lerp(double x0, double y0, double x1, double y1, double t) {
        return new Point2D.Double(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t);
    }

    public static @NonNull Point2D.Double lerp(double[] a, int offset, double t) {
        double x0 = a[offset], y0 = a[offset + 1], x1 = a[offset + 2], y1 = a[offset + 3];
        return new Point2D.Double(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t);
    }

    public static @NonNull PointAndTangent eval(double[] a, int offset, double t) {
        double x0 = a[offset], y0 = a[offset + 1], x1 = a[offset + 2], y1 = a[offset + 3];
        return new PointAndTangent(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t,
                x1 - x0, y1 - y0);
    }

    /**
     * Computes the linear interpolation/extrapolation between two points.
     *
     * @param start point a
     * @param end   point b
     * @param t     a value between [0, 1] defines the interpolation between a and
     *              b. Values outside this range yield an extrapolation.
     * @return the interpolated or extrapolated value
     */
    public static @NonNull Point2D.Double lerp(@NonNull Point2D start, @NonNull Point2D end, double t) {
        return lerp(start.getX(), start.getY(), end.getX(), end.getY(), t);
    }
}
