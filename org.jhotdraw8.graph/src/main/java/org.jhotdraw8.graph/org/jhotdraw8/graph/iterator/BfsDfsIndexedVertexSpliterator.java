/*
 * @(#)IndexedVertexEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.collection.enumerator.AbstractIntEnumerator;
import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.primitive.DenseIntSet8Bit;
import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.graph.algo.AddToIntSet;

import java.util.Objects;
import java.util.function.Function;

/**
 * Enumerates vertices in a graph with indexed vertices starting from a root vertex in
 * breadth-first-order or in depth-first-order.
 *
 * @author Werner Randelshofer
 */
public class BfsDfsIndexedVertexSpliterator extends AbstractIntEnumerator {

    private final Function<Integer, Enumerator.OfInt> nextFunction;
    private final IntArrayDeque deque;
    private final AddToIntSet visited;
    private final boolean dfs;

    /**
     * Creates a new instance.
     *
     * @param nextFunction the nextFunction
     * @param root         the root vertex
     * @param vertexCount  the vertex count
     * @param dfs          whether to perform depth-first-search instead of breadth-first-search
     */
    public BfsDfsIndexedVertexSpliterator(Function<Integer, Enumerator.OfInt> nextFunction,
                                          int root,
                                          int vertexCount, boolean dfs) {
        this(nextFunction, root, new DenseIntSet8Bit(vertexCount)::addAsInt, dfs);
    }

    /**
     * Creates a new instance.
     *
     * @param nextFunction the nextFunction
     * @param root         the root vertex
     * @param dfs          whether to perform depth-first-search instead of breadth-first-search
     */
    public BfsDfsIndexedVertexSpliterator(Function<Integer, Enumerator.OfInt> nextFunction, int root, AddToIntSet visited, boolean dfs) {
        super(Long.MAX_VALUE, NONNULL | ORDERED | DISTINCT | NONNULL);
        this.dfs = dfs;
        Objects.requireNonNull(nextFunction, "nextFunction");
        this.nextFunction = nextFunction;
        deque = new IntArrayDeque(16);
        this.visited = visited;
        if (visited.addAsInt(root)) {
            deque.addLastAsInt(root);
        }
    }

    @Override
    public boolean moveNext() {
        if (deque.isEmpty()) {
            return false;
        }
        current = dfs ? deque.removeLastAsInt() : deque.removeFirstAsInt();
        for (Enumerator.OfInt it = nextFunction.apply(current); it.moveNext(); ) {
            int next = it.currentAsInt();
            if (visited.addAsInt(next)) {
                deque.addLastAsInt(next);
            }
        }
        return true;
    }
}
