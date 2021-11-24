package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;

import java.util.ArrayDeque;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base class for a {@link VertexSequenceFinder} that uses a 'cost function'
 * and a 'back link' data structure.
 */
public abstract class AbstractVertexSequenceFinder<V, C extends Number & Comparable<C>> implements VertexSequenceFinder<V, C> {
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
    public AbstractVertexSequenceFinder(@NonNull C zero,
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
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C maxCost) {
        return toVertexSequence(search(startVertices, goalPredicate, zero, positiveInfinity, this.maxCost, nextVerticesFunction, costFunction, sumFunction), VertexBackLink::getVertex);
    }

    @Override
    public OrderedPair<ImmutableList<V>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return VertexSequenceFinder.<V, C>findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, maxCostBetweenWaypoints),
                zero,
                sumFunction

        );
        /*
        List<V> sequence = new ArrayList<>();
        V prev = null;
        C sum = zero;
        for (V next : waypoints) {
            if (prev != null) {
                final OrderedPair<ImmutableList<V>, C> result = findVertexSequence(prev, next, maxCost);
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

         */
    }


    private <X> @Nullable OrderedPair<ImmutableList<X>, C> toVertexSequence(@Nullable VertexBackLink<V, C> node,
                                                                            @NonNull Function<VertexBackLink<V, C>, X> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<X> deque = new ArrayDeque<>();
        for (VertexBackLink<V, C> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }


    protected abstract @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices,
                                                             @NonNull Predicate<V> goalPredicate,
                                                             @NonNull C zero,
                                                             @NonNull C positiveInfinity,
                                                             @NonNull C maxCost,
                                                             @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                                             @NonNull BiFunction<V, V, C> costFunction,
                                                             @NonNull BiFunction<C, C, C> sumFunction);

}
