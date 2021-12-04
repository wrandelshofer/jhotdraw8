package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;

/**
 * Provides methods for checking common algorithm arguments.
 * <p>
 * This class is package private.
 */
class AlgoArguments {
    /**
     * Don't let anyone instantiate this class.
     */
    private AlgoArguments() {

    }

    /**
     * Checks common arguments of algorithms where the cost limit is
     * a maximal cost.
     *
     * @param maxDepth  must be {@literal >= 0}
     * @param zero      must be {@literal = 0}
     * @param costLimit must be {@literal >= zero}
     */
    static <CC extends Number & Comparable<CC>> void checkMaxDepthMaxCostArguments(int maxDepth, @NonNull CC zero, @NonNull CC costLimit) {
        checkMaxDepth(maxDepth);
        checkZero(zero);
        if (costLimit.compareTo(zero) < 0) {
            throw new IllegalArgumentException("costLimit must be >= zero. costLimit=" + costLimit + " zero=" + zero);
        }
    }

    /**
     * Checks common arguments of algorithms.
     *
     * @param zero must be {@literal = 0}
     */
    static <CC extends Number & Comparable<CC>> void checkZero(@NonNull CC zero) {
        if (zero.intValue() != 0) {
            throw new IllegalArgumentException("zero must be = 0. zero=" + zero);
        }
    }

    /**
     * Checks max depth.
     *
     * @param maxDepth must be {@literal >= 0}
     */
    static <CC extends Number & Comparable<CC>> void checkMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0. maxDepth=" + maxDepth);
        }
    }


}
