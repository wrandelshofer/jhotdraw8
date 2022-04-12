/*
 * @(#)ToLongTriFunction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.util;

import org.jhotdraw8.annotation.NonNull;

@FunctionalInterface
public interface ToLongTriFunction<T, U, V> extends TriFunction<T, U, V, Long> {

    @Override
    @NonNull
    default Long apply(T t, U u, V v) {
        return applyAsLong(t, u, v);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    long applyAsLong(T t, U u, V v);
}
