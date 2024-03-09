package org.jhotdraw8.icollection.sequenced;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;

/**
 * Provides a reversed view on a {@link SequencedMap}.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class ReversedSequencedMapView<K, V> extends AbstractMap<K, V> implements SequencedMap<K, V> {
    private final @NonNull SequencedMap<K, V> src;

    /**
     * Constructs a new instance.
     *
     * @param src the source map
     */
    public ReversedSequencedMapView(@NonNull SequencedMap<K, V> src) {
        this.src = src;
    }

    @Override
    public boolean equals(Object o) {
        return src.equals(o);
    }

    @Override
    public int hashCode() {
        return src.hashCode();
    }

    @Override
    public int size() {
        return src.size();
    }

    @Override
    public boolean isEmpty() {
        return src.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return src.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return src.containsKey(key);
    }

    @Override
    public V get(Object key) {
        return src.get(key);
    }

    @Override
    public V remove(Object key) {
        return src.remove(key);
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return src.sequencedEntrySet().reversed();
    }

    @Override
    public @Nullable V put(K key, V value) {
        if (src.containsKey(key)) {
            return src.put(key, value);
        } else {
            src.putFirst(key, value);
            return null;
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        return src.remove(key, value);
    }


    @Override
    public void clear() {
        src.clear();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return src.sequencedKeySet().reversed();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return src.sequencedValues().reversed();
    }

    @Override
    public @NonNull SequencedMap<K, V> reversed() {
        return src;
    }

    @Override
    public @NonNull SequencedSet<K> sequencedKeySet() {
        return src.sequencedKeySet().reversed();
    }

    @Override
    public @NonNull SequencedCollection<V> sequencedValues() {
        return src.sequencedValues().reversed();
    }

    @Override
    public @NonNull SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return src.sequencedEntrySet().reversed();
    }

    @Nullable
    @Override
    public Entry<K, V> firstEntry() {
        return src.lastEntry();
    }

    @Nullable
    @Override
    public Entry<K, V> lastEntry() {
        return src.firstEntry();
    }

    @Override
    public @Nullable Entry<K, V> pollFirstEntry() {
        return src.pollLastEntry();
    }

    @Override
    public @Nullable Entry<K, V> pollLastEntry() {
        return src.pollFirstEntry();
    }

    @Override
    public @Nullable V putFirst(K k, V v) {
        return src.putLast(k, v);
    }

    @Override
    public @Nullable V putLast(K k, V v) {
        return src.putFirst(k, v);
    }
}
