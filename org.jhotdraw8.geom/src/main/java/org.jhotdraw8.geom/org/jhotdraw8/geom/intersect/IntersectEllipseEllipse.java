/*
 * @(#)IntersectEllipseEllipse.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.geom.Polynomial;
import org.jhotdraw8.geom.Rectangles;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class IntersectEllipseEllipse {
    private IntersectEllipseEllipse() {
    }

    /**
     * Computes the intersection between two ellipses.
     *
     * @param c1  the center of ellipse 1
     * @param rx1 the x-radius of ellipse 1
     * @param ry1 the y-radius of ellipse 1
     * @param c2  the center of ellipse 2
     * @param rx2 the x-radius of ellipse 2
     * @param ry2 the y-radius of ellipse 2
     * @return computed intersection
     */
    public static @NonNull IntersectionResult intersectEllipseEllipse(@NonNull Point2D c1, double rx1, double ry1, @NonNull Point2D c2, double rx2, double ry2) {
        return intersectEllipseEllipse(c1.getX(), c1.getY(), rx1, ry1, c2.getX(), c2.getY(), rx2, ry2);
    }

    /**
     * Computes the intersection between two ellipses.
     *
     * @param cx1 the center of ellipse 1
     * @param cy1 the center of ellipse 1
     * @param rx1 the x-radius of ellipse 1
     * @param ry1 the y-radius of ellipse 1
     * @param cx2 the center of ellipse 2
     * @param cy2 the center of ellipse 2
     * @param rx2 the x-radius of ellipse 2
     * @param ry2 the y-radius of ellipse 2
     * @return computed intersection
     */
    public static @NonNull IntersectionResult intersectEllipseEllipse(double cx1, double cy1, double rx1, double ry1, double cx2, double cy2, double rx2, double ry2) {
        return intersectEllipseEllipse(cx1, cy1, rx1, ry1, cx2, cy2, rx2, ry2, Rectangles.REAL_THRESHOLD);
    }

    /**
     * @param cx1
     * @param cy1
     * @param rx1
     * @param ry1
     * @param cx2
     * @param cy2
     * @param rx2
     * @param ry2
     * @param epsilon
     * @return
     */
    public static @NonNull IntersectionResult intersectEllipseEllipse(double cx1, double cy1, double rx1, double ry1, double cx2, double cy2, double rx2, double ry2, double epsilon) {
        double[] a = {
                ry1 * ry1,
                0,
                rx1 * rx1,
                -2 * ry1 * ry1 * cx1,
                -2 * rx1 * rx1 * cy1,
                ry1 * ry1 * cx1 * cx1 + rx1 * rx1 * cy1 * cy1 - rx1 * rx1 * ry1 * ry1
        };
        double[] b = {
                ry2 * ry2,
                0,
                rx2 * rx2,
                -2 * ry2 * ry2 * cx2,
                -2 * rx2 * rx2 * cy2,
                ry2 * ry2 * cx2 * cx2 + rx2 * rx2 * cy2 * cy2 - rx2 * rx2 * ry2 * ry2
        };

        Polynomial yPoly = Intersections.bezout(a, b);
        double[] yRoots = yPoly.getRoots();
        double norm0 = (a[0] * a[0] + 2 * a[1] * a[1] + a[2] * a[2]) * Intersections.EPSILON;
        double norm1 = (b[0] * b[0] + 2 * b[1] * b[1] + b[2] * b[2]) * Intersections.EPSILON;
        List<IntersectionPoint> result = new ArrayList<>();

        for (double yRoot : yRoots) {
            Polynomial xPoly = new Polynomial(
                    a[0],
                    a[3] + yRoot * a[1],
                    a[5] + yRoot * (a[4] + yRoot * a[2])
            );
            double[] xRoots = xPoly.getRoots();

            for (double xRoot : xRoots) {
                double test = (a[0] * xRoot + a[1] * yRoot + a[3]) * xRoot
                        + (a[2] * yRoot + a[4]) * yRoot + a[5];
                if (Math.abs(test) < norm0) {
                    test = (b[0] * xRoot + b[1] * yRoot + b[3]) * xRoot
                            + (b[2] * yRoot + b[4]) * yRoot + b[5];
                    if (Math.abs(test) < norm1) {
                        // FIXME compute angle in radians
                        result.add(new IntersectionPoint(new Point2D.Double(xRoot, yRoot), Double.NaN));
                    }
                }
            }
        }

        return new IntersectionResult(result.isEmpty() ? IntersectionStatus.NO_INTERSECTION : IntersectionStatus.INTERSECTION,
                result);
    }

    public static IntersectionResultEx intersectEllipseEllipseEx(double cx1, double cy1, double rx1, double ry1, double cx2, double cy2, double rx2, double ry2) {
        return intersectEllipseEllipseEx(cx1, cy1, rx1, ry1, cx2, cy2, rx2, ry2, Rectangles.REAL_THRESHOLD);
    }

    public static IntersectionResultEx intersectEllipseEllipseEx(double cx1, double cy1, double rx1, double ry1, double cx2, double cy2, double rx2, double ry2, double epsilon) {
        IntersectionResult result = intersectEllipseEllipse(cx1, cy1, rx1, ry1, cx2, cy2, rx2, ry2, epsilon);
        @NonNull List<IntersectionPointEx> list = new ArrayList<>();
        for (IntersectionPoint ip : result.intersections()) {
            double px = ip.getX();
            double py = ip.getY();
            list.add(new IntersectionPointEx(px, py,
                    ip.argumentA(), py - cy1, cx1 - px,
                    Double.NaN, py - cy2, cx2 - px// FIXME compute angle in radians
            ));
        }

        return new IntersectionResultEx(result.getStatus(), list);
    }
}
