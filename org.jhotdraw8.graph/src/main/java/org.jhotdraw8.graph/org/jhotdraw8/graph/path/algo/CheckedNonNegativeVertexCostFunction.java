/*
 * @(#)CheckedNonNegativeVertexCostFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;


import java.util.function.BiFunction;

/**
 * A cost function that checks if the provided cost function always returns
 * value greater zero.
 * <p>
 * This class is package private.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
record CheckedNonNegativeVertexCostFunction<V, C extends Number & Comparable<C>>(C zero,
                                                                                 BiFunction<V, V, C> costFunction) implements BiFunction<V, V, C> {
    CheckedNonNegativeVertexCostFunction {
        AlgoArguments.checkZero(zero);
    }

    @Override
    public C apply(V v1, V v2) {
        C cost = costFunction.apply(v1, v2);
        if (cost.compareTo(zero) < 0) {
            throw new IllegalStateException("cost must be >= 0. v1=" + v1 + ", v2=" + v2 + ", cost=" + cost);
        }
        return cost;
    }
}
