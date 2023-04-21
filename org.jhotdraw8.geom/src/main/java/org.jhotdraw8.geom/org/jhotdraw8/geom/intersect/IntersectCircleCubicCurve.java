/*
 * @(#)IntersectCircleCubicCurve.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.geom.Angles;
import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.Rectangles;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IntersectCircleCubicCurve {
    private IntersectCircleCubicCurve() {
    }

    /**
     * Computes the intersection between cubic bezier curve 'p' and the given
     * circle.
     *
     * @param p0 control point P0 of 'p'
     * @param p1 control point P1 of 'p'
     * @param p2 control point P2 of 'p'
     * @param p3 control point P3 of 'p'
     * @param c  the center of the circle
     * @param r  the radius of the circle
     * @return the computed result
     */
    public static @NonNull IntersectionResult intersectCubicCurveCircle(
            @NonNull Point2D p0, @NonNull Point2D p1, @NonNull Point2D p2, @NonNull Point2D p3,
            @NonNull Point2D c, double r) {
        return IntersectCubicCurveEllipse.intersectCubicCurveEllipse(p0, p1, p2, p3, c, r, r);
    }

    public static @NonNull IntersectionResult intersectCubicCurveCircle(
            double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
            double cx, double cy, double r) {
        return IntersectCubicCurveEllipse.intersectCubicCurveEllipse(x0, y0, x1, y1, x2, y2, x3, y3, cx, cy, r, r);
    }

    public static @NonNull IntersectionResult intersectCubicCurveCircle(
            double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
            double cx, double cy, double r, double epsilon) {
        return IntersectCubicCurveEllipse.intersectCubicCurveEllipse(x0, y0, x1, y1, x2, y2, x3, y3, cx, cy, r, r, epsilon);
    }

    public static IntersectionResultEx intersectCubicCurveCircleEx(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
                                                                   double cx, double cy, double r) {
        return intersectCubicCurveCircleEx(x0, y0, x1, y1, x2, y2, x3, y3, cx, cy, r, Rectangles.REAL_THRESHOLD);
    }

    public static IntersectionResultEx intersectCubicCurveCircleEx(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3,
                                                                   double cx, double cy, double r, double epsilon) {
        IntersectionResult result = intersectCubicCurveCircle(x0, y0, x1, y1, x2, y2, x3, y3, cx, cy, r, epsilon);
        @NonNull List<IntersectionPointEx> list = new ArrayList<>();
        for (IntersectionPoint ip : result.intersections()) {
            double x = ip.getX();
            double y = ip.getY();
            PointAndDerivative pdA = CubicCurves.eval(x0, y0, x1, y1, x2, y2, x3, y3, ip.getArgumentA());
            list.add(new IntersectionPointEx(x, y,
                    ip.getArgumentA(), pdA.dx(), pdA.dy(),
                    Angles.atan2(y - cy, x - cx), y - cy, cx - x));
        }

        return new IntersectionResultEx(result.getStatus(), list);
    }
}
