/*
 * @(#)IntersectLinePathIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;


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
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_INSIDE}</dt><dd>
     *         The path iterator lies inside the shape.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_OUTSIDE}</dt><dd>
     *         The path iterator lies outside the shape.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_INSIDE_AND_OUTSIDE}</dt><dd>
     *         The path iterator lies inside and outside the shape.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION}</dt><dd>
     *         The path iterator does not intersect with the shape because either of the path iterator or the shape is
     *         empty or because the path and the shape are coincident.
     *     </dd>
     * </dl>
     *
     * @param pit   the path iterator
     * @param shape the shape
     * @return the intersection result
     */
    public static IntersectionResultEx intersectPathIteratorShapeEx(PathIterator pit, Shape shape) {
        List<IntersectionPointEx> intersections = new ArrayList<>();
        List<IntersectionPointEx> insideIntersections = new ArrayList<>();

        double firstX = 0, firstY = 0;
        double lastX = 0, lastY = 0;
        int segment = 0;
        IntersectionStatus state = IntersectionStatus.NO_INTERSECTION;

        double[] coords = new double[6];
        for (; !pit.isDone(); pit.next()) {
            IntersectionResultEx segmentResult;
            int type = pit.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_CLOSE -> {
                    segmentResult = IntersectLinePathIterator.intersectLinePathIteratorEx(lastX, lastY, firstX, firstY, shape.getPathIterator(null));
                }
                case PathIterator.SEG_CUBICTO -> {
                    segmentResult = IntersectCubicCurvePathIterator.intersectCubicCurvePathIteratorEx(lastX, lastY, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], shape.getPathIterator(null));
                    lastX = coords[2];
                    lastY = coords[3];
                }
                case PathIterator.SEG_LINETO -> {
                    segmentResult = IntersectLinePathIterator.intersectLinePathIteratorEx(lastX, lastY, coords[0], coords[1], shape.getPathIterator(null));
                    lastX = coords[2];
                    lastY = coords[3];
                }
                case PathIterator.SEG_MOVETO -> {
                    lastX = firstX = coords[0];
                    lastY = firstY = coords[1];
                    segmentResult = null;
                }
                case PathIterator.SEG_QUADTO -> {
                    segmentResult = IntersectQuadCurvePathIterator.intersectQuadCurvePathIteratorEx(lastX, lastY, coords[0], coords[1], coords[2], coords[3], shape.getPathIterator(null));
                    lastX = coords[2];
                    lastY = coords[3];
                }
                default -> {
                    segmentResult = null;
                }
            }

            if (segmentResult != null) {
                switch (segmentResult.getStatus()) {
                    case INTERSECTION -> {
                        for (var isect : segmentResult.intersections()) {
                            intersections.add(isect.withSegmentA(segment));
                        }
                        state = IntersectionStatus.INTERSECTION;
                    }
                    case NO_INTERSECTION_INSIDE -> {
                        switch (state) {
                            case NO_INTERSECTION -> state = IntersectionStatus.NO_INTERSECTION_INSIDE;
                            case NO_INTERSECTION_OUTSIDE ->
                                    state = IntersectionStatus.NO_INTERSECTION_INSIDE_AND_OUTSIDE;
                            default -> {
                            }
                        }
                    }
                    case NO_INTERSECTION_OUTSIDE -> {
                        switch (state) {
                            case NO_INTERSECTION -> state = IntersectionStatus.NO_INTERSECTION_OUTSIDE;
                            case NO_INTERSECTION_INSIDE ->
                                    state = IntersectionStatus.NO_INTERSECTION_INSIDE_AND_OUTSIDE;
                            default -> {
                            }
                        }
                    }
                    case NO_INTERSECTION_INSIDE_AND_OUTSIDE -> {
                        switch (state) {
                            case NO_INTERSECTION, NO_INTERSECTION_INSIDE, NO_INTERSECTION_OUTSIDE ->
                                    state = IntersectionStatus.NO_INTERSECTION_INSIDE_AND_OUTSIDE;
                            default -> {
                            }
                        }
                    }
                }
            }
        }

        return new IntersectionResultEx(state, intersections);
    }
}
