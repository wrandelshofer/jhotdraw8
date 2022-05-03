/*
 * @(#)SequencedMapEntry.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champtrie;

import java.util.AbstractMap;

class SequencedMapEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {
    private final int sequenceNumber;

    public SequencedMapEntry(K key, V value, int sequenceNumber) {
        super(key, value);
        this.sequenceNumber = sequenceNumber;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String toString() {
        return getKey() + "=" + getValue() + "#" + ((long) sequenceNumber - Integer.MIN_VALUE);
    }
}
