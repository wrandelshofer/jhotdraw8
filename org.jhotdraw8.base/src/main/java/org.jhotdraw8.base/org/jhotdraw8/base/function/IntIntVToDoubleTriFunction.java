/*
 * @(#)IntIntVToDoubleTriFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

@FunctionalInterface
public interface IntIntVToDoubleTriFunction<V> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    double applyAsDouble(int t, int u, V v);
}
