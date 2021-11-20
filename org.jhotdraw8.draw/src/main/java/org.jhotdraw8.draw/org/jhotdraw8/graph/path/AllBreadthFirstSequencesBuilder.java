package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.graph.Arc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AllBreadthFirstSequencesBuilder<V, A> {
    private final int maxLength;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;

    public AllBreadthFirstSequencesBuilder(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction) {
        this(Integer.MAX_VALUE, nextArcsFunction);
    }

    public AllBreadthFirstSequencesBuilder(int maxLength, @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction) {
        this.maxLength = maxLength;
        this.nextArcsFunction = nextArcsFunction;
    }

    /**
     * Enumerates all vertex paths from start to goal up to the specified
     * maximal path length.
     * //
     * FIXME this should return an Iterable, because there may
     * be a very large number of vertex sequences in a graph that has
     * cycles!
     *
     * @param start the start vertex
     * @param goal  the goal predicate
     * @return the enumerated paths
     */
    public @NonNull List<ImmutableList<V>> findAllVertexSequences(@NonNull V start,
                                                                  @NonNull Predicate<V> goal) {

        if (maxLength <= 0) {
            return new ArrayList<>();
        }
        List<AbstractShortestSequenceBuilder.BackLink<V, A, Integer>> backlinks = new ArrayList<>();
        searchAll(new AbstractShortestSequenceBuilder.BackLink<>(start, null, null, maxLength),
                goal,
                nextArcsFunction,
                backlinks);
        List<ImmutableList<V>> vertexPaths = new ArrayList<>(backlinks.size());
        Deque<V> path = new ArrayDeque<>();
        for (AbstractShortestSequenceBuilder.BackLink<V, A, Integer> list : backlinks) {
            path.clear();
            for (AbstractShortestSequenceBuilder.BackLink<V, A, Integer> backlink = list; backlink != null; backlink = backlink.getParent()) {
                path.addFirst(backlink.getVertex());
            }
            vertexPaths.add(ImmutableLists.copyOf(path));
        }
        return vertexPaths;
    }

    private void searchAll(@NonNull AbstractShortestSequenceBuilder.BackLink<V, A, Integer> start,
                           @NonNull Predicate<V> goal,
                           @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                           @NonNull List<AbstractShortestSequenceBuilder.BackLink<V, A, Integer>> backlinks) {
        Deque<AbstractShortestSequenceBuilder.BackLink<V, A, Integer>> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            AbstractShortestSequenceBuilder.BackLink<V, A, Integer> current = stack.pop();
            if (goal.test(current.getVertex())) {
                backlinks.add(current);
            }
            if (current.getCost() > 1) {
                for (Arc<V, A> v : nextArcsFunction.apply(current.getVertex())) {
                    AbstractShortestSequenceBuilder.BackLink<V, A, Integer> newPath = new AbstractShortestSequenceBuilder.BackLink<>(v.getEnd(), v.getData(), current, current.getCost() - 1);
                    stack.push(newPath);
                }
            }
        }
    }

}
