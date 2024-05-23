/*
 * @(#)Lines.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import javafx.geometry.Point2D;

public class FXLines {


    public static Point2D lerp(double x0, double y0, double x1, double y1, double t) {
        return new Point2D(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t);
    }

    public static Point2D lerp(double[] a, int offset, double t) {
        double x0 = a[offset], y0 = a[offset + 1], x1 = a[offset + 2], y1 = a[offset + 3];
        return new Point2D(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t);
    }

    public static PointAndDerivative eval(double[] a, int offset, double t) {
        double x0 = a[offset], y0 = a[offset + 1], x1 = a[offset + 2], y1 = a[offset + 3];
        return new PointAndDerivative(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t,
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
    public static Point2D lerp(Point2D start, Point2D end, double t) {
        return lerp(start.getX(), start.getY(), end.getX(), end.getY(), t);
    }
}
