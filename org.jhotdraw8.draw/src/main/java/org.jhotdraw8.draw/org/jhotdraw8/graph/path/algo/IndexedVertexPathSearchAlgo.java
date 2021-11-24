package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLink;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;

public interface IndexedVertexPathSearchAlgo<C extends Number & Comparable<C>> {


    @Nullable IndexedVertexBackLink<C> search(@NonNull Iterable<Integer> starts,
                                              @NonNull IntPredicate goal,
                                              @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                                              @NonNull AddToIntSet visited,
                                              @NonNull C maxCost,
                                              @NonNull C zero,
                                              @NonNull C positiveInfinity,
                                              @NonNull BiFunction<Integer, Integer, C> costFunction,
                                              @NonNull BiFunction<C, C, C> sumFunction);
}
