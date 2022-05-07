/*
 * @(#)SequencedTrieIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;


import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

public class SequencedTrieIterator<K, V> {
    final PriorityQueue<SequencedMapEntry<K, V>> queue;
    SequencedMapEntry<K, V> current;
    boolean canRemove;

    @SuppressWarnings("unchecked")
    public SequencedTrieIterator(int size, Node<K, V> rootNode, int entryLength, boolean reversed) {
        Comparator<SequencedMapEntry<K, V>> comparator = Comparator.comparingInt(SequencedMapEntry::getSequenceNumber);
        queue = new PriorityQueue<>(Math.max(1, size), reversed ? comparator.reversed() : comparator);
        for (BaseTrieIterator<K, V> it = new BaseTrieIterator<>(rootNode, entryLength); it.hasNext(); ) {
            queue.add(it.nextValueNode.getKeyValueSeqEntry(it.nextValueCursor++, SequencedMapEntry::new, entryLength));
        }
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    protected Map.Entry<K, V> nextEntry() {
        current = queue.remove();
        canRemove = true;
        return current;
    }
}
