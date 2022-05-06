/*
 * @(#)SequencedKeyIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import java.util.Iterator;

public class SequencedKeyIterator<K, V> extends SequencedTrieIterator<K, V>
        implements Iterator<K> {


    public SequencedKeyIterator(int size, Node<K, V> rootNode, int entryLength) {
        super(size, rootNode, entryLength);
    }

    @Override
    public K next() {
        return nextEntry().getKey();
    }
}
