/*
 * @(#)CheckedNonNegativeArcCostFunction3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.Function3;
import org.jspecify.annotations.Nullable;

/**
 * A cost function that checks if the provided cost function always returns
 * value greater zero.
 * <p>
 * This class is package private.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
record CheckedNonNegativeArcCostFunction3<V, A, C extends Number & Comparable<C>>(C zero,
                                                                                  Function3<V, V, A, C> costFunction) implements Function3<V, V, A, C> {
    CheckedNonNegativeArcCostFunction3 {
        AlgoArguments.checkZero(zero);
    }

    @Override
    public C apply(V v1, V v2, @Nullable A a) {
        C cost = costFunction.apply(v1, v2, a);
        if (cost.compareTo(zero) < 0) {
            throw new IllegalStateException("cost must be >= 0. v1=" + v1 + ", v2=" + v2 + ", a=" + a + ", cost=" + cost);
        }
        return cost;
    }
}
