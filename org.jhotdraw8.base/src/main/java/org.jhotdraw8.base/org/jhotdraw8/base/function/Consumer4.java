/*
 * @(#)Consumer4.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

/**
 * Represents a consumer that accepts 4 arguments.
 *
 * @param <T1> the type of the first argument to the operation
 * @param <T2> the type of the second argument to the operation
 * @param <T3> the type of the third argument to the operation
 * @param <T4> the type of the third argument to the operation
 */
@FunctionalInterface
public interface Consumer4<T1, T2, T3, T4> {
    /**
     * Applies this consumer to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @param t4 the fourth function argument
     */
    void accept(T1 t1, T2 t2, T3 t3, T4 t4);

}
