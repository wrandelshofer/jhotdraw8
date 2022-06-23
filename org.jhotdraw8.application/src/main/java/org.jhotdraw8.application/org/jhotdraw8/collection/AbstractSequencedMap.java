/*
 * @(#)AbstractSequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.SequencedCollectionFacade;
import org.jhotdraw8.collection.facade.SequencedSetFacadeFacade;
import org.jhotdraw8.collection.mapped.MappedIterator;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
    public @NonNull SequencedSet<K> keySet() {
        return createKeySet(this);
    }


    @SuppressWarnings({"SuspiciousMethodCalls"})
    public static <K, V> @NonNull SequencedSet<K> createKeySet(@NonNull SequencedMap<K, V> m) {
        return new SequencedSetFacadeFacade<>(
                () -> new MappedIterator<>(m.entrySet().iterator(), Entry::getKey),
                () -> new MappedIterator<>(m.reversed().entrySet().iterator(), Entry::getKey),
                m::size,
                m::containsKey,
                m::clear,
                o -> {
                    if (m.containsKey(o)) {
                        m.remove(o);
                        return true;
                    }
                    return false;
                },
                m::firstKey,
                m::lastKey,
                null, null, null, null
        );
    }

    @Override
    public @NonNull SequencedCollection<V> values() {
        return createValues(this);
    }

    public static <K, V> @NonNull SequencedCollection<V> createValues(@NonNull SequencedMap<K, V> m) {
        return new SequencedCollectionFacade<>(
                () -> new MappedIterator<>(m.entrySet().iterator(), Entry::getValue),
                () -> new MappedIterator<>(m.reversed().entrySet().iterator(), Entry::getValue),
                m::size,
                m::containsValue,
                m::clear,
                (o) -> {
                    for (Entry<K, V> entry : m.entrySet()) {
                        if (Objects.equals(entry.getValue(), o)) {
                            m.remove(entry.getKey());
                            return true;
                        }
                    }
                    return false;
                },
                () -> {
                    Entry<K, V> entry = m.firstEntry();
                    if (entry == null) {
                        throw new NoSuchElementException();
                    }
                    return entry.getValue();
                },
                () -> {
                    Entry<K, V> entry = m.lastEntry();
                    if (entry == null) {
                        throw new NoSuchElementException();
                    }
                    return entry.getValue();
                },
                null, null
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
