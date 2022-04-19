/*
 * @(#)IndexedVertexEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractIntEnumerator;
import org.jhotdraw8.collection.DenseIntSet8Bit;
import org.jhotdraw8.collection.IntArrayDeque;
import org.jhotdraw8.collection.IntEnumerator;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.Objects;
import java.util.function.Function;

/**
 * BreadthFirstSpliterator for graphs with indexed vertices.
 *
 * @author Werner Randelshofer
 */
public class IndexedVertexEnumerator extends AbstractIntEnumerator {

    private final @NonNull Function<Integer, IntEnumerator> nextFunction;
    private final @NonNull IntArrayDeque deque;
    private final @NonNull AddToIntSet visited;
    private final boolean dfs;

    /**
     * Creates a new instance.
     *
     * @param nextFunction the nextFunction
     * @param root         the root vertex
     * @param vertexCount  the vertex count
     * @param dfs
     */
    public IndexedVertexEnumerator(@NonNull Function<Integer, IntEnumerator> nextFunction,
                                   int root,
                                   int vertexCount, boolean dfs) {
        this(nextFunction, root, new DenseIntSet8Bit(vertexCount)::addAsInt, dfs);
    }

    /**
     * Creates a new instance.
     *
     * @param nextFunction the nextFunction
     * @param root         the root vertex
     * @param dfs
     */
    public IndexedVertexEnumerator(@NonNull Function<Integer, IntEnumerator> nextFunction, int root, @NonNull AddToIntSet visited, boolean dfs) {
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
        for (IntEnumerator it = nextFunction.apply(current); it.moveNext(); ) {
            int next = it.currentAsInt();
            if (visited.addAsInt(next)) {
                deque.addLastAsInt(next);
            }
        }
        return true;
    }
}
