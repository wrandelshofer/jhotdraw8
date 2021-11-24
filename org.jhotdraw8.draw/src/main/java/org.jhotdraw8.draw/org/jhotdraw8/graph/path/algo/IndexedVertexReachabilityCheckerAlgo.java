package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;

public interface IndexedVertexReachabilityCheckerAlgo<C extends Number & Comparable<C>> {
    boolean tryToReach(@NonNull Iterable<Integer> startVertices,
                       @NonNull IntPredicate goalPredicate,
                       @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                       @NonNull AddToIntSet visited,
                       C maxCost,
                       C zero,
                       @NonNull BiFunction<Integer, Integer, C> costFunction,
                       @NonNull BiFunction<C, C, C> sumFunction);
}
