/*
 * @(#)IntersectCircleQuadCurve.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;


import java.awt.geom.Point2D;

public class IntersectCircleQuadCurve {
    private IntersectCircleQuadCurve() {
    }

    /**
     * Computes the intersection between quadratic bezier curve 'p' and the
     * given circle.
     *
     * @param p0 control point P0 of 'p'
     * @param p1 control point P1 of 'p'
     * @param p2 control point P3 of 'p'
     * @param c  the center of the circle
     * @param r  the radius of the circle
     * @return the computed result
     */
    public static IntersectionResult intersectQuadCurveCircle(Point2D p0, Point2D p1, Point2D p2, Point2D c, double r) {
        return IntersectEllipseQuadCurve.intersectQuadCurveEllipse(p0, p1, p2, c, r, r);
    }

    public static IntersectionResult intersectQuadCurveCircle(
            double x0, double y0, double x1, double y1, double x2, double y2,
            double cx, double cy, double r) {
        return IntersectEllipseQuadCurve.intersectQuadCurveEllipse(new Point2D.Double(x0, y0), new Point2D.Double(x1, y1), new Point2D.Double(x2, y2), new Point2D.Double(cx, cy), r, r);
    }

    public static IntersectionResultEx intersectQuadCurveCircleEx(
            double x0, double y0, double x1, double y1, double x2, double y2,
            double cx, double cy, double r) {
        return IntersectEllipseQuadCurve.intersectQuadCurveEllipseEx(x0, y0, x1, y1, x2, y2, cx, cy, r, r);
    }
}
