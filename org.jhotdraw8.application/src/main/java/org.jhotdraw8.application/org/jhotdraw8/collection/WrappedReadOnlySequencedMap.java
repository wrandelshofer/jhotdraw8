/*
 * @(#)WrappedReadOnlyMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Wraps map functions in the {@link ReadOnlySequencedMap} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedReadOnlySequencedMap<K, V> extends WrappedReadOnlyMap<K, V>
        implements ReadOnlySequencedMap<K, V> {
    private final @NonNull Supplier<Map.Entry<K, V>> firstEntryFunction;
    private final @NonNull Supplier<Map.Entry<K, V>> lastEntryFunction;

    public WrappedReadOnlySequencedMap(SequencedMap<K, V> target) {
        super(target);
        this.firstEntryFunction = target::firstEntry;
        this.lastEntryFunction = target::lastEntry;
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
        return firstEntryFunction.get();
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        return lastEntryFunction.get();
    }
}
