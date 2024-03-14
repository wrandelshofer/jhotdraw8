/*
 * @(#)IntersectLinePathIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.geom.Points;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public class IntersectPathIteratorShape {
    private IntersectPathIteratorShape() {
    }


    /**
     * Intersects the given path iterator with the given shape.
     * <p>
     * This method can produce the following {@link IntersectionStatus} codes:
     * <dl>
     *     <dt>{@link IntersectionStatus#INTERSECTION}</dt><dd>
     *         The path iterator intersects with a segment of the shape within the
     *         given tolerance radius.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_COINCIDENT}</dt><dd>
     *         The path iterator coincides with the shape.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_INSIDE}</dt><dd>
     *         The path iterator lies inside the shape.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_OUTSIDE}</dt><dd>
     *         The path iterator lies outside the shape.
     *     </dd>
     * </dl>
     *
     * <p>
     * FIXME this implementation does not work yet
     *
     * @param pit   the path iterator
     * @param shape the shape
     * @return the intersection result
     */
    public static @NonNull IntersectionResultEx intersectPathIteratorShapeEx(@NonNull PathIterator pit, @NonNull Shape shape) {
        List<IntersectionPointEx> lineIntersections = new ArrayList<>();
        List<IntersectionPointEx> insideIntersections = new ArrayList<>();

        double firstX = 0, firstY = 0;
        double lastX = 0, lastY = 0;
        int segment = 0;
        int clockwiseCrossingsSum = 0;
        int counterClockwiseCrossingsSum = 0;
        int clockwiseCrossings = 0;
        int counterClockwiseCrossings = 0;
        int windingRule = pit.getWindingRule();

        double[] coords = new double[6];
        for (; !pit.isDone(); pit.next()) {
            IntersectionResultEx boundaryCheck;
            IntersectionResultEx rayCheck;
            int type = pit.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_CLOSE -> {
                    boundaryCheck = IntersectLinePathIterator.intersectLinePathIteratorEx(lastX, lastY, firstX, firstY, shape.getPathIterator(null));
                    rayCheck = IntersectRayPathIterator.intersectRayPathIteratorEx(coords[0], coords[1], 1, 0, shape.getPathIterator(null));
                }
                case PathIterator.SEG_CUBICTO -> {
                    boundaryCheck = IntersectCubicCurvePathIterator.intersectCubicCurvePathIteratorEx(lastX, lastY, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], shape.getPathIterator(null));
                    rayCheck = IntersectRayPathIterator.intersectRayPathIteratorEx(coords[0], coords[1], 1, 0, shape.getPathIterator(null));
                    lastX = coords[2];
                    lastY = coords[3];
                }
                case PathIterator.SEG_LINETO -> {
                    boundaryCheck = IntersectLinePathIterator.intersectLinePathIteratorEx(lastX, lastY, coords[0], coords[1], shape.getPathIterator(null));
                    rayCheck = IntersectRayPathIterator.intersectRayPathIteratorEx(coords[0], coords[1], 1, 0, shape.getPathIterator(null));
                    lastX = coords[2];
                    lastY = coords[3];
                }
                case PathIterator.SEG_MOVETO -> {
                    lastX = firstX = coords[0];
                    lastY = firstY = coords[1];
                    boundaryCheck = null;
                    rayCheck = null;
                }
                case PathIterator.SEG_QUADTO -> {
                    boundaryCheck = IntersectQuadCurvePathIterator.intersectQuadCurvePathIteratorEx(lastX, lastY, coords[0], coords[1], coords[2], coords[3], shape.getPathIterator(null));
                    rayCheck = IntersectRayPathIterator.intersectRayPathIteratorEx(coords[0], coords[1], 1, 0, shape.getPathIterator(null));
                    lastX = coords[2];
                    lastY = coords[3];
                }
                default -> {
                    boundaryCheck = null;
                    rayCheck = null;
                }
            }

            if (boundaryCheck != null && boundaryCheck.getStatus() == IntersectionStatus.INTERSECTION) {
                for (var isect : boundaryCheck.intersections()) {
                    lineIntersections.add(isect.withSegment2(segment));
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
                case PathIterator.SEG_CLOSE:
                    clockwiseCrossingsSum += clockwiseCrossings;
                    counterClockwiseCrossingsSum += counterClockwiseCrossings;
                    clockwiseCrossings = counterClockwiseCrossings = 0;
                    if (windingRule == PathIterator.WIND_EVEN_ODD) {
                        if ((clockwiseCrossingsSum + counterClockwiseCrossingsSum) % 2 == 1) {
                            insideIntersections.add(new IntersectionPointEx(coords[0], coords[1], 0, 0, 0, 0, 0, 0, 0, segment));
                        }
                    } else if (windingRule == PathIterator.WIND_NON_ZERO) {
                        if (clockwiseCrossingsSum != counterClockwiseCrossingsSum) {
                            insideIntersections.add(new IntersectionPointEx(coords[0], coords[1], 0, 0, 0, 0, 0, 0, 0, segment));
                        }
                    }
                    break;
                case PathIterator.SEG_MOVETO:
                    clockwiseCrossings = counterClockwiseCrossings = 0;
                    break;
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
