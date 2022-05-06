/*
 * @(#)SequencedKeyIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import java.util.Iterator;

public class SequencedKeyIterator<K, V> extends SequencedTrieIterator<K, V>
        implements Iterator<K> {


    public SequencedKeyIterator(int size, Node<K, V> rootNode, int entryLength, boolean reversed) {
        super(size, rootNode, entryLength, reversed);
    }

    @Override
    public K next() {
        return nextEntry().getKey();
    }
}
