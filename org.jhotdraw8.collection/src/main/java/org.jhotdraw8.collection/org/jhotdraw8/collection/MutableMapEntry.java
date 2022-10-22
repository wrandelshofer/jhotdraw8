/*
 * @(#)MutableMapEntry.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractMap;
import java.util.function.BiConsumer;

public class MutableMapEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {
    private final static long serialVersionUID = 0L;
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
