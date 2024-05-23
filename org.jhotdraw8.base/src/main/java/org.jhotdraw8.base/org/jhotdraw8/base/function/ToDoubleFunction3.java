/*
 * @(#)ToDoubleFunction3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;


@FunctionalInterface
public interface ToDoubleFunction3<T1, T2, T3> extends Function3<T1, T2, T3, Double> {

    @Override
    default Double apply(T1 t1, T2 t2, T3 t3) {
        return applyAsDouble(t1, t2, t3);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @return the function result
     */
    double applyAsDouble(T1 t1, T2 t2, T3 t3);
}
