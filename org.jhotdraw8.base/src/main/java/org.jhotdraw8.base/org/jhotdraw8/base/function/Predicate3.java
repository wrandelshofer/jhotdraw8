/*
 * @(#)Predicate3.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.function;

/**
 * Represents a predicate (boolean-valued function) of three arguments.
 *
 * @param <T1> the type of the first argument to the predicate
 * @param <T2> the type of the second argument to the predicate
 * @param <V> the type of the third argument to the predicate
 */
@FunctionalInterface
public interface Predicate3<T1, T2, V> {

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t1 the first input argument
     * @param t2 the second input argument
     * @param v the third input argument
     * @return {@code true} if the input arguments match the predicate,
     * otherwise {@code false}
     */
    boolean test(T1 t1, T2 t2, V v);

}
