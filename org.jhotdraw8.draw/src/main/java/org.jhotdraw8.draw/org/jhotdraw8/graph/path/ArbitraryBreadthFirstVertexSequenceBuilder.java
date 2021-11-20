/*
 * @(#)AnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating arbitrary paths from a directed graph.
 * <p>
 * The builder searches for paths using a breadth-first search.<br>
 * Returns the first path that it finds.<br>
 * Returns nothing if there is no path.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class ArbitraryBreadthFirstVertexSequenceBuilder<V> extends AbstractVertexSequenceBuilder<V> {
    public ArbitraryBreadthFirstVertexSequenceBuilder(@NonNull Function<V, Iterable<V>> nextVertexFunction) {
        this(Integer.MAX_VALUE, nextVertexFunction);
    }

    public ArbitraryBreadthFirstVertexSequenceBuilder(int maxLength, @NonNull Function<V, Iterable<V>> nextVertexFunction) {
        super(maxLength, nextVertexFunction);
    }

    /**
     * Enumerates all vertex paths from start to goal up to the specified maximal path length.
     *
     * @param start     the start vertex
     * @param goal      the goal predicate
     * @param maxLength the maximal length of a path
     * @return the enumerated paths
     */
    public @NonNull List<ImmutableList<V>> findAllVertexSequences(@NonNull V start,
                                                                  @NonNull Predicate<V> goal,
                                                                  int maxLength) {
        List<BackLink<V>> backlinks = new ArrayList<>();
        searchAll(new BackLink<>(start, maxLength, null),
                goal,
                getNextVertexFunction(),
                backlinks);
        List<ImmutableList<V>> vertexPaths = new ArrayList<>(backlinks.size());
        Deque<V> path = new ArrayDeque<>();
        for (BackLink<V> list : backlinks) {
            path.clear();
            for (BackLink<V> backlink = list; backlink != null; backlink = backlink.getParent()) {
                path.addFirst(backlink.getVertex());
            }
            vertexPaths.add(ImmutableLists.copyOf(path));
        }
        return vertexPaths;
    }

    private void searchAll(@NonNull BackLink<V> start, @NonNull Predicate<V> goal,
                           @NonNull Function<V, Iterable<V>> nextVertexFunction,
                           @NonNull List<BackLink<V>> backlinks) {
        Deque<BackLink<V>> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            BackLink<V> current = stack.pop();
            if (goal.test(current.getVertex())) {
                backlinks.add(current);
            }
            if (current.getRemainingLength() > 0) {
                for (V v : nextVertexFunction.apply(current.getVertex())) {
                    BackLink<V> newPath = new BackLink<>(v, current.getRemainingLength() - 1, current);
                    stack.push(newPath);
                }
            }
        }
    }

    @Override
    protected @Nullable BackLink<V> search(@NonNull Iterable<V> startVertices,
                                           @NonNull Predicate<V> goalPredicate,
                                           @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                           int maxLength) {
        return search(startVertices, goalPredicate, new HashSet<V>()::add, maxLength, nextNodesFunction);
    }

    protected @Nullable BackLink<V> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goal,
                                           @NonNull AddToSet<V> visited,
                                           @NonNull Integer maxLength, @NonNull Function<V, Iterable<V>> nextNodesFunction) {
        Queue<BackLink<V>> queue = new ArrayDeque<>(16);
        for (V root : startVertices) {
            BackLink<V> rootBackLink = new BackLink<>(root, maxLength, null);
            if (visited.add(root)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            BackLink<V> node = queue.remove();
            if (goal.test(node.getVertex())) {
                return node;
            }

            if (node.getRemainingLength() > 0) {
                for (V next : nextNodesFunction.apply(node.getVertex())) {
                    if (visited.add(next)) {
                        BackLink<V> backLink = new BackLink<>(next, node.getRemainingLength() - 1, node);
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}