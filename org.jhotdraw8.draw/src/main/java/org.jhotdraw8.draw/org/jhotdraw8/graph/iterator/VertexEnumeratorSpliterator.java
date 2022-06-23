/*
 * @(#)VertexEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * Enumerates vertices in a graph starting from a root vertex in
 * breadth-first-order or in depth-first-order.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class VertexEnumeratorSpliterator<V> extends AbstractEnumeratorSpliterator<V> {

    private final @NonNull Function<V, Iterable<V>> nextFunction;
    private final @NonNull Deque<V> deque;
    private final @NonNull AddToSet<V> visited;
    private final boolean dfs;

    /**
     * Creates a new instance.
     *
     * @param nextFunction a function that returns the next vertices for a given vertex
     * @param root         the root vertex
     * @param dfs          whether to enumerate depth-first instead of breadth-first
     */
    public VertexEnumeratorSpliterator(@NonNull Function<V, Iterable<V>> nextFunction, @NonNull V root, boolean dfs) {
        this(nextFunction, root, new HashSet<>()::add, dfs);
    }

    /**
     * Creates a new instance.
     *
     * @param nextFunction a function that returns the next vertices for a given vertex
     * @param root         the root vertex
     * @param dfs          whether to enumerate depth-first instead of breadth-first
     */
    public VertexEnumeratorSpliterator(@NonNull Function<V, Iterable<V>> nextFunction, @NonNull V root, @NonNull AddToSet<V> visited, boolean dfs) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        this.dfs = dfs;
        Objects.requireNonNull(nextFunction, "nextFunction");
        Objects.requireNonNull(root, "root");
        this.nextFunction = nextFunction;
        deque = new ArrayDeque<>(16);
        this.visited = visited;
        if (visited.add(root)) {
            deque.addLast(root);
        }
    }

    @Override
    public boolean moveNext() {
        if (deque.isEmpty()) {
            return false;
        }
        current = dfs ? deque.removeLast() : deque.removeFirst();
        for (V next : nextFunction.apply(current)) {
            if (visited.add(next)) {
                deque.addLast(next);
            }
        }
        return true;
    }
}
