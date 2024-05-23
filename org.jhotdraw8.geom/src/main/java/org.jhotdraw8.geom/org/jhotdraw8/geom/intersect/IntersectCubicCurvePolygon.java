/*
 * @(#)IntersectCubicCurvePolygon.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IntersectCubicCurvePolygon {
    private IntersectCubicCurvePolygon() {
    }

    /**
     * Computes the intersection between cubic bezier curve 'p' and the given
     * closed polygon.
     *
     * @param p0     control point P0 of 'p'
     * @param p1     control point P1 of 'p'
     * @param p2     control point P2 of 'p'
     * @param p3     control point P3 of 'p'
     * @param points the points of the polygon
     * @return the computed intersection
     */
    public static IntersectionResult intersectCubicCurvePolygon(Point2D p0, Point2D p1, Point2D p2, Point2D p3, List<Point2D.Double> points) {
        List<IntersectionPoint> result = new ArrayList<>();
        int length = points.size();

        for (int i = 0; i < length; i++) {
            Point2D.Double a1 = points.get(i);
            Point2D.Double a2 = points.get((i + 1) % length);
            IntersectionResult inter = IntersectCubicCurveLine.intersectCubicCurveLine(p0, p1, p2, p3, a1, a2);

            result.addAll(inter.intersections().asList());
        }

        return new IntersectionResult(
                result.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION,
                result);
    }
}
