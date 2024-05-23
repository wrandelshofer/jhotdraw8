/*
 * @(#)IntersectLinePathIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.Rectangles;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class IntersectRayPathIterator {
    private IntersectRayPathIterator() {
    }

    public static IntersectionResultEx intersectRayPathIteratorEx(double aox, double aoy, double adx, double ady, PathIterator pit) {
        return intersectRayPathIteratorEx(aox, aoy, adx, ady, pit, 1.0);
    }

    public static IntersectionResultEx intersectRayPathIteratorEx(Point2D ao, Point2D ad, PathIterator pit, double maxT) {
        return intersectRayPathIteratorEx(ao.getX(), ao.getY(), ad.getX(), ad.getY(), pit, maxT);
    }

    /**
     * Intersects the given ray with the given path iterator.
     * <p>
     * This method can produce the following {@link IntersectionStatus} codes:
     * <dl>
     *     <dt>{@link IntersectionStatus#INTERSECTION}</dt><dd>
     *         The ray intersects with a segment of the path within the
     *         given tolerance radius.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION}</dt><dd>
     *         The ray does not intersect with a segment of the path.
     *     </dd>
     * </dl>
     *
     * @param aox  the x-coordinate of the origin of the ray
     * @param aoy  the y-coordinate of the origin of the ray
     * @param adx  the x-coordinate of the direction of the ray
     * @param ady  the y-coordinate of the direction of the ray
     * @param pit  the path iterator
     * @param maxT the maximal time of the line (1 = entire line)
     * @return the intersection result
     */
    public static IntersectionResultEx intersectRayPathIteratorEx(double aox, double aoy, double adx, double ady, PathIterator pit, double maxT) {
        ImmutableList<IntersectionPointEx> lineIntersections = VectorList.of();
        final double[] coords = new double[6];
        double firstX = 0, firstY = 0;
        double lastX = 0, lastY = 0;
        double x, y;
        int clockwiseCrossingsSum = 0;
        int counterClockwiseCrossingsSum = 0;
        int clockwiseCrossings = 0;
        int counterClockwiseCrossings = 0;
        int segment = 0;
        int windingRule = pit.getWindingRule();


        for (; !pit.isDone(); pit.next()) {
            IntersectionResultEx rayCheck;
            int type = pit.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_CLOSE -> {
                    rayCheck = IntersectRayLine.intersectRayLineEx(aox, aoy, adx, ady, Double.MAX_VALUE, lastX, lastY, firstX, firstY, Rectangles.REAL_THRESHOLD);
                }
                case PathIterator.SEG_CUBICTO -> {
                    x = coords[4];
                    y = coords[5];
                    rayCheck = IntersectRayCubicCurve.intersectRayCubicCurveEx(aox, aoy, adx, ady, Double.MAX_VALUE, lastX, lastY, coords[0], coords[1], coords[2], coords[3], x, y, Rectangles.REAL_THRESHOLD);
                    lastX = x;
                    lastY = y;
                }
                case PathIterator.SEG_LINETO -> {
                    x = coords[0];
                    y = coords[1];
                    rayCheck = IntersectRayLine.intersectRayLineEx(aox, aoy, adx, ady, Double.MAX_VALUE, lastX, lastY, x, y, Rectangles.REAL_THRESHOLD);
                    lastX = x;
                    lastY = y;
                }
                case PathIterator.SEG_MOVETO -> {
                    lastX = firstX = coords[0];
                    lastY = firstY = coords[1];
                    rayCheck = null;
                }
                case PathIterator.SEG_QUADTO -> {
                    x = coords[2];
                    y = coords[3];
                    rayCheck = IntersectRayQuadCurve.intersectRayQuadCurveEx(aox, aoy, adx, ady, Double.MAX_VALUE,
                            lastX, lastY, coords[0], coords[1], x, y, Rectangles.REAL_THRESHOLD);
                    lastX = x;
                    lastY = y;
                }
                default -> {
                    rayCheck = null;
                }
            }

            if (rayCheck != null && rayCheck.getStatus() == IntersectionStatus.INTERSECTION) {
                lineIntersections = lineIntersections.addAll(rayCheck.intersections());
            }
        }
        return new IntersectionResultEx(lineIntersections.toMutable());
    }


    public static IntersectionResultEx intersectRayPathIteratorEx(Point2D a0, Point2D a1, PathIterator pit) {
        IntersectionResultEx i = intersectRayPathIteratorEx(a0, a1, pit, 1.0);
        if (i.getStatus() == IntersectionStatus.INTERSECTION && i.intersections().getFirst().argumentA() > 1) {
            return new IntersectionResultEx(IntersectionStatus.NO_INTERSECTION, new ArrayList<>());
        } else {// FIXME remove intersections with t>1
            return i;
        }
    }
}
