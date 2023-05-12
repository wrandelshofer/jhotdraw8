/*
 * @(#)KeySpliterator.java
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
 * Data iterator over a CHAMP trie.
 */
public class ChampSpliterator<K, E> extends AbstractEnumeratorSpliterator<E> {
    private final @NonNull Function<K, E> mappingFunction;
    private final @NonNull Deque<Object> queue = new ArrayDeque<>(Node.MAX_DEPTH);
    private Node<K> node;
    private int dataRemaining;
    private K current;

    @SuppressWarnings("unchecked")
    public ChampSpliterator(@NonNull Node<K> root, @Nullable Function<K, E> mappingFunction, int characteristics, long size) {
        super(size, characteristics);
        node = root;
        dataRemaining = node.dataArity();
        this.mappingFunction = mappingFunction == null ? i -> (E) i : mappingFunction;
    }

    @Override
    public E current() {
        return mappingFunction.apply(current);
    }

    @Override
    public boolean moveNext() {
        // Performance: We carefully avoid de-referencing nodes before we actually need them.
        //              We use a queue instead of a stack, so that the CPU can prefetch the nodes.
        //              With stack.addFirst() instead of stack.addLast() the performance is 50% slower!
        if (node == null) {
            return false;
        }
        while (true) {
            if (dataRemaining > 0) {
                // De-referencing a node which we already have fetched from memory is fast.
                current = node.getData(--dataRemaining);
                return true;
            }
            // We do not de-reference the nodes, because we do not want to wait until they are fetched from memory!
            for (int i = node.nodeArity() - 1; i >= 0; i--) {
                queue.addFirst(node.getNodeRaw(i));
            }
            if (queue.isEmpty()) {
                node = null;
                return false;
            }
            node = (Node<K>) queue.removeFirst();
            // We do de-reference the node for the first time. Hopefully the CPU was smart enough to prefetch it.
            dataRemaining = node.dataArity();
        }
    }
}
