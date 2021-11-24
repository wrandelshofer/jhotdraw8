package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base class for a combined {@link ArcSequenceFinder},
 * {@link ArrowSequenceFinder},  {@link VertexSequenceFinder}
 * which uses a 'cost function' and 'back link' data structure.
 */
public abstract class AbstractSequenceFinder<V, A, C extends Number & Comparable<C>>
        implements SequenceFinder<V, A, C>, ReachabilityChecker<V, C> {

    private final @NonNull C zero;
    private final @NonNull C positiveInfinity;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

    public AbstractSequenceFinder(@NonNull C zero,
                                  @NonNull C positiveInfinity,
                                  @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                  @NonNull BiFunction<V, V, C> costFunction,
                                  @NonNull BiFunction<C, C, C> sumFunction) {
        this(zero, positiveInfinity, nextArcsFunction, (u, v, a) -> costFunction.apply(u, v), sumFunction);
    }

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
     */
    public AbstractSequenceFinder(@NonNull C zero,
                                  @NonNull C positiveInfinity,
                                  @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                  @NonNull TriFunction<V, V, A, C> costFunction,
                                  @NonNull BiFunction<C, C, C> sumFunction) {
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
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C maxCost) {
        return toVertexSequence(search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextArcsFunction, costFunction, sumFunction), ArcBackLink::getVertex);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C maxCost) {
        return toArrowSequence(search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextArcsFunction, costFunction, sumFunction), (a, b) -> b.getArrow());
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C maxCost) {
        return toArrowSequence(search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextArcsFunction, costFunction, sumFunction), (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow()));
    }

    @Override
    public OrderedPair<ImmutableList<V>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxCostBetweenWaypoints), zero, sumFunction);
    }

    @Override
    public OrderedPair<ImmutableList<A>, C> findArrowSequenceOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return ArrowSequenceFinder.findArrowSequenceOverWaypoints(waypoints, (start, goal) -> findArrowSequence(start, goal, maxCostBetweenWaypoints), zero, sumFunction);
    }

    @Override
    public OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequenceOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return ArcSequenceFinder.findArcSequenceOverWaypoints(waypoints, (start, goal) -> findArcSequence(start, goal, maxCostBetweenWaypoints), zero, sumFunction);
    }

    public static <VV, AA, CC extends Number & Comparable<CC>, XX> @Nullable OrderedPair<ImmutableList<XX>, CC> toVertexSequence(@Nullable ArcBackLink<VV, AA, CC> node,
                                                                                                                                 @NonNull Function<ArcBackLink<VV, AA, CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<XX> deque = new ArrayDeque<>();
        for (ArcBackLink<VV, AA, CC> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

    public static <VV, AA, CC extends Number & Comparable<CC>, XX> @Nullable OrderedPair<ImmutableList<XX>, CC> toArrowSequence(@Nullable ArcBackLink<VV, AA, CC> node,
                                                                                                                                @NonNull BiFunction<ArcBackLink<VV, AA, CC>, ArcBackLink<VV, AA, CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<XX> deque = new ArrayDeque<>();
        ArcBackLink<VV, AA, CC> prev = node;
        for (ArcBackLink<VV, AA, CC> parent = node.getParent(); parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent, prev));
            prev = parent;
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

    @Override
    public boolean isReachable(@NonNull Iterable<V> startVertices,
                               @NonNull Predicate<V> goalPredicate,
                               @NonNull C maxCost) {
        return findVertexSequence(startVertices, goalPredicate, maxCost) != null;
    }

    /**
     * Checks if a vertex sequence from a start vertex to a vertex
     * that satisfies the goal predicate exists.
     *
     * @param start         the start vertex
     * @param goalPredicate the goal vertex
     * @param maxCost
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @Override
    public boolean isReachable(@NonNull V start,
                               @NonNull Predicate<V> goalPredicate, @NonNull C maxCost) {
        return findVertexSequence(Collections.singletonList(start), goalPredicate, maxCost) != null;
    }

    /**
     * Checks if a vertex sequence from start to goal exists.
     *
     * @param start   the start vertex
     * @param goal    the goal vertex
     * @param maxCost
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @Override
    public boolean isReachable(@NonNull V start, @NonNull V goal, @NonNull C maxCost) {
        return findVertexSequence(start, goal, maxCost) != null;
    }

    /**
     * Checks if a vertex sequence through the given waypoints exists.
     *
     * @param waypoints               a list of waypoints
     * @param maxCostBetweenWaypoints
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @Override
    public boolean isReachableOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return findVertexSequenceOverWaypoints(waypoints, maxCostBetweenWaypoints) != null;
    }

    /**
     * Search engine method.
     *
     * @param startVertices     the set of start vertices
     * @param goalPredicate     the goal predicate
     * @param zero              the zero cost value
     * @param positiveInfinity  the positive infinity value
     * @param maxCost           the maximal cost (inclusive) that a sequence may have
     * @param nextNodesFunction the next nodes function
     * @param costFunction      the cost function
     * @param sumFunction       the sum function for adding two cost values
     * @return
     */
    protected abstract @Nullable ArcBackLink<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                             @NonNull Predicate<V> goalPredicate,
                                                             @NonNull C zero,
                                                             @NonNull C positiveInfinity,
                                                             @NonNull C maxCost,
                                                             @NonNull Function<V, Iterable<Arc<V, A>>> nextNodesFunction,
                                                             @NonNull TriFunction<V, V, A, C> costFunction,
                                                             @NonNull BiFunction<C, C, C> sumFunction);

}
