/*
 * @(#)IntersectQuadCurveRectangle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IntersectQuadCurveRectangle {
    /**
     * Don't let anyone instantiate this class.
     */
    private IntersectQuadCurveRectangle() {
    }

    /**
     * Computes the intersection between quadratic bezier curve 'p' and the
     * provided rectangle.
     *
     * @param p0 control point P0 of 'p'
     * @param p1 control point P1 of 'p'
     * @param p2 control point P2 of 'p'
     * @param r0 corner point 0 of the rectangle
     * @param r1 corner point 1 of the rectangle
     * @return the computed intersection
     */
    public static @NonNull IntersectionResult intersectQuadCurveRectangle(@NonNull Point2D p0, @NonNull Point2D p1, @NonNull Point2D p2, @NonNull Point2D r0, @NonNull Point2D r1) {
        final Point2D.Double topLeft, bottomRight, topRight, bottomLeft;
        topLeft = Intersections.topLeft(r0, r1);
        bottomRight = Intersections.bottomRight(r0, r1);
        topRight = new Point2D.Double(bottomRight.getX(), topLeft.getY());
        bottomLeft = new Point2D.Double(topLeft.getX(), bottomRight.getY());

        final IntersectionResult inter1, inter2, inter3, inter4;
        inter1 = IntersectQuadCurveLine.intersectQuadCurveLine(p0, p1, p2, topLeft, topRight);
        inter2 = IntersectQuadCurveLine.intersectQuadCurveLine(p0, p1, p2, topRight, bottomRight);
        inter3 = IntersectQuadCurveLine.intersectQuadCurveLine(p0, p1, p2, bottomRight, bottomLeft);
        inter4 = IntersectQuadCurveLine.intersectQuadCurveLine(p0, p1, p2, bottomLeft, topLeft);

        final List<IntersectionPoint> result = new ArrayList<>();
        result.addAll(inter1.intersections().asList());
        result.addAll(inter2.intersections().asList());
        result.addAll(inter3.intersections().asList());
        result.addAll(inter4.intersections().asList());

        return new IntersectionResult(result);
    }
}
