/*
 * @(#)AbstractKeySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * Key iterator over a CHAMP trie.
 * <p>
 * Uses a stack with a fixed maximal depth.
 * Iterates over keys in preorder sequence.
 * <p>
 * Supports the {@code remove} operation. The remove function must
 * create a new version of the trie, so that iterator does not have
 * to deal with structural changes of the trie.
 */
abstract class AbstractChampSpliterator<K, E> extends AbstractEnumeratorSpliterator<E> {
    private final @NonNull Function<K, E> mappingFunction;
    private final @NonNull Deque<StackElement<K>> stack = new ArrayDeque<>(Node.MAX_DEPTH);
    private K current;

    public AbstractChampSpliterator(@NonNull Node<K> root, @Nullable Function<K, E> mappingFunction, int characteristics, long size) {
        super(size, characteristics);
        if (root.nodeArity() + root.dataArity() > 0) {
            stack.push(new StackElement<>(root, isReverse()));
        }
        //noinspection unchecked
        this.mappingFunction = mappingFunction == null ? i -> (E) i : mappingFunction;
    }

    @Override
    public E current() {
        return mappingFunction.apply(current);
    }

    abstract int getNextBitpos(StackElement<K> elem);

    abstract boolean isDone(@NonNull StackElement<K> elem);

    abstract boolean isReverse();

    abstract int moveIndex(@NonNull StackElement<K> elem);

    @Override
    public boolean moveNext() {
        while (!stack.isEmpty()) {
            StackElement<K> elem = stack.peek();
            Node<K> node = elem.node;

            if (node instanceof HashCollisionNode<K> hcn) {
                current = hcn.getData(moveIndex(elem));
                if (isDone(elem)) {
                    stack.pop();
                }
                return true;
            } else if (node instanceof BitmapIndexedNode<K> bin) {
                int bitpos = getNextBitpos(elem);
                elem.map ^= bitpos;
                moveIndex(elem);
                if (isDone(elem)) {
                    stack.pop();
                }
                if ((bin.nodeMap() & bitpos) != 0) {
                    stack.push(new StackElement<>(bin.nodeAt(bitpos), isReverse()));
                } else {
                    current = bin.dataAt(bitpos);
                    return true;
                }
            }
        }
        return false;
    }

    static class StackElement<K> {
        final @NonNull Node<K> node;
        final int size;
        int index;
        int map;

        public StackElement(@NonNull Node<K> node, boolean reverse) {
            this.node = node;
            this.size = node.nodeArity() + node.dataArity();
            this.index = reverse ? size - 1 : 0;
            this.map = (node instanceof BitmapIndexedNode<K> bin)
                    ? (bin.dataMap() | bin.nodeMap()) : 0;
        }
    }
}