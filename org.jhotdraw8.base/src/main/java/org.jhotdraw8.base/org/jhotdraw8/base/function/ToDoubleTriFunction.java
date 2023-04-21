/*
 * @(#)ToDoubleTriFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

import org.jhotdraw8.annotation.NonNull;

@FunctionalInterface
public interface ToDoubleTriFunction<T, U, V> extends TriFunction<T, U, V, Double> {

    @Override
    @NonNull
    default Double apply(T t, U u, V v) {
        return applyAsDouble(t, u, v);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    double applyAsDouble(T t, U u, V v);
}
