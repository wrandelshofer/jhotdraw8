package org.jhotdraw8.icollection.navigable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.IntSupplier;

/**
 * Provides a descending view on a {@link NavigableMap}.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class DescendingNavigableMapView<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V> {
    private final @NonNull NavigableMap<K, V> src;
    private final @NonNull IntSupplier modCount;

    /**
     * Constructs a new instance.
     *
     * @param src the source map
     */
    public DescendingNavigableMapView(@NonNull NavigableMap<K, V> src, @NonNull IntSupplier modCount) {
        this.src = src;
        this.modCount = modCount;
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
    public @NonNull NavigableMap<K, V> reversed() {
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

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return src.higherEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return src.higherKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return src.ceilingEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return src.ceilingKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return src.floorEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return src.floorKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return src.lowerEntry(key);
    }

    @Override
    public K higherKey(K key) {
        return src.lowerKey(key);
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
    public NavigableMap<K, V> descendingMap() {
        return src;
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new DescendingNavigableSetView<>(src.navigableKeySet(), modCount);
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return new DescendingNavigableSetView<>(src.navigableKeySet(), modCount);
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<? super K> comparator() {
        return src.comparator().reversed();
    }

    @Override
    public @NonNull SortedMap<K, V> subMap(K fromKey, K toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull SortedMap<K, V> headMap(K toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull SortedMap<K, V> tailMap(K fromKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public K firstKey() {
        return src.lastKey();
    }

    @Override
    public K lastKey() {
        return src.firstKey();
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
