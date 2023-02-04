/*
 * @(#)PointAndTangent.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
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
}
