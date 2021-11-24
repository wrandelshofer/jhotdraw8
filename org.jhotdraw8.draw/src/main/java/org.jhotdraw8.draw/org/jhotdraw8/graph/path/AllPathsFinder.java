package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.Arc;

import java.util.function.Predicate;

public interface AllPathsFinder<V, A, C extends Number & Comparable<C>> {
    @NonNull Iterable<OrderedPair<ImmutableList<Arc<V, A>>, C>> findAllArcSequences(V start,
                                                                                    @NonNull Predicate<V> goal,
                                                                                    C maxCost);

    @NonNull Iterable<OrderedPair<ImmutableList<A>, C>> findAllArrowSequences(V start,
                                                                              @NonNull Predicate<V> goal,
                                                                              C maxCost);

    @NonNull Iterable<OrderedPair<ImmutableList<V>, C>> findAllVertexSequences(V start,
                                                                               @NonNull Predicate<V> goal,
                                                                               C maxCost);
}
