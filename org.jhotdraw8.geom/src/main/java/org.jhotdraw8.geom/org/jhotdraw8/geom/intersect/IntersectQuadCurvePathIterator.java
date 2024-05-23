/*
 * @(#)Intersectquadratic bezier curvePathIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.Rectangles;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public class IntersectQuadCurvePathIterator {
    private IntersectQuadCurvePathIterator() {
    }


    /**
     * Intersects the given quadratic Bézier curve with the given path iterator.
     * <p>
     * This method can produce the following {@link IntersectionStatus} codes:
     * <dl>
     *     <dt>{@link IntersectionStatus#INTERSECTION}</dt><dd>
     *         The quadratic Bézier curve intersects with a segment of the path within the
     *         given tolerance radius.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_INSIDE}</dt><dd>
     *         The quadratic Bézier curve lies inside the path.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_OUTSIDE}</dt><dd>
     *         The quadratic Bézier curve lies outside the path.
     *     </dd>
     * </dl>
     *
     * @param a0x the x-coordinate of control point 0
     * @param a0y the y-coordinate of control point 0
     * @param a1x the x-coordinate of control point 1
     * @param a1y the y-coordinate of control point 1
     * @param a2x the x-coordinate of control point 2
     * @param a2y the y-coordinate of control point 2
     * @param pit the path iterator
     * @return the intersection result
     */
    public static IntersectionResultEx intersectQuadCurvePathIteratorEx(double a0x, double a0y, double a1x, double a1y, double a2x, double a2y, PathIterator pit) {
        List<IntersectionPointEx> lineIntersections = new ArrayList<>();
        List<IntersectionPointEx> insideIntersections = new ArrayList<>();
        final double[] seg = new double[6];
        double firstx = 0, firsty = 0;
        double lastx = 0, lasty = 0;
        double x, y;
        int clockwiseCrossingsSum = 0;
        int counterClockwiseCrossingsSum = 0;
        int clockwiseCrossings = 0;
        int counterClockwiseCrossings = 0;
        int segment = 0;
        int windingRule = pit.getWindingRule();


        for (; !pit.isDone(); pit.next()) {
            IntersectionResultEx boundaryCheck;
            IntersectionResultEx rayCheck;
            int type = pit.currentSegment(seg);
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    boundaryCheck = IntersectQuadCurveLine.intersectQuadCurveLineEx(a0x, a0y, a1x, a1y, a2x, a2y, lastx, lasty, firstx, firsty);
                    rayCheck = IntersectRayLine.intersectRayLineEx(a0x, a0y, 1, 0, Double.MAX_VALUE, lastx, lasty, firstx, firsty, Rectangles.REAL_THRESHOLD);
                    break;
                case PathIterator.SEG_CUBICTO:
                    x = seg[4];
                    y = seg[5];
                    boundaryCheck = IntersectQuadCurveCubicCurve.intersectQuadCurveCubicCurveEx(a0x, a0y, a1x, a1y, a2x, a2y, lastx, lasty, seg[0], seg[1], seg[2], seg[3], x, y);
                    rayCheck = IntersectRayCubicCurve.intersectRayCubicCurveEx(a0x, a0y, 1, 0, Double.MAX_VALUE, lastx, lasty, seg[0], seg[1], seg[2], seg[3], x, y, Rectangles.REAL_THRESHOLD);
                    lastx = x;
                    lasty = y;
                    break;
                case PathIterator.SEG_LINETO:
                    x = seg[0];
                    y = seg[1];
                    boundaryCheck = IntersectQuadCurveLine.intersectQuadCurveLineEx(a0x, a0y, a1x, a1y, a2x, a2y, lastx, lasty, x, y);
                    rayCheck = IntersectRayLine.intersectRayLineEx(a0x, a0y, 1, 0, Double.MAX_VALUE, lastx, lasty, x, y, Rectangles.REAL_THRESHOLD);
                    lastx = x;
                    lasty = y;
                    break;
                case PathIterator.SEG_MOVETO:
                    lastx = firstx = seg[0];
                    lasty = firsty = seg[1];
                    boundaryCheck = null;
                    rayCheck = null;
                    break;
                case PathIterator.SEG_QUADTO:
                    x = seg[2];
                    y = seg[3];
                    boundaryCheck = IntersectQuadCurveQuadCurve.intersectQuadCurveQuadCurveEx(a0x, a0y, a1x, a1y, a2x, a2y, lastx, lasty, seg[0], seg[1], x, y);
                    rayCheck = IntersectRayQuadCurve.intersectRayQuadCurveEx(a0x, a0y, 1, 0, Double.MAX_VALUE,
                            lastx, lasty, seg[0], seg[1], x, y, Rectangles.REAL_THRESHOLD);
                    lastx = x;
                    lasty = y;
                    break;
                default:
                    boundaryCheck = null;
                    rayCheck = null;
                    break;
            }

            if (boundaryCheck != null && boundaryCheck.getStatus() == IntersectionStatus.INTERSECTION) {
                for (var isect : boundaryCheck.intersections()) {
                    lineIntersections.add(isect.withSegmentB(segment));
                }
            }
            if (rayCheck != null && rayCheck.getStatus() == IntersectionStatus.INTERSECTION) {
                for (IntersectionPointEx ip : rayCheck.intersections()) {
                    double ty = ip.getDerivativeB().getY();
                    if (Points.almostZero(ty)) {
                        // intersection point is tangential to ray - no crossing
                    } else if (ty > 0) {
                        clockwiseCrossings++;
                    } else {
                        counterClockwiseCrossings++;
                    }
                }
            }
            switch (type) {
                case PathIterator.SEG_CLOSE -> {
                    clockwiseCrossingsSum += clockwiseCrossings;
                    counterClockwiseCrossingsSum += counterClockwiseCrossings;
                    clockwiseCrossings = counterClockwiseCrossings = 0;
                    if (windingRule == PathIterator.WIND_EVEN_ODD) {
                        if ((clockwiseCrossingsSum + counterClockwiseCrossingsSum) % 2 == 1) {
                            insideIntersections.add(new IntersectionPointEx(a0x, a0y, 0, 0, 0, 0, 0, 0, 0, segment));
                        }
                    } else if (windingRule == PathIterator.WIND_NON_ZERO) {
                        if (clockwiseCrossingsSum != counterClockwiseCrossingsSum) {
                            insideIntersections.add(new IntersectionPointEx(a0x, a0y, 0, 0, 0, 0, 0, 0, 0, segment));
                        }
                    }
                }
                case PathIterator.SEG_MOVETO -> clockwiseCrossings = counterClockwiseCrossings = 0;
            }
        }

        if (!lineIntersections.isEmpty()) {
            return new IntersectionResultEx(lineIntersections);
        }
        if (!insideIntersections.isEmpty()) {
            return new IntersectionResultEx(IntersectionStatus.NO_INTERSECTION_INSIDE, insideIntersections);
        }
        return new IntersectionResultEx(List.of());
    }
}
