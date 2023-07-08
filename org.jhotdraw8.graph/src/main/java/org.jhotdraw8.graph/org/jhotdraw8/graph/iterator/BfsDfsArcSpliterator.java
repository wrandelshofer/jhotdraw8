/*
 * @(#)ArcEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.AbstractEnumerator;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * Enumerates arcs in a graph starting from a root vertex in
 * breadth-first-order or in depth-first-order.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class BfsDfsArcSpliterator<V, A> extends AbstractEnumerator<Arc<V, A>> {

    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextFunction;
    private final @NonNull Deque<Arc<V, A>> deque;
    private final @NonNull AddToSet<Arc<V, A>> visited;
    private final boolean dfs;

    /**
     * Creates a new instance.
     *
     * @param nextArcsFunction the nextFunction
     * @param root             the root vertex
     * @param dfs              whether to perform depth-first-search instead of breadth-first-search
     */
    public BfsDfsArcSpliterator(Function<V, Iterable<Arc<V, A>>> nextArcsFunction, V root, boolean dfs) {
        this(nextArcsFunction, root, new HashSet<>()::add, dfs);
    }


    /**
     * Creates a new instance.
     *
     * @param nextFunction the function that returns the next vertices of a given vertex
     * @param root         the root vertex
     * @param visited      a function that adds an arc to a set, and returns true if the arc was not yet in the set
     * @param dfs whether to perform depth-first-search instead of breadth-first-search
     */
    public BfsDfsArcSpliterator(@Nullable Function<V, Iterable<Arc<V, A>>> nextFunction, @Nullable V root, @Nullable AddToSet<Arc<V, A>> visited, boolean dfs) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        this.dfs = dfs;
        Objects.requireNonNull(nextFunction, "nextFunction");
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(visited, "visited");
        this.nextFunction = nextFunction;
        deque = new ArrayDeque<>(16);
        this.visited = visited;
        for (Arc<V, A> next : nextFunction.apply(root)) {
            if (visited.add(next)) {
                deque.addLast(next);
            }
        }
    }

    @Override
    public boolean moveNext() {
        current = dfs ? deque.pollLast() : deque.pollFirst();
        if (current == null) {
            return false;
        }
        for (Arc<V, A> next : nextFunction.apply(current.getEnd())) {
            if (visited.add(next)) {
                deque.addLast(next);
            }
        }
        return true;
    }
}
