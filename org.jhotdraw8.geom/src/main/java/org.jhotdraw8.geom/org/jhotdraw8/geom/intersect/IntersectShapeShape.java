/*
 * @(#)IntersectLinePathIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;


import java.awt.*;

public class IntersectShapeShape {
    private IntersectShapeShape() {
    }


    /**
     * Intersects the given shapes 'a' and 'b'.
     * <p>
     * This method can produce the following {@link IntersectionStatus} codes:
     * <dl>
     *     <dt>{@link IntersectionStatus#INTERSECTION}</dt><dd>
     *         A segment of shape 'a' intersects with a segment of shape 'b' within the
     *         given tolerance radius.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_COINCIDENT}</dt><dd>
     *         All segments of shape 'a' coincide with all segments of shape 'b'.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_INSIDE}</dt><dd>
     *         Shape 'a' lies inside shape 'b'.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_OUTSIDE}</dt><dd>
     *         Shape 'a' lies outside shape 'b'.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION_INSIDE_AND_OUTSIDE}</dt><dd>
     *         Shape 'a' lies inside and outside shape 'b'.
     *     </dd>
     *     <dt>{@link IntersectionStatus#NO_INTERSECTION}</dt><dd>
     *         Shape 'a' does not intersect with shape 'b' because 'a' or 'b' (or both) is (are) empty.
     *     </dd>
     * </dl>
     *
     * <p>
     * FIXME this implementation does not work yet
     *
     * @param a shape 'a'
     * @param b shape 'b'
     * @return the intersection result
     */
    public static IntersectionResultEx intersectShapeShapeEx(Shape a, Shape b) {
        return IntersectPathIteratorShape.intersectPathIteratorShapeEx(a.getPathIterator(null), b);
    }
}
