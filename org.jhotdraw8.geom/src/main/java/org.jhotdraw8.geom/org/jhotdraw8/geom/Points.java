/*
 * @(#)Points.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;

import java.awt.*;
import java.awt.geom.PathIterator;

import static java.lang.Math.sqrt;

public class Points {
    /**
     * Don't let anyone instantiate this class.
     */
    private Points() {
    }

    /**
     * Gets the squared distance between the points (x1,y1) and (x2,y2).
     *
     * @param x1 x-coordinate of point 1
     * @param y1 y-coordinate of point 1
     * @param x2 x-coordinate of point 2
     * @param y2 y-coordinate of point 2
     * @return
     */
    public static double squaredDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    /**
     * Computes the distance between the points (x1,y1) and (x2,y2).
     *
     * @param x1 x-coordinate of point 1
     * @param y1 y-coordinate of point 1
     * @param x2 x-coordinate of point 2
     * @param y2 y-coordinate of point 2
     * @return
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return sqrt(dx * dx + dy * dy);
    }

    /**
     * Computes the distance between the points (x1,y1) and (x2,y2) with float precision.
     * <p>
     * Computing the sqrt of a float is twice as fast as computing the sqrt of a double.
     *
     * @param x1 x-coordinate of point 1
     * @param y1 y-coordinate of point 1
     * @param x2 x-coordinate of point 2
     * @param y2 y-coordinate of point 2
     * @return
     */
    public static float distanceF(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (float) sqrt((float) (dx * dx + dy * dy));
    }

    public static boolean almostEqual(java.awt.geom.Point2D v1, java.awt.geom.Point2D v2) {
        return almostEqual(v1, v2, Rectangles.REAL_THRESHOLD);
    }

    public static boolean almostEqual(java.awt.geom.Point2D v1, java.awt.geom.Point2D v2, double epsilon) {
        return v1.distanceSq(v2) < epsilon * epsilon;
    }

    public static boolean almostEqual(double x0, double y0, double x1, double y1) {
        return almostEqual(x0, y0, x1, y1, Rectangles.REAL_THRESHOLD);
    }

    public static boolean almostEqual(double x0, double y0, double x1, double y1, double epsilon) {
        return squaredDistance(x0, y0, x1, y1) < epsilon * epsilon;
    }

    public static boolean almostZero(java.awt.geom.Point2D.Double v) {
        return almostZero(v, Rectangles.REAL_THRESHOLD);
    }

    public static boolean almostZero(java.awt.geom.Point2D.Double v, double epsilon) {
        return Points2D.magnitudeSq(v) < epsilon * epsilon;
    }

    public static boolean almostEqual(double a, double b) {
        return almostEqual(a, b, Rectangles.REAL_THRESHOLD);
    }

    public static boolean almostEqual(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static boolean almostZero(double a) {
        return almostZero(a, Rectangles.REAL_THRESHOLD);
    }

    public static boolean almostZero(double a, double epsilon) {
        return Math.abs(a) < epsilon;
    }

    /**
     * Computes the distance from the given shape to the given point.
     *
     * @param awtShape a shape
     * @param x        x-coordinate of the point
     * @param y        y-coordinate of the point
     * @return the distance
     */
    public static double distanceFromShape(@NonNull Shape awtShape, double x, double y) {
        return Math.sqrt(squaredDistanceFromShape(awtShape, x, y));
    }

    public static double squaredDistanceFromShape(@NonNull Shape awtShape, double x, double y) {
        if (awtShape.contains(x, y)) {
            return 0;
        }
        double[] coords = new double[6];
        double firstX = Double.NaN, firstY = Double.NaN;
        double lastX = Double.NaN, lastY = Double.NaN;
        double minSquaredDistance = Double.POSITIVE_INFINITY;
        for (final PathIterator it = awtShape.getPathIterator(null, 1); !it.isDone(); it.next()) {
            double squaredDistance;
            switch (it.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    firstX = coords[0];
                    firstY = coords[1];
                    lastX = coords[0];
                    lastY = coords[1];
                    squaredDistance = Double.POSITIVE_INFINITY;
                    break;
                case PathIterator.SEG_LINETO:
                    squaredDistance = Lines.squaredDistanceFromLine(lastX, lastY, coords[0], coords[1], x, y);
                    lastX = coords[0];
                    lastY = coords[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    squaredDistance = Lines.squaredDistanceFromLine(lastX, lastY, firstX, firstY, x, y);
                    firstX = lastX;
                    firstY = lastY;
                    break;
                default:
                    squaredDistance = Double.POSITIVE_INFINITY;
                    break;
            }
            if (squaredDistance < minSquaredDistance) {
                minSquaredDistance = squaredDistance;
            }
        }
        return minSquaredDistance;

    }
}
