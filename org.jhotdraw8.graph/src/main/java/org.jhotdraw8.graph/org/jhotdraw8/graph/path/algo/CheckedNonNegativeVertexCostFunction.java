/*
 * @(#)CheckedNonNegativeVertexCostFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;


import java.util.function.ToIntBiFunction;

/// A cost function that checks if the provided cost function always returns
/// value greater zero.
///
/// This class is package private.
///
/// @param <V> the vertex data type
/// @param <C> the cost number type
record CheckedNonNegativeVertexCostFunction<V>(
        ToIntBiFunction<V, V> costFunction) implements ToIntBiFunction<V, V> {
    CheckedNonNegativeVertexCostFunction {

    }

    @Override
    public int applyAsInt(V v1, V v2) {
        int cost = costFunction.applyAsInt(v1, v2);
        if (cost < 0) {
            throw new IllegalStateException("cost must be >= 0. v1=" + v1 + ", v2=" + v2 + ", cost=" + cost);
        }
        return cost;
    }
}
