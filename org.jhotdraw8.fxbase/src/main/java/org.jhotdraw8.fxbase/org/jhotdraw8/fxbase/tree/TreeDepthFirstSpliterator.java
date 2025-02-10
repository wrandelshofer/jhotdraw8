/*
 * @(#)TreeDepthFirstSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.collection.enumerator.AbstractEnumerator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Function;

/**
 * DepthFirstSpliterator for a tree structure.
 *
 * @param <V> the vertex data type
 */
public class TreeDepthFirstSpliterator<V> extends AbstractEnumerator<V> {

    private final Function<V, Iterable<V>> nextFunction;
    private final Deque<V> deque;

    /**
     * Creates a new instance.
     *
     * @param nextFunction the nextFunction
     * @param root         the root vertex
     */
    public TreeDepthFirstSpliterator(Function<V, Iterable<V>> nextFunction, V root) {
        super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL);
        Objects.requireNonNull(nextFunction, "nextFunction");
        Objects.requireNonNull(root, "root");
        this.nextFunction = nextFunction;
        deque = new ArrayDeque<>(16);
        deque.add(root);
    }

    @Override
    public boolean moveNext() {
        current = deque.pollLast();
        if (current == null) {
            return false;
        }
        for (V next : nextFunction.apply(current)) {
            deque.addLast(next);
        }
        return true;
    }
}
