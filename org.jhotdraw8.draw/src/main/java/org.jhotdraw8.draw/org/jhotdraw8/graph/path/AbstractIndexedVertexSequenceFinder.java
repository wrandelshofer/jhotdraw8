package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLink;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base class for a {@link VertexSequenceFinder} that uses a 'cost function'
 * and a 'back link' data structure.
 */
public abstract class AbstractIndexedVertexSequenceFinder<C extends Number & Comparable<C>> implements VertexSequenceFinder<Integer, C> {
    private final @NonNull C zero;
    private final @NonNull C positiveInfinity;
    private final @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction;
    private final @NonNull BiFunction<Integer, Integer, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

    /**
     * Creates a new instance.
     *
     * @param zero                 the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity     the positive infinity value or max value,
     *                             e.g. {@link Integer#MAX_VALUE},
     *                             {@link Double#POSITIVE_INFINITY}.
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function, which adds two numbers,
     *                             e.g. {@link Integer#sum}, {@link Double#sum}.
     */
    public AbstractIndexedVertexSequenceFinder(@NonNull C zero,
                                               @NonNull C positiveInfinity,
                                               @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                                               @NonNull BiFunction<Integer, Integer, C> costFunction,
                                               @NonNull BiFunction<C, C, C> sumFunction) {
        this.zero = zero;
        this.positiveInfinity = positiveInfinity;
        this.nextVerticesFunction = nextVerticesFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Integer>, C> findVertexSequence(
            @NonNull Iterable<Integer> startVertices, @NonNull Predicate<Integer> goalPredicate, @NonNull C maxCost) {
        return toVertexSequence(search(startVertices, goalPredicate,
                nextVerticesFunction,
                AddToIntSet.addToBitSet(new BitSet()),
                maxCost,
                zero, positiveInfinity,
                costFunction,
                sumFunction), IndexedVertexBackLink::getVertex);
    }

    @Override
    public OrderedPair<ImmutableList<Integer>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<Integer> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return VertexSequenceFinder.<Integer, C>findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, maxCostBetweenWaypoints),
                zero,
                sumFunction

        );
    }


    private <X> @Nullable OrderedPair<ImmutableList<X>, C> toVertexSequence(@Nullable IndexedVertexBackLink<C> node,
                                                                            @NonNull Function<IndexedVertexBackLink<C>, X> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<X> deque = new ArrayDeque<>();
        for (IndexedVertexBackLink<C> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }


    protected abstract @Nullable IndexedVertexBackLink<C> search(@NonNull Iterable<Integer> starts,
                                                                 @NonNull Predicate<Integer> goal,
                                                                 @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                                                                 @NonNull AddToIntSet visited,
                                                                 @NonNull C maxCost,
                                                                 @NonNull C zero,
                                                                 @NonNull C positiveInfinity,
                                                                 @NonNull BiFunction<Integer, Integer, C> costFunction,
                                                                 @NonNull BiFunction<C, C, C> sumFunction);

}
