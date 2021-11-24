/*
 * @(#)UniqueOrOneHopPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.util.function.AddToSet;

import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating unique paths from a directed graph.
 * <p>
 * The builder searches for unique paths using a breadth-first search.<br>
 * Returns only a path if it is unique or if there is only one hop
 * from start to goal.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class GloballyUniqueOrOneHopVertexPathFinder<V, C extends Number & Comparable<C>> extends AbstractVertexSequenceFinder<V, C> {

    public GloballyUniqueOrOneHopVertexPathFinder(@NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<V>> nextVerticesFunction, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, maxCost, nextVerticesFunction, costFunction, sumFunction);
    }

    @Override
    protected @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<V>> nextNodesFunction, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, nextNodesFunction, new HashSet<>(16)::add, zero, maxCost, costFunction, sumFunction);
    }

    protected @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices,
                                                    @NonNull Predicate<V> goalPredicate,
                                                    @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                                    @NonNull AddToSet<V> visited,
                                                    @NonNull C zero,
                                                    @NonNull C maxCost,
                                                    @NonNull BiFunction<V, V, C> costFunction,
                                                    @NonNull BiFunction<C, C, C> sumFunction) {
        return new GloballyUniqueOrOneHopVertexPathSearchAlgo<V, C>().search(
                startVertices, goalPredicate, nextNodesFunction, visited, zero, maxCost, costFunction, sumFunction);
    }

}
