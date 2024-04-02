/*
 * @(#)MutableMapEntry.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;

import java.io.Serial;
import java.util.AbstractMap;
import java.util.function.BiConsumer;

/**
 * A map entry that supports mutation of the map that contains the entry.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class MutableMapEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings({"serial", "RedundantSuppression"})//This field is conditionally serializable
    private final @NonNull BiConsumer<K, V> putFunction;

    public MutableMapEntry(@NonNull BiConsumer<K, V> putFunction, K key, V value) {
        super(key, value);
        this.putFunction = putFunction;
    }

    @Override
    public V setValue(V value) {
        V oldValue = super.setValue(value);
        putFunction.accept(getKey(), value);
        return oldValue;
    }
}
