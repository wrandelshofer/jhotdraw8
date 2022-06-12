/*
 * @(#)AbstractSequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * Abstract base class for {@link SequencedMap}s.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class AbstractSequencedMap<K, V> extends AbstractMap<K, V> implements SequencedMap<K, V>, ReadOnlySequencedMap<K, V> {
    public AbstractSequencedMap() {
    }

    boolean removeKey(final @Nullable Object o) {
        if (containsKey(o)) {
            remove(o);
            return true;
        }
        return false;
    }

    boolean removeValue(final @Nullable Object o) {
        for (Entry<K, V> entry : entrySet()) {
            if (Objects.equals(entry.getValue(), o)) {
                remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable Entry<K, V> firstEntry() {
        return SequencedMap.super.firstEntry();
    }

    @Override
    public K firstKey() {
        return SequencedMap.super.firstKey();
    }

    @Override
    public @Nullable Entry<K, V> lastEntry() {
        return SequencedMap.super.lastEntry();
    }

    @Override
    public K lastKey() {
        return SequencedMap.super.lastKey();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull SequencedSet<K> keySet() {
        return new WrappedSequencedSet<>(
                () -> new MappedIterator<>(entrySet().iterator(), Entry::getKey),
                () -> new MappedIterator<>(reversed().entrySet().iterator(), Entry::getKey),
                AbstractSequencedMap.this::size,
                AbstractSequencedMap.this::containsKey,
                AbstractSequencedMap.this::clear,
                AbstractSequencedMap.this::removeKey,
                AbstractSequencedMap.this::firstKey,
                AbstractSequencedMap.this::lastKey, null, null
        );
    }

    @Override
    public @NonNull SequencedCollection<V> values() {
        return new WrappedSequencedCollection<>(
                () -> new MappedIterator<>(entrySet().iterator(), Entry::getValue),
                () -> new MappedIterator<>(reversed().entrySet().iterator(), Entry::getValue),
                AbstractSequencedMap.this::size,
                AbstractSequencedMap.this::containsValue,
                AbstractSequencedMap.this::clear,
                AbstractSequencedMap.this::removeValue,
                () -> firstEntry().getValue(),
                () -> lastEntry().getValue(), null, null
        );
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return SequencedMap.super.getOrDefault(key, defaultValue);
    }

    @Override
    public @NonNull Iterator<Entry<K, V>> iterator() {
        return entrySet().iterator();
    }
}
