/*
 * @(#)UniquePathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating unique paths from a directed graph.
 * <p>
 * The builder searches for unique paths using a breadth-first search.<br>
 * Returns only a path if it is unique.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class GloballyUniqueVertexPathFinder<V, C extends Number & Comparable<C>> extends AbstractVertexSequenceFinder<V, C> {

    public GloballyUniqueVertexPathFinder(@NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<V>> nextVerticesFunction, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, maxCost, nextVerticesFunction, costFunction, sumFunction);
    }

    @Override
    protected @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<V>> nextNodesFunction, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        return new GloballyUniqueVertexPathSearchAlgo<V, C>().search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextNodesFunction, costFunction, sumFunction);
    }
}


