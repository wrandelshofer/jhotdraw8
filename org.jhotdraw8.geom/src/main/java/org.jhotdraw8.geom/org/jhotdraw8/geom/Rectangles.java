/*
 * @(#)Rectangles.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;

import java.awt.geom.Rectangle2D;

import static java.lang.Math.max;

/**
 * Provides geometry utility functions.
 */
public class Rectangles {

    /**
     * The bitmask that indicates that a point lies below the rectangle.
     */
    public static final int OUT_BOTTOM = 8;
    /**
     * The bitmask that indicates that a point lies to the left of the
     * rectangle.
     */
    public static final int OUT_LEFT = 1;
    /**
     * The bitmask that indicates that a point lies to the right of the
     * rectangle.
     */
    public static final int OUT_RIGHT = 4;
    /**
     * The bitmask that indicates that a point lies above the rectangle.
     */
    public static final int OUT_TOP = 2;
    /**
     * Absolute threshold to be used for comparing reals generally.
     */
    public static final double REAL_THRESHOLD = 1e-8;

    /**
     * Don't let anyone instantiate this class.
     */
    private Rectangles() {
    }


    /**
     * Returns true if the bounds contain the specified point within the given
     * tolerance.
     *
     * @param rx        the bounds x-coordinate
     * @param ry        the bounds y-coordinate
     * @param rw        the bounds width
     * @param rh        the bounds height
     * @param x         the x-coordinate of the point
     * @param y         the y-coordinate of the point
     * @param tolerance the tolerance
     * @return true if inside
     */
    public static boolean contains(double rx, double ry, double rw, double rh, double x, double y, double tolerance) {
        return rx - tolerance <= x && x <= (rx + rw) + tolerance
                && ry - tolerance <= y && y <= (ry + rh) + tolerance;
    }

    /**
     * Returns true, if rectangle 1 contains rectangle 2.
     * <p>
     * This method is similar to Rectangle2D.contains, but also returns true,
     * when rectangle1 contains rectangle2 and either or both of them are empty.
     *
     * @param r1 Rectangle 1.
     * @param r2 Rectangle 2.
     * @return true if r1 contains r2.
     */
    public static boolean containsAWT(@NonNull Rectangle2D r1, @NonNull Rectangle2D r2) {
        return (r2.getX()) >= r1.getX()
                && r2.getY() >= r1.getY()
                && (r2.getX() + max(0, r2.getWidth())) <= r1.getX() + max(0, r1.getWidth())
                && (r2.getY() + max(0, r2.getHeight())) <= r1.getY() + max(0, r1.getHeight());
    }


}