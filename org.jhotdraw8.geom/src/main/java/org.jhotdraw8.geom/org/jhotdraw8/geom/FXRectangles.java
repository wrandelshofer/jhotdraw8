/*
 * @(#)FXRectangles.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.jspecify.annotations.Nullable;

import java.awt.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class FXRectangles {
    /**
     * Gets the bounds of the specified shape.
     *
     * @param shape an AWT shape
     * @return JavaFX bounds
     */
    public static BoundingBox getBounds(Shape shape) {
        java.awt.geom.Rectangle2D r = shape.getBounds2D();
        return new BoundingBox(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Converts a bounding box to a AWT rectangle.
     *
     * @param r a bounding box
     * @return the rectangle
     */
    public static java.awt.geom.Rectangle2D.Double toAwtRectangle2D(Bounds r) {
        return new java.awt.geom.Rectangle2D.Double(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }

    /**
     * Converts a bounding box to a FX rectangle.
     *
     * @param r a bounding box
     * @return the rectangle
     */
    public static Rectangle2D toRectangle2D(Bounds r) {
        return new Rectangle2D(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }

    public static BoundingBox add(Bounds a, Bounds b) {
        double x = min(a.getMinX(), b.getMinX());
        double y = min(a.getMinY(), b.getMinY());
        return new BoundingBox(x, y, max(a.getMaxX(), b.getMaxX()) - x, max(a.getMaxY(), b.getMaxY()) - y);
    }

    /**
     * Calculate the center of the given bounds
     *
     * @param r the bounds
     * @return the center
     */
    public static Point2D center(Bounds r) {
        return new Point2D(
                r.getMinX() + r.getWidth() * 0.5,
                r.getMinY() + r.getHeight() * 0.5
        );
    }

    /**
     * Returns true if the bounds contain the specified point within the given
     * tolerance.
     *
     * @param r         the bounds
     * @param p         the point
     * @param tolerance the tolerance
     * @return true if inside
     */
    public static boolean contains(Bounds r, Point2D p, double tolerance) {
        return contains(r, p.getX(), p.getY(), tolerance);
    }

    /**
     * Returns true if the bounds contain the specified point within the given
     * tolerance.
     *
     * @param r         the bounds
     * @param x         the x-coordinate of the point
     * @param y         the y-coordinate of the point
     * @param tolerance the tolerance
     * @return true if inside
     */
    public static boolean contains(Bounds r, double x, double y, double tolerance) {
        return Rectangles.contains(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, tolerance);
    }

    /**
     * Gets the bounds of the specified shape.
     *
     * @param r a rectangle
     * @return JavaFX bounds
     */
    public static Bounds getBounds(Rectangle2D r) {
        return new BoundingBox(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }

    /**
     * Resizes the <code>Bounds</code> both horizontally and vertically.
     * <p>
     * This method returns a new <code>Bounds</code> so that it is
     * <code>h</code> units larger on both the left and right side, and
     * <code>v</code> units larger at both the top and bottom.
     * <p>
     * The new <code>Bounds</code> has (<code>x&nbsp;-&nbsp;h</code>,
     * <code>y&nbsp;-&nbsp;v</code>) as its top-left corner, a width of
     * <code>width</code>&nbsp;<code>+</code>&nbsp;<code>2h</code>, and a height
     * of <code>height</code>&nbsp;<code>+</code>&nbsp;<code>2v</code>.
     * <p>
     * If negative values are supplied for <code>h</code> and <code>v</code>,
     * the size of the <code>Rectangle2D</code> decreases accordingly. The
     * <code>grow</code> method does not check whether the resulting values of
     * <code>width</code> and <code>height</code> are non-negative.
     *
     * @param r the bounds
     * @param h the horizontal expansion
     * @param v the vertical expansion
     * @return the new rectangle
     */
    public static Bounds grow(Bounds r, double h, double v) {
        return new BoundingBox(
                r.getMinX() - h,
                r.getMinY() - v,
                r.getWidth() + h * 2d,
                r.getHeight() + v * 2d);
    }

    /**
     * Resizes the <code>Bounds</code> both horizontally and vertically.
     *
     * @param r  the bounds
     * @param hv the horizontal and vertical expansion
     * @return the new rectangle
     * @see #grow(Bounds, double, double)
     */
    public static Bounds grow(Bounds r, double hv) {
        return grow(r, hv, hv);

    }

    public static Bounds union(Bounds a, Bounds... bs) {
        double minx = a.getMinX();
        double miny = a.getMinY();
        double maxx = a.getMaxX();
        double maxy = a.getMaxY();

        for (Bounds b : bs) {
            minx = Math.min(minx, b.getMinX());
            miny = Math.min(miny, b.getMinY());
            maxx = Math.max(maxx, b.getMaxX());
            maxy = Math.max(maxy, b.getMaxY());
        }
        return new BoundingBox(minx, miny, maxx - minx, maxy - miny);
    }

    /**
     * Returns true if the given bounds are finite.
     *
     * @param bounds the bounds
     * @return true if finiite
     */
    public static boolean isFinite(Bounds bounds) {
        return Double.isFinite(bounds.getMinX())
                && Double.isFinite(bounds.getMinY())
                && Double.isFinite(bounds.getWidth())
                && Double.isFinite(bounds.getHeight());
    }

    public static Bounds intersection(Bounds a, Bounds b) {
        double minx = Math.max(a.getMinX(), b.getMinX());
        double miny = Math.max(a.getMinY(), b.getMinY());
        double maxx = Math.min(a.getMaxX(), b.getMaxX());
        double maxy = Math.min(a.getMaxY(), b.getMaxY());
        return new BoundingBox(minx, miny, maxx - minx, maxy - miny);
    }

    private static Rectangle2D add(Rectangle2D r, double newx, double newy) {
        double x1 = Math.min(r.getMinX(), newx);
        double x2 = Math.max(r.getMaxX(), newx);
        double y1 = Math.min(r.getMinY(), newy);
        double y2 = Math.max(r.getMaxY(), newy);
        return new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
    }

    public static Point2D center(java.awt.geom.Rectangle2D r) {
        return new Point2D(r.getCenterX(), r.getCenterY());
    }

    /**
     * Calculate the center of the given bounds
     *
     * @param r the bounds
     * @return the center
     */
    public static Point2D center(Rectangle2D r) {
        return new Point2D(r.getMinX() + r.getWidth() * 0.5, r.getMinY()
                + r.getHeight() * 0.5);
    }

    public static Point2D east(Rectangle2D r) {
        return new Point2D(r.getMinX() + r.getWidth(), r.getMinY()
                + r.getHeight() / 2);
    }

    public static Point2D north(Rectangle2D r) {
        return new Point2D(r.getMinX() + r.getWidth() * 0.5, r.getMinY());
    }

    /**
     * This method computes a binary OR of the appropriate mask values
     * indicating, for each side of Rectangle2D r1, whether or not the
     * Rectangle2D r2 is on the same side of the edge as the rest of this
     * Rectangle2D.
     *
     * @param r1 rectangle 1
     * @param r2 rectangle 2
     * @return the logical OR of all appropriate out codes OUT_RIGHT, OUT_LEFT,
     * OUT_BOTTOM, OUT_TOP.
     */
    public static int outcode(Rectangle2D r1, Rectangle2D r2) {
        int outcode = 0;

        if (r2.getMinX() > r1.getMinX() + r1.getWidth()) {
            outcode = Rectangles.OUT_RIGHT;
        } else if (r2.getMinX() + r2.getWidth() < r1.getMinX()) {
            outcode = Rectangles.OUT_LEFT;
        }

        if (r2.getMinY() > r1.getMinY() + r1.getHeight()) {
            outcode |= Rectangles.OUT_BOTTOM;
        } else if (r2.getMinY() + r2.getHeight() < r1.getMinY()) {
            outcode |= Rectangles.OUT_TOP;
        }

        return outcode;
    }

    public static Point2D south(Rectangle2D r) {
        return new Point2D(r.getMinX() + r.getWidth() * 0.5, r.getMinY()
                + r.getHeight());
    }

    static BoundingBox subtractInsets(Bounds b, Insets i) {
        return new BoundingBox(
                b.getMinX() + i.getLeft(),
                b.getMinY() + i.getTop(),
                b.getWidth() - i.getLeft() - i.getRight(),
                b.getHeight() - i.getTop() - i.getBottom()
        );
    }

    /**
     * Returns true if the widht or the height is less or equal 0.
     *
     * @param b a rectangle
     * @return true if empty
     */
    public static boolean isEmpty(Rectangle2D b) {
        return b.getWidth() <= 0 || b.getHeight() <= 0;
    }

    public static String toString(@Nullable Bounds b) {
        return b == null ? "null" : b.getMinX() + "," + b.getMinY() + "," + b.getWidth() + "," + b.getHeight();
    }

    /**
     * Returns true, if rectangle 1 contains rectangle 2.
     * <p>
     * This method is similar to Rectangle2D.contains, but also returns true,
     * when rectangle1 contains rectangle2 and either or both of them are empty.
     *
     * @param r1 Rectangle2D 1.
     * @param r2 Rectangle2D 2.
     * @return true if r1 contains r2.
     */
    public static boolean contains(Rectangle2D r1, Rectangle2D r2) {
        return (r2.getMinX() >= r1.getMinX()
                && r2.getMinY() >= r1.getMinY()
                && (r2.getMinX() + max(0, r2.getWidth())) <= r1.getMinX()
                + max(0, r1.getWidth())
                && (r2.getMinY() + max(0, r2.getHeight())) <= r1.getMinY()
                + max(0, r1.getHeight()));
    }

    /**
     * Resizes the <code>Rectangle2D</code> both horizontally and vertically.
     * <p>
     * This method returns a new <code>Rectangle2D</code> so that it is
     * <code>h</code> units larger on both the left and right side, and
     * <code>v</code> units larger at both the top and bottom.
     * <p>
     * The new <code>Rectangle2D</code> has (<code>x&nbsp;-&nbsp;h</code>,
     * <code>y&nbsp;-&nbsp;v</code>) as its top-left corner, a width of
     * <code>width</code>&nbsp;<code>+</code>&nbsp;<code>2h</code>, and a height
     * of <code>height</code>&nbsp;<code>+</code>&nbsp;<code>2v</code>.
     * <p>
     * If negative values are supplied for <code>h</code> and <code>v</code>,
     * the size of the <code>Rectangle2D</code> decreases accordingly. The
     * <code>grow</code> method does not check whether the resulting values of
     * <code>width</code> and <code>height</code> are non-negative.
     *
     * @param r the rectangle
     * @param h the horizontal expansion
     * @param v the vertical expansion
     * @return the new rectangle
     */
    public static Rectangle2D grow(Rectangle2D r, double h, double v) {
        return new Rectangle2D(
                r.getMinX() - h,
                r.getMinY() - v,
                r.getWidth() + h * 2d,
                r.getHeight() + v * 2d);
    }

    public static String toString(@Nullable Rectangle2D b) {
        return b == null ? "null" : b.getMinX() + "," + b.getMinY() + "," + b.getWidth() + "," + b.getHeight();
    }

    public static Point2D west(Rectangle2D r) {
        return new Point2D(r.getMinX(), r.getMinY() + r.getHeight() / 2);
    }
}
