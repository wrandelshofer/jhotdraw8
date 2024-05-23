/*
 * @(#)ToDoubleFunction3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;


import java.util.function.Function;

@FunctionalInterface
public interface ToFloatFunction<T1> extends Function<T1, Float> {

    @Override
    default Float apply(T1 t1) {
        return applyAsFloat(t1);
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @return the function result
     */
    float applyAsFloat(T1 t1);
}
