/*
 * @(#)CheckedNonNegativeArcCostFunction3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jspecify.annotations.Nullable;

/// A cost function that checks if the provided cost function always returns
/// value greater zero.
///
/// This class is package private.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
/// @param <C> the cost number type
record CheckedNonNegativeArcCostFunction3<V, A>(
        ToIntFunction3<V, V, A> costFunction) implements ToIntFunction3<V, V, A> {
    CheckedNonNegativeArcCostFunction3 {

    }

    @Override
    public int applyAsInt(V v1, V v2, @Nullable A a) {
        int cost = costFunction.applyAsInt(v1, v2, a);
        if (cost < 0) {
            throw new IllegalStateException("cost must be >= 0. v1=" + v1 + ", v2=" + v2 + ", a=" + a + ", cost=" + cost);
        }
        return cost;
    }
}
