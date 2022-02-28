/*
 * @(#)BreadthFirstSpliterator.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.iterator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractIntEnumeratorSpliterator;
import org.jhotdraw8.collection.IntArrayDeque;
import org.jhotdraw8.collection.IntEnumerator;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.BitSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * BreadthFirstSpliterator for graphs with indexed vertices.
 *
 * @author Werner Randelshofer
 */
public class IndexedBreadthFirstSpliterator extends AbstractIntEnumeratorSpliterator {

    private final @NonNull Function<Integer, IntEnumerator> nextFunction;
    private final @NonNull IntArrayDeque deque;
    private final @NonNull AddToIntSet visited;

    /**
     * Creates a new instance.
     *
     * @param nextFunction the nextFunction
     * @param root         the root vertex
     */
    public IndexedBreadthFirstSpliterator(@NonNull Function<Integer, IntEnumerator> nextFunction, @NonNull int root) {
        this(nextFunction, root, AddToIntSet.addToBitSet(new BitSet()));
    }

    /**
     * Creates a new instance.
     *
     * @param nextFunction the nextFunction
     * @param root         the root vertex
     */
    public IndexedBreadthFirstSpliterator(@NonNull Function<Integer, IntEnumerator> nextFunction, int root, @NonNull AddToIntSet visited) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
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
        current = deque.removeFirstAsInt();
        for (IntEnumerator it = nextFunction.apply(current); it.moveNext(); ) {
            int next = it.currentAsInt();
            if (visited.addAsInt(next)) {
                deque.addLastAsInt(next);
            }
        }
        return true;
    }
}
