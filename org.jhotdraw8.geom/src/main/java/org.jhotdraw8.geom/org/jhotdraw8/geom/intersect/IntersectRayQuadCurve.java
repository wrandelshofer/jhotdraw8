/*
 * @(#)IntersectQuadCurveRay.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.Points2D;
import org.jhotdraw8.geom.Polynomial;
import org.jhotdraw8.geom.QuadCurves;
import org.jhotdraw8.geom.Rectangles;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.jhotdraw8.geom.Lines.lerp;
import static org.jhotdraw8.geom.intersect.IntersectLinePoint.argumentOnLine;

public class IntersectRayQuadCurve {
    private IntersectRayQuadCurve() {
    }

    /**
     * Computes the intersection between quadratic bezier curve 'p' and the line
     * 'a'.
     * <p>
     * The intersection will contain the parameters 't' of curve 'a' in range
     * [0,1].
     *
     * @param p0   control point P0 of 'p'
     * @param p1   control point P1 of 'p'
     * @param p2   control point P2 of 'p'
     * @param ao   origin of ray 'a'
     * @param ad   direction of ray 'a'
     * @param maxT maximal parameter value for ray 'a'
     * @return the computed intersection
     */
    public static IntersectionResult intersectRayQuadCurve(Point2D ao, Point2D ad, double maxT, Point2D p0, Point2D p1, Point2D p2) {
        return intersectRayQuadCurve(
                ao.getX(), ao.getY(),
                ad.getX(), ad.getY(), maxT,
                p0.getX(), p0.getY(),
                p1.getX(), p1.getY(),
                p2.getX(), p2.getY());
    }

    public static IntersectionResult intersectRayQuadCurve(double aox, double aoy, double adx, double ady, double maxT,
                                                                    double p0x, double p0y, double p1x, double p1y, double p2x, double p2y) {
        return intersectRayQuadCurve(
                aox, aoy,
                aox, aoy, maxT,
                p0x, p0y,
                p1x, p1y,
                p2x, p2y, Rectangles.REAL_THRESHOLD);
    }

    public static IntersectionResult intersectRayQuadCurve(double aox, double aoy, double adx, double ady, double maxT,
                                                                    double p0x, double p0y, double p1x, double p1y, double p2x, double p2y,
                                                                    double epsilon) {
        /* steps:
         * 1. Rotate the bezier curve so that the line coincides with the x-axis.
         *    This will position the curve in a way that makes it cross the line at points where its y-function is zero.
         * 2. Insert the control points of the rotated bezier curve in the polynomial equation.
         * 3. Find the roots of the polynomial equation.
         */
        double
                a1x = aox + adx,
                a1y = aoy + ady;

        List<IntersectionPoint> result = new ArrayList<>();

        final Point2D.Double p0, p1;
        p0 = new Point2D.Double(p0x, p0y);
        p1 = new Point2D.Double(p1x, p1y);

        final Point2D.Double c2, c1, c0;       // coefficients of quadratic
        c2 = Points2D.add(Points2D.add(p0, Points2D.multiply(p1, -2)), p2x, p2y);
        c1 = Points2D.add(Points2D.multiply(p0, -2), Points2D.multiply(p1, 2));
        c0 = p0;

        // Convert line to normal form: ax + by + c = 0
        // Find normal to line: negative inverse of original line's slope
        final Point2D.Double n;                // normal for normal form of line
        n = new Point2D.Double(aoy - a1y, a1x - aox);

        // Determine new c coefficient
        final double cl;               // c coefficient for normal form of line
        cl = aox * a1y - a1x * aoy;

        // Transform cubic coefficients to line's coordinate system and find roots
        // of cubic
        double[] roots = new Polynomial(
                Points2D.dotProduct(n, c2),
                Points2D.dotProduct(n, c1),
                Points2D.dotProduct(n, c0) + cl
        ).getRoots();
        // Any roots in closed interval [0,1] are intersections on Bezier, but
        // might not be on the line segment.
        // Find intersections and calculate point coordinates
        IntersectionStatus status = IntersectionStatus.NO_INTERSECTION;
        for (double t : roots) {
            if (0 <= t && t <= 1) {
                // We're within the Bezier curve
                // Find point on Bezier
                final Point2D.Double p4, p5, p6;
                p4 = lerp(p0, p1, t);
                p5 = lerp(p1x, p1y, p2x, p2y, t);
                p6 = lerp(p4, p5, t);

                // See if point is on line segment
                double t1 = argumentOnLine(aox, aoy, a1x, a1y, p6.getX(), p6.getY());
                if (-epsilon <= t1 && t1 <= maxT) {
                    status = IntersectionStatus.INTERSECTION;
                    result.add(new IntersectionPoint(p6, t1));
                }
            }
        }

        return new IntersectionResult(status, result);
    }


    public static IntersectionResultEx intersectRayQuadCurveEx(double aox, double aoy, double adx, double ady, double maxT,
                                                               double p0x, double p0y, double p1x, double p1y, double p2x, double p2y) {
        return intersectRayQuadCurveEx(aox, aoy, adx, ady, p0x, maxT, p0y, p1x, p1y, p2x, p2y, Rectangles.REAL_THRESHOLD);
    }

    public static IntersectionResultEx intersectRayQuadCurveEx(double aox, double aoy, double adx, double ady, double maxT,
                                                               double p0x, double p0y, double p1x, double p1y, double p2x, double p2y,
                                                               double epsilon) {
        IntersectionResult result = IntersectQuadCurveRay.intersectQuadCurveRay(p0x, p0y, p1x, p1y, p2x, p2y, aox, aoy, adx, ady, maxT, epsilon);
        ArrayList<IntersectionPointEx> list = new ArrayList<>();
        for (IntersectionPoint ip : result.intersections()) {
            double px = ip.getX();
            double py = ip.getY();
            PointAndDerivative pdA = QuadCurves.eval(p0x, p0y, p1x, p1y, p2x, p2y, ip.argumentA());
            list.add(new IntersectionPointEx(
                    px, py,
                    IntersectPointRay.projectedPointOnRay(aox, aoy, adx, ady, px, py), adx - aox, ady - aoy,
                    ip.argumentA(), pdA.dx(), pdA.dy()
            ));
        }
        return new IntersectionResultEx(result.getStatus(), list);

    }


}
