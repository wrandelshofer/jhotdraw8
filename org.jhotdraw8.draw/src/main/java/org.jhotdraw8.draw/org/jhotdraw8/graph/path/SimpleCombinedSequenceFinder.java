package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.algo.ArcPathSearchAlgo;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link CombinedSequenceFinder} interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class SimpleCombinedSequenceFinder<V, A, C extends Number & Comparable<C>> implements CombinedSequenceFinder<V, A, C> {

    private final @NonNull C zero;
    private final @NonNull C positiveInfinity;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;
    private final @NonNull ArcPathSearchAlgo<V, A, C> algo;


    /**
     * Creates a new instance.
     *
     * @param zero             the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity the positive infinity value or max value,
     *                         e.g. {@link Integer#MAX_VALUE},
     *                         {@link Double#POSITIVE_INFINITY}.
     *                         Must be &gt;= zero.
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the {@link Arc}s
     *                         starting at that vertex.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function, which adds two numbers,
     *                         e.g. {@link Integer#sum}, {@link Double#sum}.
     * @param algo             the search algorithm
     */
    public SimpleCombinedSequenceFinder(
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction,
            @NonNull ArcPathSearchAlgo<V, A, C> algo) {
        if (zero.doubleValue() != 0.0) {
            throw new IllegalArgumentException("zero(" + zero + ") is != 0");
        }
        if (positiveInfinity.compareTo(zero) < 0) {
            throw new IllegalArgumentException("positiveInfinity(" + positiveInfinity + ") is < zero(" + zero + ")");
        }
        this.zero = zero;
        this.positiveInfinity = positiveInfinity;
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
        this.algo = algo;
    }


    /**
     * Creates a new instance with a cost function that returns integer
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Integer> costFunction,
            @NonNull ArcPathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleCombinedSequenceFinder<>(0, Integer.MAX_VALUE, nextArcsFunction, costFunction, Integer::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that returns integer
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull BiFunction<VV, VV, Integer> costFunction,
            @NonNull ArcPathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleCombinedSequenceFinder<>(0, Integer.MAX_VALUE, nextArcsFunction, (u, v, a) -> costFunction.apply(u, v), Integer::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that counts the number
     * of arrows.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull ArcPathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleCombinedSequenceFinder<>(0, Integer.MAX_VALUE, nextArcsFunction, (u, v, a) -> 1, Integer::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that returns double
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Double> newDoubleCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Double> costFunction,
            @NonNull ArcPathSearchAlgo<VV, AA, Double> algo) {
        return new SimpleCombinedSequenceFinder<>(0.0, Double.POSITIVE_INFINITY, nextArcsFunction, costFunction, Double::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that returns long
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Long> newLongCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Long> costFunction,
            @NonNull ArcPathSearchAlgo<VV, AA, Long> algo) {
        return new SimpleCombinedSequenceFinder<>(0L, Long.MAX_VALUE, nextArcsFunction, costFunction, Long::sum, algo);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, int maxDepth, @NonNull C costLimit) {
        return ArcBackLinkWithCost.toArrowSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, zero, positiveInfinity, costLimit, costFunction, sumFunction
        ), (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow()));
    }

    @Override
    public OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequenceOverWaypoints(@NonNull Iterable<V> waypoints, int maxDepth, @NonNull C costLimit) {
        return ArcSequenceFinder.findArcSequenceOverWaypoints(waypoints, (start, goal) -> findArcSequence(start, goal, maxDepth, costLimit), zero, sumFunction);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, int maxDepth, @NonNull C costLimit) {
        return ArcBackLinkWithCost.toArrowSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, zero, positiveInfinity, costLimit, costFunction, sumFunction
        ), (a, b) -> b.getArrow());
    }

    @Override
    public OrderedPair<ImmutableList<A>, C> findArrowSequenceOverWaypoints(@NonNull Iterable<V> waypoints, int maxDepth, @NonNull C costLimit) {
        return ArrowSequenceFinder.findArrowSequenceOverWaypoints(waypoints, (start, goal) -> findArrowSequence(start, goal, maxDepth, costLimit), zero, sumFunction);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, int maxDepth, @NonNull C costLimit) {
        return ArcBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, zero, positiveInfinity, costLimit, costFunction, sumFunction
        ), ArcBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<V> waypoints, int maxDepth, @NonNull C costLimit) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxDepth, costLimit), zero, sumFunction);
    }


}
