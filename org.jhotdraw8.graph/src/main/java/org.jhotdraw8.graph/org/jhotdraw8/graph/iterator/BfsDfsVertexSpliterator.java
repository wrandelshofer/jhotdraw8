/*
 * @(#)VertexEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.AbstractEnumerator;
import org.jhotdraw8.graph.algo.AddToSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * Enumerates vertices in a graph starting from a root vertex in
 * breadth-first-order or in depth-first-order.
 * <p>
 * References:
 * <dl>
 *     <dt>Robert Sedgewick, Kevin Wayne. (2011)</dt>
 *     <dd>Algorithms, 4th Edition. Chapter 4. Algorithm 4.1 Depth-First Search; Algorithm 4.2. Breadth-First Search.
 *          <a href="https://www.math.cmu.edu/~af1p/Teaching/MCC17/Papers/enumerate.pdf">math.cmu.edu</a>
 *     </dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class BfsDfsVertexSpliterator<V> extends AbstractEnumerator<V> {

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
    public BfsDfsVertexSpliterator(@NonNull Function<V, Iterable<V>> nextFunction, @NonNull V root, boolean dfs) {
        this(nextFunction, root, new HashSet<>()::add, dfs);
    }

    /**
     * Creates a new instance.
     *
     * @param nextFunction a function that returns the next vertices for a given vertex
     * @param root         the root vertex
     * @param visited      a function that adds a provided vertex to a set, and returns true if the vertex was not in the set.
     *                     If the graph is known to be a tree, the function can always return true.
     * @param dfs          whether to enumerate depth-first instead of breadth-first
     */
    public BfsDfsVertexSpliterator(@NonNull Function<V, Iterable<V>> nextFunction, @NonNull V root, @NonNull AddToSet<V> visited, boolean dfs) {
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
