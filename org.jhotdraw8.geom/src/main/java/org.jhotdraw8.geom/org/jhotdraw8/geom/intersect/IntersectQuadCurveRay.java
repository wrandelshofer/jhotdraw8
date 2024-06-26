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

public class IntersectQuadCurveRay {
    private IntersectQuadCurveRay() {
    }


    /**
     * Computes the intersection between quadratic bezier curve 'p' and the line
     * 'a'.
     * <p>
     * The intersection will contain the parameters 't1' of curve 'a' in range
     * [0,1].
     *
     * @param p0 control point P0 of 'p'
     * @param p1 control point P1 of 'p'
     * @param p2 control point P2 of 'p'
     * @param ao point 0 of 'a'
     * @param ad point 1 of 'a'
     * @return the computed intersection
     */
    public static IntersectionResult intersectQuadCurveRay(Point2D p0, Point2D p1, Point2D p2, Point2D ao, Point2D ad, double maxT) {
        return intersectQuadCurveRay(p0, p1, p2, ao, ad, maxT, Rectangles.REAL_THRESHOLD);
    }

    /**
     * @param p0
     * @param p1
     * @param p2
     * @param ao
     * @param ad
     * @param maxT
     * @param epsilon
     * @return
     */
    public static IntersectionResult intersectQuadCurveRay(Point2D p0, Point2D p1, Point2D p2, Point2D ao, Point2D ad, double maxT,
                                                           double epsilon) {

        // Bezier curve:
        //   (1 - t)²·p0 + 2·(1 - t)·t·p1 + t²·p2 , 0 ≤ t ≤ 1
        //   (p0 - 2·p1 + p2)·t² - 2·(p0 - p1)·t + p0
        //   c2·t² + c1·t + c0
        final Point2D c2, c1, c0;       // coefficients of quadratic
        c2 = Points2D.sum(p0, Points2D.multiply(p1, -2), p2);
        c1 = Points2D.multiply(Points2D.subtract(p0, p1), -2);
        c0 = p0;

        final double a0x, a0y, a1x, a1y;
        a0x = ao.getX();
        a0y = ao.getY();
        a1x = a0x + ad.getX();
        a1y = a0y + ad.getY();

        // Convert line to normal form: a·x + b·y + c = 0
        // Find normal to line: negative inverse of original line's slope
        final Point2D.Double n;                // normal for normal form of line
        n = new Point2D.Double(a0y - a1y, a1x - a0x);

        // Determine new c coefficient
        final double cl;               // c coefficient for normal form of line
        cl = a0x * a1y - a1x * a0y;

        // Transform cubic coefficients to line's coordinate system and find roots
        // of cubic
        final double[] roots = new Polynomial(
                Points2D.dotProduct(n, c2),
                Points2D.dotProduct(n, c1),
                Points2D.dotProduct(n, c0) + cl
        ).getRoots();

        // Any roots in closed interval [0,1] are intersections on Bezier, but
        // might not be on the line segment.
        // Find intersections and calculate point coordinates
        List<IntersectionPoint> result = new ArrayList<>();
        IntersectionStatus status = IntersectionStatus.NO_INTERSECTION;
        for (double t : roots) {
            if (-epsilon <= t && t <= 1 + epsilon) {
                // We're within the Bezier curve
                // Find point on Bezier
                final Point2D.Double p4, p5, p6;
                p4 = lerp(p0, p1, t);
                p5 = lerp(p1, p2, t);
                p6 = lerp(p4, p5, t);

                // See if point is on ray
                double t1 = argumentOnLine(a0x, a0y, a1x, a1y, p6.getX(), p6.getY());
                if (-epsilon <= t1 && t1 <= maxT) {
                    status = IntersectionStatus.INTERSECTION;
                    result.add(new IntersectionPoint(p6, t));
                }
            }
        }

        return new IntersectionResult(status, result);
    }

    public static IntersectionResult intersectQuadCurveRay(
            double ax0, double ay0, double ax1, double ay1, double ax2, double ay2,
            double box, double boy, double bdx, double bdy, double maxT) {
        return intersectQuadCurveRay(ax0, ay0, ax1, ay1, ax2, ay2, box, boy, bdx, bdy, maxT, Rectangles.REAL_THRESHOLD);
    }

    public static IntersectionResult intersectQuadCurveRay(
            double ax0, double ay0, double ax1, double ay1, double ax2, double ay2,
            double box, double boy, double bdx, double bdy, double maxT, double epsilon) {
        return intersectQuadCurveRay(new Point2D.Double(ax0, ay0), new Point2D.Double(ax1, ay1), new Point2D.Double(ax2, ay2),
                new Point2D.Double(box, boy), new Point2D.Double(bdx, bdy), maxT, epsilon);
    }


    public static IntersectionResultEx intersectQuadCurveLineEx(
            double p0x, double p0y, double p1x, double p1y, double p2x, double p2y,
            double aox, double aoy, double adx, double ady
    ) {
        return intersectQuadCurveLineEx(p0x, p0y, p1x, p1y, p2x, p2y, aox, aoy, adx, ady, Rectangles.REAL_THRESHOLD);
    }

    public static IntersectionResultEx intersectQuadCurveLineEx(
            double p0x, double p0y, double p1x, double p1y, double p2x, double p2y,
            double aox, double aoy, double adx, double ady,
            double epsilon) {
        IntersectionResult result = intersectQuadCurveRay(p0x, p0y, p1x, p1y, p2x, p2y, aox, aoy, adx, ady, epsilon);
        ArrayList<IntersectionPointEx> list = new ArrayList<>();
        for (IntersectionPoint ip : result.intersections()) {
            double px = ip.getX();
            double py = ip.getY();
            PointAndDerivative pdA = QuadCurves.eval(p0x, p0y, p1x, p1y, p2x, p2y, ip.argumentA());
            list.add(new IntersectionPointEx(
                    px, py,
                    ip.argumentA(), pdA.dx(), pdA.dy(),
                    IntersectPointRay.projectedPointOnRay(aox, aoy, adx, ady, px, py), adx - aox, ady - aoy
            ));
        }
        return new IntersectionResultEx(result.getStatus(), list);

    }
}
