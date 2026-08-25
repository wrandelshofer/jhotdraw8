/*
 * @(#)AlgoArguments.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;


/// Provides methods for checking common algorithm arguments.
///
/// This class is package private.
class AlgoArguments {
    /// Don't let anyone instantiate this class.
    private AlgoArguments() {

    }

    /// Checks common arguments of algorithms where the cost limit is
    /// a maximal cost.
    ///
    /// @param maxDepth  must be `>= 0`
    /// @param zero      must be `= 0`
    /// @param costLimit must be `>= zero`
    static void checkMaxDepthMaxCostArguments(int maxDepth, int costLimit) {
        checkMaxDepth(maxDepth);

        if (costLimit < 0) {
            throw new IllegalArgumentException("costLimit must be >= zero. costLimit=" + costLimit);
        }
    }


    /// Checks max depth.
    ///
    /// @param maxDepth must be `>= 0`
    static void checkMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0. maxDepth=" + maxDepth);
        }
    }


}
