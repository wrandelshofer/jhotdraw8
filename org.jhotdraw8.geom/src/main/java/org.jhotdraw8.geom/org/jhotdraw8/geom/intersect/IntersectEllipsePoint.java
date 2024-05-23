/*
 * @(#)IntersectEllipsePoint.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.Rectangles;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class IntersectEllipsePoint {
    private IntersectEllipsePoint() {
    }

    /**
     * Computes the intersection between a point and an ellipse.
     *
     * @param point  the point
     * @param center the center of the ellipse
     * @param rx     the x-radius of ellipse
     * @param ry     the y-radius of ellipse
     * @return computed intersection. Status can be{@link IntersectionStatus#INTERSECTION},
     * Status#NO_INTERSECTION_INSIDE or Status#NO_INTERSECTION_OUTSIDE}.
     */
    public static IntersectionResult intersectPointEllipse(Point2D point, Point2D center, double rx, double ry) {
        List<IntersectionPoint> result = new ArrayList<>();

        double px = point.getX();
        double py = point.getY();
        double cx = center.getX();
        double cy = center.getY();

        double det = (px - cx) * (px - cx) / (rx * rx) + (py - cy) * (py - cy) / (ry * ry);
        IntersectionStatus status;
        if (abs(det) - 1 >= Rectangles.REAL_THRESHOLD) {
            status = IntersectionStatus.INTERSECTION;
            result.add(new IntersectionPoint(new Point2D.Double(px, py), 0.0));
        } else if (det < 1) {
            status = IntersectionStatus.NO_INTERSECTION_INSIDE;
        } else {
            status = IntersectionStatus.NO_INTERSECTION_OUTSIDE;
        }

        return new IntersectionResult(status, result);
    }
}
