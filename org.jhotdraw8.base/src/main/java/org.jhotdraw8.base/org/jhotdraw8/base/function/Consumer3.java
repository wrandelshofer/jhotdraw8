/*
 * @(#)Consumer3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

/**
 * Represents an operation that accepts 3 input arguments and returns no
 * result.
 *
 * @param <T1> the type of the first argument to the operation
 * @param <T2> the type of the second argument to the operation
 * @param <T3> the type of the third argument to the operation
 */
@FunctionalInterface
public interface Consumer3<T1, T2, T3> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t1 the first input argument
     * @param t2 the second input argument
     * @param t3 the third input argument
     */
    void accept(T1 t1, T2 t2, T3 t3);
}