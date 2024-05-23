/*
 * @(#)IntersectLineRectangle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class IntersectLineRectangle {
    private IntersectLineRectangle() {
    }

    /**
     * Computes the intersection between a line and a rectangle.
     * <p>
     * The intersection will contain the parameters 't1' of the line in range
     * [0,1].
     *
     * @param a0 point 0 of the line
     * @param a1 point 1 of the line
     * @param r0 corner point 0 of the rectangle
     * @param r1 corner point 1 of the rectangle
     * @return computed intersection
     */
    public static IntersectionResultEx intersectLineRectangleEx(Point2D a0, Point2D a1, Point2D r0, Point2D r1) {
        return IntersectAABBLine.intersectLineAABBEx(a0, a1,
                Math.min(r0.getX(), r1.getX()),
                Math.min(r0.getY(), r1.getY()),
                Math.max(r0.getX(), r1.getX()),
                Math.max(r0.getY(), r1.getY()));
    }

    public static IntersectionResultEx intersectLineRectangleEx(Point2D a0, Point2D a1, Rectangle2D.Double r) {
        return IntersectAABBLine.intersectLineAABBEx(a0, a1, r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY());
    }

    public static IntersectionResultEx intersectRectangleLineEx(Rectangle2D.Double r, Point2D a0, Point2D a1) {
        return IntersectAABBLine.intersectAABBLineEx(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY(), a0, a1);
    }
}
