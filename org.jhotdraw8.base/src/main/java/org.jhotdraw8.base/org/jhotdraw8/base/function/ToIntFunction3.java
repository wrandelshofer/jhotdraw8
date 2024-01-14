/*
 * @(#)ToIntFunction3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

import org.jhotdraw8.annotation.NonNull;

@FunctionalInterface
public interface ToIntFunction3<T1, T2, T3> extends Function3<T1, T2, T3, Integer> {

    @Override
    @NonNull
    default Integer apply(T1 t1, T2 t2, T3 t3) {
        return applyAsInt(t1, t2, t3);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @return the function result
     */
    int applyAsInt(T1 t1, T2 t2, T3 t3);
}
