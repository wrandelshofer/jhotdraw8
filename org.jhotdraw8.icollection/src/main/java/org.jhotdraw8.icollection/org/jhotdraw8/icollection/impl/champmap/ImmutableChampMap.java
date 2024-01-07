/*
 * @(#)ImmutableChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.champmap;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
import org.jhotdraw8.icollection.serialization.MapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Implements an immutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null keys and null values</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyPut: O(1)</li>
 *     <li>copyRemove: O(1)</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + a cost distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other maps.
 * <p>
 * If a write operation is performed on a node, then this map creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP tree has a fixed maximal height, the cost is O(1).
 * <p>
 * This map can create a mutable copy of itself in O(1) time and O(0) space
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
public class ImmutableChampMap<K, V> extends BitmapIndexedNode<K, V>
        implements ImmutableMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 2;

    private static final ImmutableChampMap<?, ?> EMPTY = new ImmutableChampMap<>(BitmapIndexedNode.emptyNode(), 0);

    final transient int size;
    private final transient ToIntFunction<K> hashFunction = Objects::hashCode;

    ImmutableChampMap(@NonNull BitmapIndexedNode<K, V> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        this.size = size;
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
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
    public static <K, V> @NonNull ImmutableChampMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ImmutableChampMap<K, V>) ((ImmutableChampMap<K, V>) ImmutableChampMap.EMPTY).putAll(map);
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
    public static <K, V> @NonNull ImmutableChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ((ImmutableChampMap<K, V>) ImmutableChampMap.EMPTY).putAll(map);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableChampMap<K, V> of() {
        return (ImmutableChampMap<K, V>) ImmutableChampMap.EMPTY;
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
    public static <K, V> @NonNull ImmutableChampMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return (ImmutableChampMap<K, V>) of().putAll(entries);
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(key, hashFunction.applyAsInt(key), 0) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableChampMap<K, V> put(@NonNull K key, @Nullable V value) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();

        final BitmapIndexedNode<K, V> newRootNode = update(null, key, value,
                keyHash, 0, details);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                return new ImmutableChampMap<>(newRootNode,
                        size);
            }

            return new ImmutableChampMap<>(newRootNode, size + 1);
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ImmutableChampMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof ChampMap)) {
            return ((ChampMap<K, V>) m).toImmutable();
        }
        return putAll(m.entrySet().iterator());
    }

    //@Override
    @SuppressWarnings("unchecked")
    public @NonNull ImmutableChampMap<K, V> putAll(@NonNull ImmutableMap<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof ImmutableChampMap)) {
            return (ImmutableChampMap<K, V>) m;
        }
        return putAll(m.readOnlyEntrySet().iterator());
    }


    //@Override
    public @NonNull ImmutableChampMap<K, V> putAll(@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> entries) {
        final ChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        while (entries.hasNext()) {
            Map.Entry<? extends K, ? extends V> entry = entries.next();
            ChangeEvent<V> details = t.putAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableChampMap<K, V> remove(@NonNull K key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                remove(null, key, keyHash, 0, details);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            return new ImmutableChampMap<>(newRootNode, size - 1);
        }
        return this;
    }

    @Override
    public @NonNull ImmutableChampMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }

        final ChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<V> details = t.removeAndGiveDetails(key);
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    //@Override
    public @NonNull ImmutableChampMap<K, V> retainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        final ChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    /**
     * Dumps the internal structure of this set in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public @NonNull String dump() {
        return new ChampTrieGraphviz<K, V>().dumpTrie(this, ENTRY_LENGTH, true, false);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return new EntryIterator<>(this, null, null);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof ImmutableChampMap) {
            ImmutableChampMap<?, ?> that = (ImmutableChampMap<?, ?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final @NonNull Object o) {
        K key = (K) o;
        Object result = findByKey(key, hashFunction.applyAsInt(key), 0);
        return result == Node.NO_VALUE ? null : (V) result;
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
    public int size() {
        return size;
    }

    @Override
    public @NonNull ChampMap<K, V> toMutable() {
        return new ChampMap<>(this);
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
            return ImmutableChampMap.of().putAll(deserializedEntries.iterator());
        }
    }
}
