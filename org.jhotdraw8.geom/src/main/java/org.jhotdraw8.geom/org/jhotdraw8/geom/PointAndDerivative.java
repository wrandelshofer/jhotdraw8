/*
 * @(#)PointAndDerivative.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;

import java.util.function.BiFunction;

public record PointAndDerivative(double x, double y, double dx, double dy) {

    public <T> @NonNull T getPoint(BiFunction<Double, Double, T> factory) {
        return factory.apply(x, y);
    }

    public <T> @NonNull T getDerivative(BiFunction<Double, Double, T> factory) {
        return factory.apply(dx, dy);
    }

    /**
     * Returns the angle of the point in radians.
     *
     * @return angle in radians
     */
    public double getAngle() {
        return Angles.atan2(dy, dx);
    }

    /**
     * Returns a new point with reversed derivative.
     */
    public PointAndDerivative reverse() {
        return new PointAndDerivative(x, y, -dx, -dy);
    }
}
