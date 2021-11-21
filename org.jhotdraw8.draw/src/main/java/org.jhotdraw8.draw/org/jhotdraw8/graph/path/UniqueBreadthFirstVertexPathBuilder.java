/*
 * @(#)UniquePathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating unique paths from a directed graph.
 * <p>
 * The builder searches for unique paths using a breadth-first search.<br>
 * Returns only a path if it is unique.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class UniqueBreadthFirstVertexPathBuilder<V, A> extends AbstractVertexSequenceBuilder<V> {


    public UniqueBreadthFirstVertexPathBuilder(@NonNull Function<V, Iterable<V>> nextNodesFunction) {
        this(Integer.MAX_VALUE, nextNodesFunction);
    }

    public UniqueBreadthFirstVertexPathBuilder(int maxLength, @NonNull Function<V, Iterable<V>> nextNodesFunction) {
        super(maxLength, nextNodesFunction);
    }

    @Override
    protected @Nullable BackLink<V> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull Function<V, Iterable<V>> nextNodesFunction, int maxLength) {
        return search(startVertices, goalPredicate, nextNodesFunction, new HashSet<>(16)::add, maxLength);
    }


    protected @Nullable BackLink<V> search(@NonNull Iterable<V> starts,
                                           @NonNull Predicate<V> goal,
                                           @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                           @NonNull AddToSet<V> visited,
                                           int maxLength) {
        Queue<BackLink<V>> queue = new ArrayDeque<>(16);
        for (V start : starts) {
            BackLink<V> rootBackLink = new BackLink<>(start, null, maxLength);
            if (visited.add(start)) {
                queue.add(rootBackLink);
            }

        }

        BackLink<V> found = null;
        Set<V> nonUnique = new LinkedHashSet<>();
        while (!queue.isEmpty()) {
            BackLink<V> node = queue.remove();
            if (goal.test(node.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                found = node;
            }
            if (node.getRemainingLength() > 0) {
                for (V next : nextNodesFunction.apply(node.getVertex())) {
                    if (visited.add(next)) {
                        BackLink<V> backLink = new BackLink<V>(next, node, node.getRemainingLength() - 1);
                        queue.add(backLink);
                    } else {
                        nonUnique.add(next);
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (BackLink<V> node = found; node != null; node = node.getParent()) {
            if (nonUnique.contains(node.getVertex())) {
                return null;// path is not unique!
            }
        }
        return found;
    }

}
