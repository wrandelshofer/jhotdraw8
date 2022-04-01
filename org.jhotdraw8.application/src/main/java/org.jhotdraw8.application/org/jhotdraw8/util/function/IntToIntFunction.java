package org.jhotdraw8.util.function;

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
