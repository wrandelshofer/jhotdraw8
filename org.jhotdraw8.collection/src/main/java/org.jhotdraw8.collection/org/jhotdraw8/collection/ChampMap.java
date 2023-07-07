/*
 * @(#)ChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.ReadOnlySetFacade;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.impl.champ.*;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;

/**
 * Implements the {@link ImmutableMap} interface using a Compressed Hash-Array
 * Mapped Prefix-tree (CHAMP).
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
 * All operations on this map can be performed concurrently, without a need for
 * synchronisation.
 * <p>
 * The immutable version of this map extends from the non-public class
 * {@code ChampBitmapIndexNode}. This design safes 16 bytes for every instance,
 * and reduces the number of redirections for finding an element in the
 * collection by 1.
 * <p>
 * References:
 * <p>
 * Portions of the code in this class has been derived from 'The Capsule Hash Trie Collections Library'.
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class ChampMap<K, V> extends BitmapIndexedNode<SimpleImmutableEntry<K, V>>
        implements ImmutableMap<K, V>, Serializable {
    private static final @NonNull ChampMap<?, ?> EMPTY = new ChampMap<>(BitmapIndexedNode.emptyNode(), 0);
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * We do not guarantee an iteration order. Make sure that nobody accidentally relies on it.
     */
    static final int SALT = new java.util.Random().nextInt();
    final int size;

    ChampMap(@NonNull BitmapIndexedNode<SimpleImmutableEntry<K, V>> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        this.size = size;
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param c   a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampMap<K, V> copyOf(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return ChampMap.<K, V>of().putAll(c);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    public static <K, V> @NonNull ChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ChampMap.<K, V>of().putAll(map);
    }

    static <V, K> boolean entryKeyEquals(SimpleImmutableEntry<K, V> a, SimpleImmutableEntry<K, V> b) {
        return Objects.equals(a.getKey(), b.getKey());
    }

    static <V, K> int keyHash(Object e) {
        return SALT ^ Objects.hashCode(e);
    }

    static <V, K> int entryKeyHash(SimpleImmutableEntry<K, V> e) {
        return SALT ^ Objects.hashCode(e.getKey());
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
     * {@inheritDoc}
     */
    @Override
    public @NonNull ChampMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return find(new SimpleImmutableEntry<>(key, null), keyHash(key), 0,
                ChampMap::entryKeyEquals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ChampMap) {
            ChampMap<?, ?> that = (ChampMap<?, ?>) other;
            return size == that.size && equivalent(that);
        }
        return ReadOnlyMap.mapEquals(this, other);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        Object result = find(
                new SimpleImmutableEntry<>((K) o, null), keyHash(o), 0, ChampMap::entryKeyEquals);
        return result == Node.NO_DATA || result == null ? null : ((SimpleImmutableEntry<K, V>) result).getValue();
    }

    @Nullable
    static <K, V> SimpleImmutableEntry<K, V> updateEntry(@Nullable SimpleImmutableEntry<K, V> oldv, @Nullable SimpleImmutableEntry<K, V> newv) {
        return Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iteratorToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return new ChampIterator<>(this, null);
    }

    @Override
    public @NonNull ChampMap<K, V> put(@NonNull K key, @Nullable V value) {
        var details = new ChangeEvent<SimpleImmutableEntry<K, V>>();
        var newRootNode = put(null, new SimpleImmutableEntry<>(key, value),
                keyHash(key), 0, details,
                ChampMap::updateEntry, ChampMap::entryKeyEquals, ChampMap::entryKeyHash);
        if (details.isModified()) {
            return new ChampMap<>(newRootNode, details.isReplaced() ? size : size + 1);
        }
        return this;
    }

    @Override
    public @NonNull ChampMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (ChampMap<K, V>) ImmutableMap.super.putAll(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        var m = toMutable();
        m.putAll(c);
        return m.toImmutable();
    }

    @Override
    public @NonNull ChampMap<K, V> remove(@NonNull K key) {
        int keyHash = keyHash(key);
        var details = new ChangeEvent<SimpleImmutableEntry<K, V>>();
        var newRootNode =
                remove(null, new SimpleImmutableEntry<>(key, null), keyHash, 0, details,
                        ChampMap::entryKeyEquals);
        if (details.isModified()) {
            return size == 1 ? ChampMap.of() : new ChampMap<>(newRootNode, size - 1);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        var m = toMutable();
        m.removeAll(c);
        return m.toImmutable();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampMap<K, V> retainAll(@NonNull Iterable<? extends K> c) {
        var m = toMutable();
        m.retainAll(c);
        return m.toImmutable();
    }

    @Override
    public @NonNull ReadOnlySet<K> readOnlyKeySet() {
        return new ReadOnlySetFacade<>(
                () -> new ChampIterator<>(this, Map.Entry::getKey),
                this::size,
                this::containsKey
        );
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull Spliterator<Map.Entry<K, V>> spliterator() {
        return new ChampSpliterator<>(this, null, size(), Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
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
    public @NonNull MutableChampMap<K, V> asMap() {
        return new MutableChampMap<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    @Serial
    private @NonNull Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        @Serial
        private static final long serialVersionUID = 0L;

        SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return ChampMap.of().putAll(deserialized);
        }
    }
}