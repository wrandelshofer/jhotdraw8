package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.SpliteratorIterable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.algo.AllPathsSpliterator;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.util.TriFunction;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for enumerating all arc-, arrow-, or vertex-sequences from a
 * set of start vertices to a set of goal vertices using a breadth-first
 * algorithm.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public class SimpleAllPathsFinder<V, A, C extends Number & Comparable<C>> implements AllPathsFinder<V, A, C> {
    private final @NonNull C zero;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

    public SimpleAllPathsFinder(@NonNull C zero,
                                @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                @NonNull TriFunction<V, V, A, C> costFunction,
                                @NonNull BiFunction<C, C, C> sumFunction) {
        this.zero = zero;
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
    }

    /**
     * Lazily Enumerates all vertex sequences from start to goal up to the specified
     * maximal path length.
     *
     * @param start the start vertex
     * @param goal  the goal predicate
     * @return the enumerated sequences
     */
    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<Arc<V, A>>, C>> findAllArcSequences(@NonNull V start,
                                                                                           @NonNull Predicate<V> goal,
                                                                                           @NonNull C maxCost) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(
                Collections.singletonList(start), goal, nextArcsFunction,
                (backLink) -> AbstractSequenceFinder.toArrowSequence(backLink, (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow())),
                maxCost, zero, costFunction, sumFunction));
    }

    /**
     * Lazily Enumerates all array sequences from start to goal up to the specified
     * maximal cost.
     *
     * @param start   the start vertex
     * @param goal    the goal predicate
     * @param maxCost the maximal cost
     * @return the enumerated sequences
     */
    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<A>, C>> findAllArrowSequences(@NonNull V start,
                                                                                     @NonNull Predicate<V> goal,
                                                                                     @NonNull C maxCost) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(
                Collections.singletonList(start), goal, nextArcsFunction,
                (backLink) -> AbstractSequenceFinder.toArrowSequence(backLink, (a, b) -> b.getArrow()),
                maxCost, zero, costFunction, sumFunction));
    }

    /**
     * Lazily Enumerates all vertex sequences from start to goal up to the specified
     * maximal path length.
     *
     * @param start   the start vertex
     * @param goal    the goal predicate
     * @param maxCost the maximal cost
     * @return the enumerated sequences
     */
    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<V>, C>> findAllVertexSequences(@NonNull V start,
                                                                                      @NonNull Predicate<V> goal,
                                                                                      @NonNull C maxCost) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(Collections.singletonList(start), goal, nextArcsFunction,
                (backLink) -> AbstractSequenceFinder.toVertexSequence(backLink, ArcBackLink::getVertex),
                maxCost, zero, costFunction, sumFunction));
    }


}
