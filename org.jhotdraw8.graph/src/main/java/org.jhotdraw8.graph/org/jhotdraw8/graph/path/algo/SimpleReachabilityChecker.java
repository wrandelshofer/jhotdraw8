/*
 * @(#)SimpleReachabilityChecker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

/// Implements the [ReachabilityChecker] interface.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
/// @param <C> the cost number type
public class SimpleReachabilityChecker<V, A>
        implements ReachabilityChecker<V> {
    private final ArcReachabilityAlgo<V, A> algo;

    private final Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final ToIntFunction3<V, V, A> costFunction;

    /// Creates a new instance.
    ///
    /// @param nextArcsFunction a function that given a vertex,
    ///                         returns an [Iterable] for the [Arc]s
    ///                         starting at that vertex.
    /// @param costFunction     the cost function
    /// @param algo             The search algorithm.
    public SimpleReachabilityChecker(
            Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            ToIntFunction3<V, V, A> costFunction,
            ArcReachabilityAlgo<V, A> algo) {
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.algo = algo;
    }

    /// Creates a new instance which has a cost function that returns integer
    /// numbers.
    ///
    /// @param nextArcsFunction a function that given a vertex,
    ///                         returns an [Iterable] for the arcs
    ///                         of that vertex.
    /// @param costFunction     the cost function
    /// @param algo             the search algorithm
    /// @param <VV>             the vertex data type
    /// @return the new [SimpleReachabilityChecker] instance.
    public static <VV, AA> SimpleReachabilityChecker<VV, AA> newIntCostInstance(
            Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            ToIntFunction3<VV, VV, AA> costFunction,
            ArcReachabilityAlgo<VV, AA> algo) {
        return new SimpleReachabilityChecker<>(nextArcsFunction, costFunction, algo);
    }


    @Override
    public boolean isReachable(Iterable<V> startVertices,
                               Predicate<V> goalPredicate,
                               int maxDepth, int costLimit, AddToSet<V> visited) {
        return algo.tryToReach(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, costLimit,
                costFunction
        );
    }

    @Override
    public boolean isReachable(V start,
                               Predicate<V> goalPredicate, int maxDepth, int costLimit, AddToSet<V> visited) {
        return algo.tryToReach(
                Collections.singletonList(start), goalPredicate, nextArcsFunction, maxDepth, costLimit,
                costFunction
        );
    }

    @Override
    public boolean isReachable(V start, V goal, int maxDepth, int costLimit, AddToSet<V> visited) {
        return algo.tryToReach(
                Collections.singletonList(start), goal::equals, nextArcsFunction, maxDepth, costLimit, costFunction
        );
    }


}
