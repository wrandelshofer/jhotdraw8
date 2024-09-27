/*
 * @(#)IntersectPointRay.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.intersect;

import org.jhotdraw8.geom.Scalars;
import org.jspecify.annotations.Nullable;

public class IntersectPointRay {
    /**
     * Don't let anyone instantiate this class.
     */
    private IntersectPointRay() {
    }

    /**
     * Projects a point on an infinite line defined by the given ray.
     * The returned argument value is in range
     * [{@link Double#NEGATIVE_INFINITY},{@link Double#POSITIVE_INFINITY}].
     *
     * @param ox x origin of ray
     * @param oy y origin of ray
     * @param dx x direction of ray
     * @param dy y direction of ray
     * @param px x coordinate of point
     * @param py y coordinate of point
     * @return argument 't' at point px,py on the ray.
     */
    public static double projectedPointOnRay(double ox, double oy, double dx, double dy, double px, double py) {
        if (Math.abs(dx) > Math.abs(dy)) {
            return (px - ox) / dx;
        } else {
            return (py - oy) / dy;
        }
    }

    public static @Nullable Double argumentOnRay(double ox, double oy,
                                       double dx, double dy,
                                       double amax,
                                       double px, double py, double tolerance) {

        // equations:
        // o + t * d = p
        //     t     = (p - o) / d

        boolean aIsPoint = Scalars.almostZero(dx * amax) && Scalars.almostZero(dy * amax);
        if (aIsPoint) {
            return Scalars.almostEqual(ox, px) && Scalars.almostEqual(oy, py) ? 0.0 : null;
        }
        double t;
        if (Scalars.almostZero(dx)) {
            t = (py - oy) / dy;
        } else {
            t = (px - ox) / dx;
        }
        return -tolerance < t && t <= amax && Scalars.almostEqual(ox + t * dx, px) && Scalars.almostEqual(oy + t * dy, py)
                ? t : null;
    }
}
