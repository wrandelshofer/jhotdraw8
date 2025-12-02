/*
 * @(#)IntersectPathIteratorPoint.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.Scalars;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public class IntersectPathIteratorPoint {
    private IntersectPathIteratorPoint() {
    }

    /**
     * Intersects the given path iterator with the given point.
     * <p>
     * This method can produce the following {@link IntersectionStatus} codes:
     * <dl>
     *     <dt>{@link IntersectionStatus#INTERSECTION}</dt><dd>
     *         The point intersects with a segment of the path within the
     *         given tolerance radius.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_INSIDE}</dt><dd>
     *         The point lies inside a path segment. The segment returned
     *         by {@link IntersectionPointEx#getSegmentB()} points to the
     *         segment that closes the path segment with
     *         {@link PathIterator#SEG_CLOSE}.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_OUTSIDE}</dt><dd>
     *         The point lies outside the path.
     *     </dd>
     * </dl>
     *
     * @param pit       the path iterator
     * @param px        the x-coordinate of the point
     * @param py        the y-coordinate of the point
     * @param tolerance radius around the point which counts as a hit.
     * @return the intersection
     */
    public static IntersectionResult intersectPathIteratorPoint(PathIterator pit, double px, double py, double tolerance) {
        // FIXME we must take the winding rule into account!
        List<IntersectionPoint> lineIntersections = new ArrayList<>();
        List<IntersectionPoint> insideIntersections = new ArrayList<>();
        final double[] seg = new double[6];
        double firstx = 0, firsty = 0;
        double lastx = 0, lasty = 0;
        double x, y;
        int i = 0;
        int windingRule = pit.getWindingRule();

        // Count clockwise and counterclockwise crossings of a ray
        // starting at px,py going to POSITIVE_INFINITY,py.
        int clockwiseCrossingsSum = 0;
        int counterClockwiseCrossingsSum = 0;
        int clockwiseCrossings = 0;
        int counterClockwiseCrossings = 0;
        int segment = 0;

        for (; !pit.isDone(); pit.next(), i++) {
            IntersectionResult boundaryCheck;
            IntersectionResultEx rayCheck;
            int type = pit.currentSegment(seg);
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    boundaryCheck = IntersectLinePoint.intersectLinePoint(lastx, lasty, firstx, firsty, px, py, tolerance);
                    rayCheck = IntersectRayLine.intersectRayLineEx(px, py, 1, 0, Double.MAX_VALUE, lastx, lasty, firstx, firsty, Scalars.REAL_THRESHOLD);
                    break;
                case PathIterator.SEG_CUBICTO:
                    x = seg[4];
                    y = seg[5];
                    boundaryCheck = IntersectCubicCurvePoint.intersectCubicCurvePoint(lastx, lasty, seg[0], seg[1], seg[2], seg[3], x, y, px, py, tolerance);
                    rayCheck = IntersectRayCubicCurve.intersectRayCubicCurveEx(px, py, 1, 0, Double.MAX_VALUE, lastx, lasty, seg[0], seg[1], seg[2], seg[3], x, y, Scalars.REAL_THRESHOLD);
                    //IntersectCubicCurveRa
                    lastx = x;
                    lasty = y;
                    break;
                case PathIterator.SEG_LINETO:
                    x = seg[0];
                    y = seg[1];
                    boundaryCheck = IntersectLinePoint.intersectLinePoint(lastx, lasty, x, y, px, py, tolerance);
                    rayCheck = IntersectRayLine.intersectRayLineEx(px, py, 1, 0, Double.MAX_VALUE, lastx, lasty, x, y, Scalars.REAL_THRESHOLD);
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
                    boundaryCheck = IntersectPointQuadCurve.intersectQuadCurvePoint(lastx, lasty, seg[0], seg[1], x, y, px, py, tolerance);
                    rayCheck = IntersectRayQuadCurve.intersectRayQuadCurveEx(px, py, 1, 0, Double.MAX_VALUE,
                            lastx, lasty, seg[0], seg[1], x, y, Scalars.REAL_THRESHOLD);
                    lastx = x;
                    lasty = y;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported segment type: " + type);
            }

            if (boundaryCheck != null && boundaryCheck.getStatus() == IntersectionStatus.INTERSECTION) {
                final IntersectionPoint first = boundaryCheck.intersections().getFirst();
                lineIntersections.add(new IntersectionPoint(first.getX(), first.getY(), first.argumentA(), segment));
                break;
            }
            if (rayCheck != null && rayCheck.getStatus() == IntersectionStatus.INTERSECTION) {
                for (IntersectionPointEx ip : rayCheck.intersections()) {
                    double ty = ip.getDerivativeB().getY();
                    if (Scalars.almostZero(ty)) {
                        // intersection point is tangential to ray - no crossing
                    } else if (ty > 0) {
                        clockwiseCrossings++;
                    } else {
                        counterClockwiseCrossings++;
                    }
                }
            }
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    clockwiseCrossingsSum += clockwiseCrossings;
                    counterClockwiseCrossingsSum += counterClockwiseCrossings;
                    clockwiseCrossings = counterClockwiseCrossings = 0;
                    if (windingRule == PathIterator.WIND_EVEN_ODD) {
                        if ((clockwiseCrossingsSum + counterClockwiseCrossingsSum) % 2 == 1) {
                            insideIntersections.add(new IntersectionPoint(px, py, 0, segment));
                        }
                    } else if (windingRule == PathIterator.WIND_NON_ZERO) {
                        if (clockwiseCrossingsSum != counterClockwiseCrossingsSum) {
                            insideIntersections.add(new IntersectionPoint(px, py, 0, segment));
                        }
                    }
                    break;
                case PathIterator.SEG_MOVETO:
                    clockwiseCrossings = counterClockwiseCrossings = 0;
                    break;
            }
            segment++;
        }

        if (!lineIntersections.isEmpty()) {
            return new IntersectionResult(lineIntersections);
        }
        if (!insideIntersections.isEmpty()) {
            return new IntersectionResult(IntersectionStatus.NO_INTERSECTION_INSIDE, insideIntersections);
        }
        return new IntersectionResult(List.of());
    }
}
