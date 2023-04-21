/*
 * @(#)IntToIntFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.function;

/**
 * Represents a function that accepts one argument and produces a result.
 */
@FunctionalInterface
public interface IntToIntFunction {
    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    int applyAsInt(int value);
}
