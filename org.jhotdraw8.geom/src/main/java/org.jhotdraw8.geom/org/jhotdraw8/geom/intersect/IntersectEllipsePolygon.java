/*
 * @(#)IntersectEllipsePolygon.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IntersectEllipsePolygon {
    private IntersectEllipsePolygon() {
    }

    /**
     * Computes the intersection between a circle and a polygon.
     *
     * @param c      the center of the ellipse
     * @param rx     the x-radius of the ellipse
     * @param ry     the y-radius of the ellipse
     * @param points the points of the polygon
     * @return computed intersection
     */
    public static IntersectionResult intersectEllipsePolygon(Point2D c, double rx, double ry, List<Point2D.Double> points) {
        List<IntersectionPoint> result = new ArrayList<>();
        int length = points.size();

        for (int i = 0; i < length; i++) {
            Point2D.Double b1 = points.get(i);
            Point2D.Double b2 = points.get((i + 1) % length);
            IntersectionResult inter = IntersectEllipseLine.intersectEllipseLine(c, rx, ry, b1, b2);

            result.addAll(inter.intersections().asList());
        }

        return new IntersectionResult(result.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION,
                result);
    }
}
