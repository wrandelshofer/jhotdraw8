/*
 * @(#)ToLongFunction3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

import org.jhotdraw8.annotation.NonNull;

@FunctionalInterface
public interface ToLongFunction3<T1, T2, T3> extends Function3<T1, T2, T3, Long> {

    @Override
    @NonNull
    default Long apply(T1 t1, T2 t2, T3 t3) {
        return applyAsLong(t1, t2, t3);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @return the function result
     */
    long applyAsLong(T1 t1, T2 t2, T3 t3);
}
