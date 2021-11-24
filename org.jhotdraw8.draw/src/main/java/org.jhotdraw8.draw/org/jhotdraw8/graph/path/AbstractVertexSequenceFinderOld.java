package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;

import java.util.ArrayDeque;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base class for a {@link VertexSequenceFinder}
 * which internally uses a 'back link' data structure.
 * <p>
 * This class needs an iterator over the next vertices of a vertex.
 */
public abstract class AbstractVertexSequenceFinderOld<V> implements VertexSequenceFinder<V, Integer> {
    private final int maxLength;
    private final Function<V, Iterable<V>> nextVertexFunction;

    public AbstractVertexSequenceFinderOld(int maxLength,
                                           Function<V, Iterable<V>> nextVerticesFunction
    ) {
        this.maxLength = maxLength;
        this.nextVertexFunction = nextVerticesFunction;

    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, Integer> findVertexSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull Integer maxCost) {
        return toVertexSequence(search(startVertices, goalPredicate, nextVertexFunction, maxLength));
    }


    @Override
    public OrderedPair<ImmutableList<V>, Integer> findVertexSequenceOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull Integer maxCostBetweenWaypoints) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxCostBetweenWaypoints), 0, Integer::sum);
    }


    private @Nullable OrderedPair<ImmutableList<V>, Integer> toVertexSequence(@Nullable VertexBackLinkOld<V> node) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<V> deque = new ArrayDeque<>();
        for (VertexBackLinkOld<V> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(parent.getVertex());
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), deque.size());
    }

    public Function<V, Iterable<V>> getNextVertexFunction() {
        return nextVertexFunction;
    }

    protected abstract @Nullable VertexBackLinkOld<V> search(@NonNull Iterable<V> startVertices,
                                                             @NonNull Predicate<V> goalPredicate,
                                                             @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                                             int maxLength);

}
