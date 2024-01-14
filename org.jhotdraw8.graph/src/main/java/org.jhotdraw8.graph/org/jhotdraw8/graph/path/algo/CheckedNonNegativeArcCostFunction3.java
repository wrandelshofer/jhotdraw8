/*
 * @(#)CheckedNonNegativeArcCostFunction3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.Function3;

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
class CheckedNonNegativeArcCostFunction3<V, A, C extends Number & Comparable<C>> implements Function3<V, V, A, C> {
    final @NonNull C zero;
    final @NonNull Function3<V, V, A, C> costFunction;

    public CheckedNonNegativeArcCostFunction3(@NonNull C zero, @NonNull Function3<V, V, A, C> costFunction) {
        AlgoArguments.checkZero(zero);
        this.zero = zero;
        this.costFunction = costFunction;
    }

    @Override
    public @NonNull C apply(@NonNull V v1, @NonNull V v2, @Nullable A a) {
        C cost = costFunction.apply(v1, v2, a);
        if (cost.compareTo(zero) < 0) {
            throw new IllegalStateException("cost must be >= 0. v1=" + v1 + ", v2=" + v2 + ", a=" + a + ", cost=" + cost);
        }
        return cost;
    }
}
