/*
 * @(#)AnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for finding arbitrary paths on a directed graph.
 * <p>
 * The builder searches for paths using a breadth-first search.<br>
 * Returns the first path that it finds.<br>
 * Returns nothing if there is no path.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class ArbitraryPathFinder<V, A, C extends Number & Comparable<C>> extends AbstractSequenceFinder<V, A, C> {


    public ArbitraryPathFinder(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction, C zero, C maxCost,
                               TriFunction<V, V, A, C> costFunction,
                               BiFunction<C, C, C> sumFunction) {
        super(zero, maxCost, nextArcsFunction, costFunction, sumFunction);
    }

    public ArbitraryPathFinder(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction, C zero, C maxCost,
                               BiFunction<V, V, C> costFunction,
                               BiFunction<C, C, C> sumFunction) {
        super(zero, maxCost, nextArcsFunction, costFunction, sumFunction);
    }

    public static <V, A> ArbitraryPathFinder<V, A, Integer> newIntCostInstance(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                                                               TriFunction<V, V, A, Integer> costFunction) {
        return new ArbitraryPathFinder<>(nextArcsFunction, 0, Integer.MAX_VALUE, costFunction, Integer::sum);
    }

    public static <V, A> ArbitraryPathFinder<V, A, Integer> newIntCostInstance(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction) {
        return new ArbitraryPathFinder<>(nextArcsFunction, 0, Integer.MAX_VALUE, (u, v, a) -> 1, Integer::sum);
    }


    @Override
    protected @Nullable ArcBackLink<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                    @NonNull Predicate<V> goalPredicate,
                                                    @NonNull C zero,
                                                    @NonNull C positiveInfinity,
                                                    @NonNull C maxCost,
                                                    @NonNull Function<V, Iterable<Arc<V, A>>> nextNodesFunction,
                                                    @NonNull TriFunction<V, V, A, C> costFunction,
                                                    @NonNull BiFunction<C, C, C> sumFunction) {
        return new ArbitraryPathFinderAlgo<V, A, C>().search(
                startVertices,
                goalPredicate,
                zero,
                positiveInfinity,
                maxCost,
                nextNodesFunction,
                costFunction,
                sumFunction);
    }
}