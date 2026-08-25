/*
 * @(#)EditableMapEntry.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl;

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.AbstractMap;
import java.util.function.BiConsumer;

/// A map entry that supports a put operation.
///
/// @param <K> the key type
/// @param <V> the value type
public class EditableMapEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {
    @Serial
    private static final long serialVersionUID = 0L;
    private final int sequenceNumber;
    @SuppressWarnings({"serial", "RedundantSuppression"})
    private @Nullable BiConsumer<K, V> putIfPresentFunction;// This map entry is actually not serializable

    public EditableMapEntry(K key, V value, int sequenceNumber) {
        this(key, value, sequenceNumber, null);
    }

    public EditableMapEntry(K key, V value, int sequenceNumber, @Nullable BiConsumer<K, V> putIfPresentFunction) {
        super(key, value);
        this.sequenceNumber = sequenceNumber;
        this.putIfPresentFunction = putIfPresentFunction;
    }

    public EditableMapEntry(K key, V value, @Nullable BiConsumer<K, V> putIfPresentFunction) {
        this(key, value, 0, putIfPresentFunction);
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setPutIfPresentFunction(@Nullable BiConsumer<K, V> putIfPresentFunction) {
        this.putIfPresentFunction = putIfPresentFunction;
    }

    @Override
    public V setValue(V value) {
        if (putIfPresentFunction == null) {
            throw new UnsupportedOperationException();
        }
        putIfPresentFunction.accept(getKey(), value);
        return super.setValue(value);
    }
}
