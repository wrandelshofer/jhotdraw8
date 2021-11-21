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
 * Base class for a {@link VertexSequenceBuilder}
 * which internally uses a 'back link' data structure.
 * <p>
 * This class needs an iterator over the next vertices of a vertex.
 */
public abstract class AbstractVertexSequenceBuilder<V> implements VertexSequenceBuilder<V, Integer> {
    private final int maxLength;
    private final Function<V, Iterable<V>> nextVertexFunction;

    public AbstractVertexSequenceBuilder(int maxLength,
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
        return VertexSequenceBuilder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxCostBetweenWaypoints), 0, Integer::sum);
    }


    private @Nullable OrderedPair<ImmutableList<V>, Integer> toVertexSequence(@Nullable BackLink<V> node) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<V> deque = new ArrayDeque<>();
        for (BackLink<V> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(parent.getVertex());
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), deque.size());
    }

    public Function<V, Iterable<V>> getNextVertexFunction() {
        return nextVertexFunction;
    }

    protected abstract @Nullable BackLink<V> search(@NonNull Iterable<V> startVertices,
                                                    @NonNull Predicate<V> goalPredicate,
                                                    @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                                    int maxLength);

    protected static class BackLink<VV> {
        private final int remainingLength;
        private final @NonNull VV vertex;
        private final @Nullable BackLink<VV> parent;
        private final int depth;

        public BackLink(@NonNull VV node, @Nullable BackLink<VV> parent, int remainingLength) {
            this.vertex = node;
            this.parent = parent;
            this.remainingLength = remainingLength;
            this.depth = parent == null ? 0 : parent.depth + 1;
        }


        /**
         * Return the path length up to this back link.
         */
        protected int getRemainingLength() {
            return remainingLength;
        }

        public @Nullable BackLink<VV> getParent() {
            return parent;
        }

        public @NonNull VV getVertex() {
            return vertex;
        }

        public int getDepth() {
            return depth;
        }

        @Override
        public String toString() {
            return "BackLink{" +
                    "v=" + vertex +
                    ", c=" + remainingLength +
                    ", parent=" + parent +
                    '}';
        }
    }
}
