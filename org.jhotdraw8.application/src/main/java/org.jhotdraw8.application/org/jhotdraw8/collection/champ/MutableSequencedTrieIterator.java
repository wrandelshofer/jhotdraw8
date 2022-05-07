/*
 * @(#)MutableSequencedTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class MutableSequencedTrieIterator<K, V> implements Iterator<Map.Entry<K, V>> {
    final PriorityQueue<SequencedMapEntry<K, V>> queue;
    SequencedMapEntry<K, V> current;
    boolean canRemove;
    final @NonNull IntSupplier modCountFunction;
    private int expectedModCount;
    private final @NonNull Consumer<K> removeFunction;

    public MutableSequencedTrieIterator(int size, Node<K, V> rootNode, int entryLength, boolean reversed, @NonNull IntSupplier modCountFunction, @NonNull Consumer<K> removeFunction) {
        this.removeFunction = removeFunction;
        Comparator<SequencedMapEntry<K, V>> comparator = Comparator.comparingInt(SequencedMapEntry::getSequenceNumber);
        queue = new PriorityQueue<>(Math.max(1, size), reversed ? comparator.reversed() : comparator);
        for (BaseTrieIterator<K, V> it = new BaseTrieIterator<>(rootNode, entryLength); it.hasNext(); ) {
            queue.add(it.nextValueNode.getKeyValueSeqEntry(it.nextValueCursor++, SequencedMapEntry::new, entryLength));
        }
        this.modCountFunction = modCountFunction;
        this.expectedModCount = modCountFunction.getAsInt();
    }

    @Override
    public boolean hasNext() {
        if (expectedModCount != modCountFunction.getAsInt()) {
            throw new ConcurrentModificationException();
        }
        return !queue.isEmpty();
    }

    @Override
    public Map.Entry<K, V> next() {
        if (expectedModCount != modCountFunction.getAsInt()) {
            throw new ConcurrentModificationException();
        }
        current = queue.remove();
        canRemove = true;
        return current;
    }

    @Override
    public void remove() {
        if (!canRemove) {
            throw new IllegalStateException();
        }
        if (expectedModCount != modCountFunction.getAsInt()) {
            throw new ConcurrentModificationException();
        }
        Map.Entry<K, V> toRemove = current;
        removeFunction.accept(toRemove.getKey());
        canRemove = false;
        current = null;
        this.expectedModCount = modCountFunction.getAsInt();
    }
}
