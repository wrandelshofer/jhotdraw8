/*
 * @(#)PersistentTrieMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Implements a persistent map using a Compressed Hash-Array Mapped Prefix-tree
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
 *     <li>toMutable: O(log n) distributed across subsequent updates</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other map.
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
public class PersistentTrieMap<K, V> extends ChampTrieHelper.BitmapIndexedNode<K, V> implements PersistentMap<K, V>, ImmutableMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int TUPLE_LENGTH = 2;

    private static final PersistentTrieMap<?, ?> EMPTY_MAP = new PersistentTrieMap<>(ChampTrieHelper.EMPTY_NODE, 0);

    final int size;
    private final ToIntFunction<K> hashFunction = Objects::hashCode;

    PersistentTrieMap(@NonNull ChampTrieHelper.BitmapIndexedNode<K, V> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.nodes, TUPLE_LENGTH);
        this.size = size;
    }

    public static <K, V> PersistentTrieMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        if (map instanceof PersistentTrieMap) {
            @SuppressWarnings("unchecked")
            PersistentTrieMap<K, V> unchecked = (PersistentTrieMap<K, V>) map;
            return unchecked;
        }
        TrieMap<K, V> tr = new TrieMap<>(of());
        for (final Map.Entry<? extends K, ? extends V> entry : map.readOnlyEntrySet()) {
            tr.putAndGiveDetails(entry.getKey(), entry.getValue());
        }
        return tr.toPersistent();
    }

    public static <K, V> PersistentTrieMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ofEntries(map.entrySet());
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull PersistentTrieMap<K, V> of() {
        return (PersistentTrieMap<K, V>) PersistentTrieMap.EMPTY_MAP;
    }

    @SafeVarargs
    public static <K, V> @NonNull PersistentTrieMap<K, V> ofEntries(@NonNull Map.Entry<K, V>... entries) {
        TrieMap<K, V> result = PersistentTrieMap.<K, V>of().toMutable();
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            result.putAndGiveDetails(entry.getKey(), entry.getValue());
        }
        return result.toPersistent();
    }

    public static <K, V> @NonNull PersistentTrieMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        TrieMap<K, V> result = PersistentTrieMap.<K, V>of().toMutable();
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            result.putAndGiveDetails(entry.getKey(), entry.getValue());
        }
        return result.toPersistent();
    }

    /**
     * Returns a copy of this set that is mutable.
     * <p>
     * This operation is performed in O(1) because the mutable map shares
     * the underlying trie nodes with this set.
     * <p>
     * Initially, the returned mutable map hasn't exclusive ownership of any
     * trie node. Therefore, the first few updates that it performs, are
     * copy-on-write operations, until it exclusively owns some trie nodes that
     * it can update.
     *
     * @return a mutable trie set
     */
    private TrieMap<K, V> toMutable() {
        return new TrieMap<>(this);
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH).keyExists();
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> entries() {
        return entryIterator();
    }

    public Iterator<Map.Entry<K, V>> entryIterator() {
        return new ChampTrieHelper.MapEntryIterator<>(this, TUPLE_LENGTH, hashFunction);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }

        if (other instanceof PersistentTrieMap) {
            PersistentTrieMap<?, ?> that = (PersistentTrieMap<?, ?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that, TUPLE_LENGTH);
        } else if (other instanceof Map) {
            Map<?, ?> that = (Map<?, ?>) other;
            if (this.size() != that.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : that.entrySet()) {
                @SuppressWarnings("unchecked") final K key = (K) entry.getKey();
                final ChampTrieHelper.SearchResult<V> result = findByKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH);

                if (!result.keyExists()) {
                    return false;
                } else {
                    @SuppressWarnings("unchecked") final V val = (V) entry.getValue();
                    if (!Objects.equals(result.get(), val)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public V get(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        final ChampTrieHelper.SearchResult<V> result = findByKey(key, hashFunction.applyAsInt(key), 0, TUPLE_LENGTH);
        return result.orElse(null);
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iterableToHashCode(entries());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public @NonNull Iterator<K> keys() {
        return new ChampTrieHelper.KeyIterator<>(this, TUPLE_LENGTH, hashFunction);
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull PersistentTrieMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChampTrieHelper.ChangeEvent<V> details = new ChampTrieHelper.ChangeEvent<>();

        final ChampTrieHelper.BitmapIndexedNode<K, V> newRootNode = updated(null, key, value,
                keyHash, 0, details, TUPLE_LENGTH, hashFunction, ChampTrieHelper.TUPLE_VALUE);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                return new PersistentTrieMap<>(newRootNode,
                        size);
            }

            return new PersistentTrieMap<>(newRootNode, size + 1);
        }

        return this;
    }

    public @NonNull PersistentTrieMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> map) {
        final TrieMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            ChampTrieHelper.ChangeEvent<V> details = t.putAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified;
        }
        return modified ? t.toPersistent() : this;
    }

    public @NonNull PersistentTrieMap<K, V> copyRemove(@NonNull K key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChampTrieHelper.ChangeEvent<V> details = new ChampTrieHelper.ChangeEvent<>();
        final ChampTrieHelper.BitmapIndexedNode<K, V> newRootNode =
                removed(null, key, keyHash, 0, details, TUPLE_LENGTH, hashFunction);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            return new PersistentTrieMap<>(newRootNode, size - 1);
        }
        return this;
    }

    @Override
    public @NonNull PersistentTrieMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        final TrieMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChampTrieHelper.ChangeEvent<V> details = t.removeAndGiveDetails(key);
            modified |= details.isModified;
        }
        return modified ? t.toPersistent() : this;
    }

    @Override
    public @NonNull PersistentTrieMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c) {
        final TrieMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : this.readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toPersistent() : this;
    }

    @Override
    public String toString() {
        return ReadOnlyMap.mapToString(this);
    }
}
