package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.SpliteratorIterable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.algo.AllPathsSpliterator;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link AllPathsFinder} interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 */
public class SimpleAllPathsFinder<V, A, C extends Number & Comparable<C>> implements AllPathsFinder<V, A, C> {
    private final @NonNull C zero;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

    /**
     * Creates a new instance.
     *
     * @param nextArcsFunction the next arcs function
     * @param zero             the zero cost value
     * @param costFunction     the cost function
     * @param sumFunction      the sum function
     */
    public SimpleAllPathsFinder(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                @NonNull C zero,
                                @NonNull TriFunction<V, V, A, C> costFunction,
                                @NonNull BiFunction<C, C, C> sumFunction) {
        this.zero = zero;
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
    }


    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<Arc<V, A>>, C>> findAllArcSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C maxCost) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLink.toArrowSequence(backLink, (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow())),
                maxCost, zero, costFunction, sumFunction));
    }


    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<A>, C>> findAllArrowSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C maxCost) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLink.toArrowSequence(backLink, (a, b) -> b.getArrow()),
                maxCost, zero, costFunction, sumFunction));
    }


    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<V>, C>> findAllVertexSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C maxCost) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLink.toVertexSequence(backLink, ArcBackLink::getVertex),
                maxCost, zero, costFunction, sumFunction));
    }


}
