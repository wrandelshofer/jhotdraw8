/*
 * @(#)IntersectCubicCurveQuadCurve.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.QuadCurves;
import org.jhotdraw8.geom.Scalars;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IntersectCubicCurveQuadCurve {

    private static final double CURVE_A_B_TOLERANCE = 1e-3;
    private static final double ROOT_X_Y_TOLERANCE = 1e-4;

    private IntersectCubicCurveQuadCurve() {
    }


    public static IntersectionResult intersectCubicCurveQuadCurve(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, double a3x, double a3y,
            double b0x, double b0y, double b1x, double b1y, double b2x, double b2y) {
        return intersectCubicCurveQuadCurve(a0x, a0y, a1x, a1y, a2x, a2y, a3x, a3y, b0x, b0y, b1x, b1y, b2x, b2y, Scalars.REAL_THRESHOLD);
    }

    public static IntersectionResult intersectCubicCurveQuadCurve(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, double a3x, double a3y,
            double b0x, double b0y, double b1x, double b1y, double b2x, double b2y, double epsilon) {
        IntersectionResult resultB = IntersectQuadCurveCubicCurve.intersectQuadCurveCubicCurve(
                new Point2D.Double(b0x, b0y), new Point2D.Double(b1x, b1y), new Point2D.Double(b2x, b2y),
                new Point2D.Double(a0x, a0y), new Point2D.Double(a1x, a1y), new Point2D.Double(a2x, a2y),
                new Point2D.Double(a3x, a3y), epsilon);
        List<IntersectionPoint> list = new ArrayList<>();
        for (IntersectionPoint ip : resultB.intersections()) {
            double x = ip.getX();
            double y = ip.getY();
            IntersectionResult resultA = IntersectCubicCurvePoint.intersectCubicCurvePoint(a0x, a0y, a1x, a1y, a2x, a2y, a3x, a3y, x, y, epsilon);
            list.add(new IntersectionPoint(x, y, resultA.intersections().getFirst().argumentA()));
        }

        return new IntersectionResult(resultB.getStatus(), list);
    }

    public static IntersectionResultEx intersectCubicCurveQuadCurveEx(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, double a3x, double a3y,
            double b0x, double b0y, double b1x, double b1y, double b2x, double b2y) {
        return intersectCubicCurveQuadCurveEx(a0x, a0y, a1x, a1y, a2x, a2y, a3x, a3y, b0x, b0y, b1x, b1y, b2x, b2y, Scalars.REAL_THRESHOLD);
    }

    public static IntersectionResultEx intersectCubicCurveQuadCurveEx(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, double a3x, double a3y,
            double b0x, double b0y, double b1x, double b1y, double b2x, double b2y, double epsilon) {
        IntersectionResult resultB = IntersectQuadCurveCubicCurve.intersectQuadCurveCubicCurve(
                new Point2D.Double(b0x, b0y), new Point2D.Double(b1x, b1y), new Point2D.Double(b2x, b2y),
                new Point2D.Double(a0x, a0y), new Point2D.Double(a1x, a1y), new Point2D.Double(a2x, a2y),
                new Point2D.Double(a3x, a3y), epsilon);
        List<IntersectionPointEx> list = new ArrayList<>();
        for (IntersectionPoint ip : resultB.intersections()) {
            double x = ip.getX();
            double y = ip.getY();
            IntersectionResultEx resultA = IntersectCubicCurvePoint.intersectCubicCurvePointEx(a0x, a0y, a1x, a1y, a2x, a2y, a3x, a3y, x, y, CURVE_A_B_TOLERANCE);
            // resultA should never by empty, but if this happen we rather have no intersection than a crash.
            if (!resultA.intersections().isEmpty()) {
                IntersectionPointEx firstA = resultA.intersections().getFirst();
                list.add(new IntersectionPointEx(ip, firstA.argumentA(), firstA.getDerivativeA(),
                        ip.argumentA(), QuadCurves.eval(b0x, b0y, b1x, b1y, b2x, b2y,
                        ip.argumentA()).getDerivative(Point2D.Double::new)));
            }
        }

        return new IntersectionResultEx(resultB.getStatus(), list);
    }

    public static IntersectionResultEx intersectQuadCurveCubicCurveEx(
            double a0x, double a0y, double a1x, double a1y, double a2x, double a2y,
            double b0x, double b0y, double b1x, double b1y, double b2x, double b2y, double b3x, double b3y, double epsilon) {
        IntersectionResult resultA = IntersectQuadCurveCubicCurve.intersectQuadCurveCubicCurve(
                new Point2D.Double(a0x, a0y), new Point2D.Double(a1x, a1y), new Point2D.Double(a2x, a2y),
                new Point2D.Double(b0x, b0y), new Point2D.Double(b1x, b1y), new Point2D.Double(b2x, b2y),
                new Point2D.Double(b3x, b3y), epsilon);
        List<IntersectionPointEx> list = new ArrayList<>();
        for (IntersectionPoint ip : resultA.intersections()) {
            double x = ip.getX();
            double y = ip.getY();
            IntersectionResultEx resultB = IntersectCubicCurvePoint.intersectCubicCurvePointEx(b0x, b0y, b1x, b1y, b2x, b2y, b3x, b3y, x, y, epsilon);
            IntersectionPointEx firstB = resultB.intersections().getFirst();
            list.add(new IntersectionPointEx(ip,
                    ip.argumentA(), QuadCurves.eval(b0x, b0y, b1x, b1y, b2x, b2y, ip.argumentA()).getDerivative(Point2D.Double::new),
                    firstB.argumentA(), firstB.getDerivativeA()
            ));
        }

        return new IntersectionResultEx(resultA.getStatus(), list);
    }
}
