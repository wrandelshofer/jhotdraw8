/*
 * @(#)IntersectLinePolygon.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IntersectLinePolygon {
    private IntersectLinePolygon() {
    }

    /**
     * Computes the intersection between a line and a polygon.
     * <p>
     * The intersection will contain the parameters 't1' of the line in range
     * [0,1].
     *
     * @param a0     point 0 of the line
     * @param a1     point 1 of the line
     * @param points the points of the polygon
     * @return computed intersection
     */
    public static IntersectionResultEx intersectLinePolygonEx(Point2D a0, Point2D a1, List<Point2D.Double> points) {
        List<IntersectionPointEx> result = new ArrayList<>();
        IntersectionStatus status = IntersectionStatus.NO_INTERSECTION;
        int length = points.size();

        for (int i = 0; i < length; i++) {
            Point2D.Double b0 = points.get(i);
            Point2D.Double b1 = points.get((i + 1) % length);
            IntersectionResultEx inter = IntersectLineLine.intersectLineLineEx(a0, a1, b0, b1);

            result.addAll(inter.intersections().asList());
        }

        return new IntersectionResultEx(result);
    }
}
