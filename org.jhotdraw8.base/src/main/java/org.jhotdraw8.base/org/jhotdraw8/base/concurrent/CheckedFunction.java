/*
 * @(#)CheckedFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.concurrent;

/**
 * A function that may throw a checked exception.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @author Werner Randelshofer
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws Exception;
}
