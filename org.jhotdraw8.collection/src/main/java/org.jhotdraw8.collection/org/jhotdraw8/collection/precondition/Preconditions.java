/*
 * @(#)Preconditions.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.precondition;


import org.jhotdraw8.annotation.NonNull;

/**
 * Preconditions.
 *
 * @author Werner Randelshofer
 */
public class Preconditions {
    private Preconditions() {

    }

    /**
     * Throws an illegal argument exception with a formatted message
     * if the expression is not true.
     *
     * @param expression           an expression
     * @param errorMessageTemplate the template for the error message
     * @param arguments            arguments for the error message
     * @throws IllegalArgumentException if expression is not true
     */
    public static void checkArgument(boolean expression, @NonNull String errorMessageTemplate, @NonNull Object... arguments) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, arguments));
        }
    }

    /**
     * Checks if the provided value is in the range {@code [min, max]}.
     *
     * @param value a value
     * @param min   the lower bound of the range (inclusive)
     * @param max   the upper bound of the range (inclusive)
     * @param name  the name of the value
     * @return the value
     * @throws IllegalArgumentException if value is not in [min, max].
     */
    public static int checkValueInRange(int value, int min, int max, String name) {
        if (value < min || value >= max) {
            throw new IllegalArgumentException(name + ": " + value + " not in range: [" + min + ", " + max + "].");
        }
        return value;
    }

}
