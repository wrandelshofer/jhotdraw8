/*
 * @(#)ImmutableChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;

/**
 * Implements an immutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> entries</li>
 *     <li>allows null keys and null values</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put: O(1)</li>
 *     <li>remove: O(1)</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other maps.
 * <p>
 * If a write operation is performed on a node, then this map creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1).
 * <p>
 * This map can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}}. The mutable copy shares its nodes
 * with this map, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * All operations on this set can be performed concurrently, without a need for
 * synchronisation.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. BSD-2-Clause License</dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class ChampMap<K, V> extends BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>>
        implements ImmutableMap<K, V>, Serializable {
    private static final @NonNull ChampMap<?, ?> EMPTY = new ChampMap<>(BitmapIndexedNode.emptyNode(), 0);
    private final static long serialVersionUID = 0L;
    private final int size;

    ChampMap(@NonNull BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        this.size = size;
    }


    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ChampMap<K, V>) ((ChampMap<K, V>) ChampMap.EMPTY).putAll(map);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ((ChampMap<K, V>) ChampMap.EMPTY).putAll(map);
    }

    static <V, K> boolean keyEquals(AbstractMap.SimpleImmutableEntry<K, V> a, AbstractMap.SimpleImmutableEntry<K, V> b) {
        return Objects.equals(a.getKey(), b.getKey());
    }

    static <V, K> int keyHash(AbstractMap.SimpleImmutableEntry<K, V> e) {
        return Objects.hashCode(e.getKey());
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampMap<K, V> of() {
        return (ChampMap<K, V>) ChampMap.EMPTY;
    }

    /**
     * Returns an immutable map that contains the provided entries.
     *
     * @param entries map entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return an immutable map of the provided entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return (ChampMap<K, V>) of().putAll(entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull ChampMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return find(new AbstractMap.SimpleImmutableEntry<>(key, null), Objects.hashCode(key), 0,
                ChampMap::keyEquals) != Node.NO_DATA;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof ChampMap) {
            ChampMap<?, ?> that = (ChampMap<?, ?>) other;
            return size == that.size && equivalent(that);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        K key = (K) o;
        Object result = find(new AbstractMap.SimpleImmutableEntry<>(key, null), Objects.hashCode(key), 0, ChampMap::keyEquals);
        return result == Node.NO_DATA || result == null ? null : ((AbstractMap.SimpleImmutableEntry<K, V>) result).getValue();
    }

    @NonNull
    private BiFunction<AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>, AbstractMap.SimpleImmutableEntry<K, V>> getUpdateFunction() {
        return (oldv, newv) -> Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iterableToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return (Iterator<Map.Entry<K, V>>) (Iterator<?>) new KeyIterator<>(this, null);
    }

    @Override
    public @NonNull ChampMap<K, V> put(@NonNull K key, @Nullable V value) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> newRootNode = update(null, new AbstractMap.SimpleImmutableEntry<>(key, value),
                keyHash, 0, details,
                getUpdateFunction(), ChampMap::keyEquals, ChampMap::keyHash);
        if (details.isModified()) {
            if (details.isReplaced()) {
                return new ChampMap<>(newRootNode, size);
            }
            return new ChampMap<>(newRootNode, size + 1);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ChampMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof MutableChampMap)) {
            return ((MutableChampMap<K, V>) m).toImmutable();
        }
        return putAll(m.entrySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ChampMap<K, V> putAll(@NonNull ImmutableMap<? extends K, ? extends V> m) {
        if (m instanceof ChampMap) {
            ChampMap<K, V> b = (ChampMap<K, V>) m;
            if (b.size() > size()) {
                return b.putAll(readOnlyEntrySet());
            }
        }
        return putAll(m.readOnlyEntrySet());
    }

    @Override
    public @NonNull ChampMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        MutableChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details =
                    t.putAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampMap<K, V> remove(@NonNull K key) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<AbstractMap.SimpleImmutableEntry<K, V>> newRootNode =
                remove(null, new AbstractMap.SimpleImmutableEntry<>(key, null), keyHash, 0, details,
                        ChampMap::keyEquals);
        if (details.isModified()) {
            return new ChampMap<>(newRootNode, size - 1);
        }
        return this;
    }

    @Override
    public @NonNull ChampMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }
        MutableChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<AbstractMap.SimpleImmutableEntry<K, V>> details = t.removeAndGiveDetails(key);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ChampMap<K, V> retainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        MutableChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull Spliterator<Map.Entry<K, V>> spliterator() {
        return Spliterators.spliterator(iterator(), size, Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    /**
     * Creates a mutable copy of this map.
     *
     * @return a mutable CHAMP map
     */
    @Override
    public @NonNull MutableChampMap<K, V> toMutable() {
        return new MutableChampMap<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    private @NonNull Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return ChampMap.of().putAll(deserialized);
        }
    }
}