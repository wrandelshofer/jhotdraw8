/*
 * @(#)ToIntTriFunction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.util.function;

import org.jhotdraw8.annotation.NonNull;

@FunctionalInterface
public interface ToIntTriFunction<T, U, V> extends TriFunction<T, U, V, Integer> {

    @Override
    @NonNull
    default Integer apply(T t, U u, V v) {
        return applyAsInt(t, u, v);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    int applyAsInt(T t, U u, V v);
}
