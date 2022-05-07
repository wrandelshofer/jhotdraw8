/*
 * @(#)AbstractSequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Objects;

/**
 * Abstract base class for {@link SequencedMap}s.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public abstract class AbstractSequencedMap<K, V> extends AbstractMap<K, V> implements SequencedMap<K, V> {
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
    @SuppressWarnings("unchecked")
    public SequencedSet<K> keySet() {
        return new WrappedSequencedSet<>(
                () -> new MappedIterator<>(entrySet().iterator(), Entry::getKey),
                AbstractSequencedMap.this::size,
                AbstractSequencedMap.this::containsKey,
                AbstractSequencedMap.this::clear,
                AbstractSequencedMap.this::removeKey,
                AbstractSequencedMap.this::firstKey,
                AbstractSequencedMap.this::lastKey
        );
    }

    @Override
    public SequencedCollection<V> values() {
        return new WrappedSequencedCollection<>(
                () -> new MappedIterator<>(entrySet().iterator(), Entry::getValue),
                AbstractSequencedMap.this::size,
                AbstractSequencedMap.this::containsValue,
                AbstractSequencedMap.this::clear,
                AbstractSequencedMap.this::removeValue,
                () -> firstEntry().getValue(),
                () -> lastEntry().getValue()
        );
    }
}
