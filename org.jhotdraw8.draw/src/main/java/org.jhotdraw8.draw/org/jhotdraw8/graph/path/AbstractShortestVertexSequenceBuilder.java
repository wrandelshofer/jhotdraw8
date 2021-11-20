package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base class for {@link VertexSequenceBuilder} which provides a 'shortest path'
 * algorithm that internally uses a 'back link' data structure.
 * <p>
 * This class uses an iterator over the next vertices of a vertex,
 * and a cost function.
 */
public abstract class AbstractShortestVertexSequenceBuilder<V, C extends Number & Comparable<C>> implements VertexSequenceBuilder<V, C> {
    private final @NonNull C zero;
    private final @NonNull C positiveInfinity;
    private final @NonNull C maxCost;
    private final @NonNull Function<V, Iterable<V>> nextVerticesFunction;
    private final @NonNull BiFunction<V, V, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

    /**
     * Creates a new instance.
     *
     * @param zero                 the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity     the positive infinity value or max value,
     *                             e.g. {@link Integer#MAX_VALUE},
     *                             {@link Double#POSITIVE_INFINITY}.
     * @param maxCost              the maximal cost of a sequence,
     *                             e.g. {@link Integer#MAX_VALUE}, {@link Double#MAX_VALUE}.
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function, which adds two numbers,
     *                             e.g. {@link Integer#sum}, {@link Double#sum}.
     */
    public AbstractShortestVertexSequenceBuilder(@NonNull C zero,
                                                 @NonNull C positiveInfinity,
                                                 @NonNull C maxCost,
                                                 @NonNull Function<V, Iterable<V>> nextVerticesFunction,
                                                 @NonNull BiFunction<V, V, C> costFunction,
                                                 @NonNull BiFunction<C, C, C> sumFunction) {
        this.zero = zero;
        this.positiveInfinity = positiveInfinity;
        this.maxCost = maxCost;
        this.nextVerticesFunction = nextVerticesFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate) {
        return toVertexSequence(search(startVertices, goalPredicate, zero, positiveInfinity, maxCost, nextVerticesFunction, costFunction, sumFunction), BackLink::getVertex);
    }

    @Override
    public OrderedPair<ImmutableList<V>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<V> waypoints) {
        List<V> sequence = new ArrayList<>();
        V prev = null;
        C sum = zero;
        for (V next : waypoints) {
            if (prev != null) {
                final OrderedPair<ImmutableList<V>, C> result = findVertexSequence(prev, next);
                if (result == null) {
                    return null;
                } else {
                    final List<V> nextSequence = result.first().asList();
                    sequence.addAll(sequence.isEmpty() ? nextSequence : nextSequence.subList(1, nextSequence.size()));
                    sum = sumFunction.apply(sum, result.second());
                }
            }
            prev = next;
        }

        return new OrderedPair<>(ImmutableLists.copyOf(sequence), sum);
    }


    private <X> @Nullable OrderedPair<ImmutableList<X>, C> toVertexSequence(@Nullable BackLink<V, C> node,
                                                                            @NonNull Function<BackLink<V, C>, X> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<X> deque = new ArrayDeque<>();
        for (BackLink<V, C> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }


    protected abstract @Nullable BackLink<V, C> search(@NonNull Iterable<V> startVertices,
                                                       @NonNull Predicate<V> goalPredicate,
                                                       @NonNull C zero,
                                                       @NonNull C positiveInfinity,
                                                       @NonNull C maxCost,
                                                       @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                                       @NonNull BiFunction<V, V, C> costFunction,
                                                       @NonNull BiFunction<C, C, C> sumFunction);

    protected static class BackLink<VV, CC extends Number & Comparable<CC>> implements Comparable<BackLink<VV, CC>> {
        private final @NonNull VV vertex;
        private final @NonNull CC cost;
        private final @Nullable BackLink<VV, CC> parent;

        public BackLink(@NonNull VV node, @Nullable BackLink<VV, CC> parent, @NonNull CC cost) {
            this.vertex = node;
            this.cost = cost;
            this.parent = parent;
        }

        @Override
        public int compareTo(@NonNull BackLink<VV, CC> that) {
            return this.getCost().compareTo(that.getCost());
        }


        public @NonNull CC getCost() {
            return cost;
        }

        /**
         * Return the path length up to this back link.
         */
        protected int getLength() {
            int length = 0;
            for (BackLink<VV, CC> node = getParent(); node != null; node = node.getParent()) {
                length++;
            }
            return length;
        }

        public @Nullable BackLink<VV, CC> getParent() {
            return parent;
        }

        public @NonNull VV getVertex() {
            return vertex;
        }


        @Override
        public String toString() {
            return "BackLink{" +
                    "v=" + vertex +
                    ", c=" + cost +
                    ", parent=" + parent +
                    '}';
        }
    }
}
