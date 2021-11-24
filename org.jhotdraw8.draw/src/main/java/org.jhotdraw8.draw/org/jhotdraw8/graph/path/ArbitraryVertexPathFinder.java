/*
 * @(#)AnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating arbitrary paths from a directed graph.
 * <p>
 * The builder searches for paths using a breadth-first search.<br>
 * Returns the first path that it finds.<br>
 * Returns nothing if there is no path.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class ArbitraryVertexPathFinder<V, C extends Number & Comparable<C>> extends AbstractVertexSequenceFinder<V, C> {
    public ArbitraryVertexPathFinder(
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, maxCost, nextVerticesFunction, costFunction, sumFunction);
    }

    public static <V, A> ArbitraryVertexPathFinder<V, Integer> newIntCostInstance(@NonNull Function<V, Iterable<V>> nextVerticesFunction) {
        return new ArbitraryVertexPathFinder<>(0, Integer.MAX_VALUE, Integer.MAX_VALUE, nextVerticesFunction, (u, v) -> 1, Integer::sum);
    }


    @Override
    protected @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<V>> nextNodesFunction,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {
        return new ArbitraryVertexPathSearchAlgo<V, C>()
                .search(startVertices, goalPredicate, new HashSet<V>()::add, maxCost, zero, nextNodesFunction,
                        costFunction, sumFunction);
    }


}