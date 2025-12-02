/*
 * @(#)IntersectCubicCurveLine.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.Points2D;
import org.jhotdraw8.geom.Polynomial;
import org.jhotdraw8.geom.Scalars;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.jhotdraw8.geom.Lines.lerp;

public class IntersectCubicCurveLine {
    private IntersectCubicCurveLine() {
    }

    public static IntersectionResult intersectCubicCurveLine(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, double a3x, double a3y,
            double b0x, double b0y, double b1x, double b1y,
            double epsilon) {
        return intersectCubicCurveLine(new Point2D.Double(a0x, a0y), new Point2D.Double(a1x, a1y), new Point2D.Double(a2x, a2y), new Point2D.Double(a3x, a3y),
                new Point2D.Double(b0x, b0y), new Point2D.Double(b1x, b1y), epsilon);
    }

    /**
     * Computes the intersection between cubic bezier curve 'p' and the line
     * 'a'.
     *
     * @param a0 control point P0 of 'p'
     * @param a1 control point P1 of 'p'
     * @param a2 control point P2 of 'p'
     * @param a3 control point P3 of 'p'
     * @param b0 point 0 of 'a'
     * @param b1 point 1 of 'a'
     * @return the computed intersection
     */
    public static IntersectionResult intersectCubicCurveLine(Point2D a0, Point2D a1, Point2D a2, Point2D a3, Point2D b0, Point2D b1) {
        return intersectCubicCurveLine(a0, a1, a2, a3, b0, b1, Scalars.REAL_THRESHOLD);
    }

    /**
     * @param p0
     * @param p1
     * @param p2
     * @param p3
     * @param a0
     * @param a1
     * @param epsilon
     * @return
     */
    public static IntersectionResult intersectCubicCurveLine(Point2D p0, Point2D p1, Point2D p2, Point2D p3,
                                                             Point2D a0, Point2D a1, double epsilon) {
        final Point2D.Double topLeft = Intersections.topLeft(a0, a1); // used to determine if point is on line segment
        final Point2D.Double bottomRight = Intersections.bottomRight(a0, a1); // used to determine if point is on line segment
        List<IntersectionPoint> result = new ArrayList<>();

        // Start with Bezier using Bernstein polynomials for weighting functions:
        //     (1-t^3)P0 + 3t(1-t)^2P1 + 3t^2(1-t)P2 + t^3P3
        //
        // Expand and collect terms to form linear combinations of original Bezier
        // controls.  This ends up with a vector cubic in t:
        //     (-P0+3P1-3P2+P3)t^3 + (3P0-6P1+3P2)t^2 + (-3P0+3P1)t + P0
        //             /\                  /\                /\       /\
        //             ||                  ||                ||       ||
        //             c3                  c2                c1       c0
        // Calculate the coefficients
        final Point2D c3, c2, c1, c0;   // coefficients of cubic
        c3 = Points2D.sum(Points2D.multiply(p0, -1), Points2D.multiply(p1, 3), Points2D.multiply(p2, -3), p3);
        c2 = Points2D.sum(Points2D.multiply(p0, 3), Points2D.multiply(p1, -6), Points2D.multiply(p2, 3));
        c1 = Points2D.add(Points2D.multiply(p0, -3), Points2D.multiply(p1, 3));
        c0 = p0;

        final double a0x, a0y, a1x, a1y;
        a0y = a0.getY();
        a1y = a1.getY();
        a1x = a1.getX();
        a0x = a0.getX();

        // Convert line to normal form: ax + by + c = 0
        // Find normal to line: negative inverse of original line's slope
        final Point2D.Double n;                // normal for normal form of line
        n = new Point2D.Double(a0y - a1y, a1x - a0x);

        // Determine new c coefficient
        final double cl;               // c coefficient for normal form of line
        cl = a0x * a1y - a1x * a0y;

        // ?Rotate each cubic coefficient using line for new coordinate system?
        // Find roots of rotated cubic
        double[] roots = new Polynomial(
                Points2D.dotProduct(n, c3),
                Points2D.dotProduct(n, c2),
                Points2D.dotProduct(n, c1),
                Points2D.dotProduct(n, c0) + cl
        ).getRoots();

        // Any roots in closed interval [0,1] are intersections on Bezier, but
        // might not be on the line segment.
        // Find intersections and calculate point coordinates
        IntersectionStatus status = IntersectionStatus.NO_INTERSECTION;
        for (final double t : roots) {
            if (0 <= t && t <= 1) {
                // We're within the Bezier curve
                // Find point on Bezier
                final Point2D.Double p5, p6, p7, p8, p9, p10;
                p5 = lerp(p0, p1, t);
                p6 = lerp(p1, p2, t);
                p7 = lerp(p2, p3, t);
                p8 = lerp(p5, p6, t);
                p9 = lerp(p6, p7, t);
                p10 = lerp(p8, p9, t);

                // See if point is on line segment
                // Had to make special cases for vertical and horizontal lines due
                // to slight errors in calculation of p10
                if (Scalars.almostEqual(a0x, a1x, epsilon)) {
                    if (topLeft.getY() <= p10.getY() && p10.getY() <= bottomRight.getY()) {
                        status = IntersectionStatus.INTERSECTION;
                        result.add(new IntersectionPoint(p10, t));
                    }
                } else if (Scalars.almostEqual(a0y, a1y, epsilon)) {
                    if (topLeft.getX() <= p10.getX() && p10.getX() <= bottomRight.getX()) {
                        status = IntersectionStatus.INTERSECTION;
                        result.add(new IntersectionPoint(p10, t));
                    }
                } else if (Intersections.gte(p10, topLeft) && Intersections.lte(p10, bottomRight)) {
                    status = IntersectionStatus.INTERSECTION;
                    result.add(new IntersectionPoint(p10, t));
                }
            }
        }

        return new IntersectionResult(status, result);
    }


    public static IntersectionResultEx intersectCubicCurveLineEx(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, double a3x, double a3y,
            double b0x, double b0y, double b1x, double b1y,
            double epsilon) {
        IntersectionResult result = intersectCubicCurveLine(
                a0x, a0y, a1x, a1y, a2x, a2y, a3x, a3y,
                b0x, b0y, b1x, b1y,
                epsilon);
        ArrayList<IntersectionPointEx> list = new ArrayList<>();
        for (IntersectionPoint ip : result.intersections()) {
            double x = ip.getX();
            double y = ip.getY();
            PointAndDerivative pdA = CubicCurves.eval(a0x, a0y, a1x, a1y, a2x, a2y, a3x, a3y, ip.argumentA());
            list.add(new IntersectionPointEx(
                    x, y,
                    ip.argumentA(), pdA.dx(), pdA.dy(),
                    IntersectLinePoint.argumentOnLine(b0x, b0y, b1x, b1y, x, y), b1x - b0x, b1y - b0y
            ));
        }

        return new IntersectionResultEx(result.getStatus(), list);
    }

    public static IntersectionResultEx intersectCubicCurveLineEx(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, double a3x, double a3y,
            double b0x, double b0y, double b1x, double b1y) {
        return intersectCubicCurveLineEx(a0x, a0y, a1x, a1y, a2x, a2y, a3x, a3y, b0x, b0y, b1x, b1y, Scalars.REAL_THRESHOLD);
    }
}
