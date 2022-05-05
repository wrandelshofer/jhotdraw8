/*
 * @(#)KeyIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import java.util.AbstractMap;
import java.util.Iterator;

public class KeyIterator<K, V> extends BaseTrieIterator<K, V>
        implements Iterator<K> {


    public KeyIterator(Node<K, V> rootNode, int entryLength) {
        super(rootNode, entryLength);
    }

    @Override
    public K next() {
        return nextEntry(AbstractMap.SimpleImmutableEntry::new).getKey();
    }
}
