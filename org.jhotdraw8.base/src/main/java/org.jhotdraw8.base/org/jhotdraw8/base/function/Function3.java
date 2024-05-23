/*
 * @(#)Function3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;


/**
 * Represents a function that accepts 3 arguments and produces a result.
 *
 * @param <T1> the type of the first argument to the function
 * @param <T2> the type of the second argument to the function
 * @param <T3> the type of the third argument to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface Function3<T1, T2, T3, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t1 the first function argument
     * @param t2 the second function argument
     * @param t3 the third function argument
     * @return the function result
     */
    R apply(T1 t1, T2 t2, T3 t3);

}
