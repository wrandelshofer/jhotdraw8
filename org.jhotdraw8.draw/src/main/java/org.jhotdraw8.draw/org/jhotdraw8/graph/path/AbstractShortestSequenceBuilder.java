package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.ArrayDeque;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base class for a combined {@link ArcSequenceBuilder},
 * {@link ArrowSequenceBuilder},  {@link VertexSequenceBuilder}
 * which provides a 'shortest path' algorithm, and internally uses a
 * 'back link' data structure.
 * <p>
 * This class uses an iterator over the next {@link Arc}s of a vertex,
 * and a cost function.
 */
public abstract class AbstractShortestSequenceBuilder<V, A, C extends Number & Comparable<C>>
        implements SequenceBuilder<V, A, C> {

    private final @NonNull C zero;
    private final @NonNull C positiveInfinity;
    protected final @NonNull C maxCost;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

    /**
     * Creates a new instance.
     *
     * @param zero             the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity the positive infinity value or max value,
     *                         e.g. {@link Integer#MAX_VALUE},
     *                         {@link Double#POSITIVE_INFINITY}.
     *                         Must be &gt;= zero.
     * @param maxCost          the maximal cost (inclusive) of a sequence,
     *                         e.g. {@link Integer#MAX_VALUE}, {@link Double#MAX_VALUE}.
     *                         Must be &gt;= zero and &lt;= positiveInfinity.
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the {@link Arc}s
     *                         starting at that vertex.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function, which adds two numbers,
     *                         e.g. {@link Integer#sum}, {@link Double#sum}.
     */
    public AbstractShortestSequenceBuilder(@NonNull C zero,
                                           @NonNull C positiveInfinity,
                                           @NonNull C maxCost,
                                           @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                           @NonNull TriFunction<V, V, A, C> costFunction,
                                           @NonNull BiFunction<C, C, C> sumFunction) {
        if (zero.doubleValue() != 0.0) {
            throw new IllegalArgumentException("zero(" + zero + ") is != 0");
        }
        if (positiveInfinity.compareTo(zero) < 0) {
            throw new IllegalArgumentException("positiveInfinity(" + positiveInfinity + ") is < zero(" + zero + ")");
        }
        if (positiveInfinity.compareTo(maxCost) < 0) {
            throw new IllegalArgumentException("positiveInfinity(" + positiveInfinity + ") is < maxCost(" + maxCost + ")");
        }
        if (maxCost.compareTo(zero) < 0) {
            throw new IllegalArgumentException("maxCost(" + maxCost + ") is < zero(" + zero + ")");
        }
        this.zero = zero;
        this.positiveInfinity = positiveInfinity;
        this.maxCost = maxCost;
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate) {
        return toVertexSequence(search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextArcsFunction, costFunction, sumFunction), BackLink::getVertex);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate) {
        return toArrowSequence(search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextArcsFunction, costFunction, sumFunction), (a, b) -> b.getArrow());
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate) {
        return toArrowSequence(search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextArcsFunction, costFunction, sumFunction), (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow()));
    }

    @Override
    public OrderedPair<ImmutableList<V>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<V> waypoints) {
        return VertexSequenceBuilder.findVertexSequenceOverWaypoints(waypoints, this::findVertexSequence, zero, sumFunction);
    }

    @Override
    public OrderedPair<ImmutableList<A>, C> findArrowSequenceOverWaypoints(@NonNull Iterable<V> waypoints) {
        return ArrowSequenceBuilder.findArrowSequenceOverWaypoints(waypoints, this::findArrowSequence, zero, sumFunction);
    }

    @Override
    public OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequenceOverWaypoints(@NonNull Iterable<V> waypoints) {
        return ArcSequenceBuilder.findArcSequenceOverWaypoints(waypoints, this::findArcSequence, zero, sumFunction);
    }

    private <X> @Nullable OrderedPair<ImmutableList<X>, C> toVertexSequence(@Nullable BackLink<V, A, C> node,
                                                                            @NonNull Function<BackLink<V, A, C>, X> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<X> deque = new ArrayDeque<>();
        for (BackLink<V, A, C> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

    private <X> @Nullable OrderedPair<ImmutableList<X>, C> toArrowSequence(@Nullable BackLink<V, A, C> node,
                                                                           @NonNull BiFunction<BackLink<V, A, C>, BackLink<V, A, C>, X> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<X> deque = new ArrayDeque<>();
        BackLink<V, A, C> prev = node;
        for (BackLink<V, A, C> parent = node.getParent(); parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent, prev));
            prev = parent;
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

    public Function<V, Iterable<Arc<V, A>>> getNextArcsFunction() {
        return nextArcsFunction;
    }

    /**
     * Search engine method.
     *
     * @param startVertices     the set of start vertices
     * @param goalPredicate     the goal predicate
     * @param zero              the zero cost value
     * @param positiveInfinity  the positive infinity value
     * @param maxCost           the maximal cost (inclusive) that a sequence may have
     * @param nextNodesFunction
     * @param costFunction
     * @param sumFunction
     * @return
     */
    protected abstract @Nullable BackLink<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                          @NonNull Predicate<V> goalPredicate,
                                                          @NonNull C zero,
                                                          @NonNull C positiveInfinity,
                                                          @NonNull C maxCost,
                                                          @NonNull Function<V, Iterable<Arc<V, A>>> nextNodesFunction,
                                                          @NonNull TriFunction<V, V, A, C> costFunction,
                                                          @NonNull BiFunction<C, C, C> sumFunction);

    protected static class BackLink<VV, AA, CC extends Number & Comparable<CC>> implements Comparable<BackLink<VV, AA, CC>> {
        private final @NonNull VV vertex;
        private final @Nullable AA arrow;
        private final @NonNull CC cost;
        private final @Nullable BackLink<VV, AA, CC> parent;
        private final int depth;

        public BackLink(@NonNull VV node, @Nullable AA arrow, @Nullable BackLink<VV, AA, CC> parent, @NonNull CC cost) {
            this.vertex = node;
            this.cost = cost;
            this.parent = parent;
            this.arrow = arrow;
            this.depth = parent == null ? 0 : parent.getDepth();
        }

        @Override
        public int compareTo(@NonNull BackLink<VV, AA, CC> that) {
            int result = this.getCost().compareTo(that.getCost());
            return result == 0
                    ? Integer.compare(this.depth, that.depth)
                    : result;
        }


        public @Nullable AA getArrow() {
            return arrow;
        }

        public @NonNull CC getCost() {
            return cost;
        }

        public int getDepth() {
            return depth;
        }

        public @Nullable BackLink<VV, AA, CC> getParent() {
            return parent;
        }

        public @NonNull VV getVertex() {
            return vertex;
        }


        @Override
        public String toString() {
            return "BackLink@" + Integer.toHexString(System.identityHashCode(this)) + "{" +
                    "v=" + vertex +
                    ", a=" + arrow +
                    ", c=" + cost +
                    ", d=" + depth +
                    ", parent=" + Integer.toHexString(System.identityHashCode(parent)) +
                    '}';
        }
    }
}
