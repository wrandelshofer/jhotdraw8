package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class SimpleSequenceFinder<V, A, C extends Number & Comparable<C>> extends AbstractSequenceFinder<V, A, C> {
    private @NonNull PathSearchAlgo<V, A, C> algo;

    public SimpleSequenceFinder(
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction,
            @NonNull PathSearchAlgo<V, A, C> algo) {
        super(zero, positiveInfinity, nextArcsFunction, costFunction, sumFunction);
        this.algo = algo;
    }

    @Override
    protected @Nullable ArcBackLink<V, A, C> search(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextf,
            @NonNull TriFunction<V, V, A, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {
        return algo.search(
                starts, goalPredicate, zero, positiveInfinity, maxCost, nextf, costf, sumf
        );
    }
}
