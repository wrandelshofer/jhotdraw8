/*
 * @(#)SequencedTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champmap;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Sequenced entry iterator.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SequencedEntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {
    private final @NonNull PriorityQueue<SequencedMapEntry<K, V>> queue;
    private SequencedMapEntry<K, V> current;
    private boolean canRemove;
    private final @Nullable Consumer<K> persistentRemoveFunction;

    @SuppressWarnings("unchecked")
    public SequencedEntryIterator(int size, @NonNull Node<K, V> rootNode, int entryLength, int numFields, boolean reversed, @Nullable Consumer<K> persistentRemoveFunction, BiConsumer<K, V> persistentPutIfPresentFunction) {
        this.persistentRemoveFunction = persistentRemoveFunction;
        Comparator<SequencedMapEntry<K, V>> comparator = Comparator.comparingInt(SequencedMapEntry::getSequenceNumber);
        queue = new PriorityQueue<>(Math.max(1, size), reversed ? comparator.reversed() : comparator);
        for (EntryIterator<K, V> it = new EntryIterator<>(rootNode, entryLength, numFields, null, persistentPutIfPresentFunction); it.hasNext(); ) {
            queue.add(it.next());
        }
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public Map.Entry<K, V> next() {
        current = queue.remove();
        canRemove = true;
        return current;
    }

    @Override
    public void remove() {
        if (persistentRemoveFunction == null) {
            throw new UnsupportedOperationException("remove");
        }
        if (!canRemove) {
            throw new IllegalStateException();
        }
        persistentRemoveFunction.accept(current.getKey());
        canRemove = false;
    }
}
