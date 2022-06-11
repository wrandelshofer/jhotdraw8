/*
 * @(#)ChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.EntryIterator;
import org.jhotdraw8.collection.champ.Node;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implements a mutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP).
 * <p>
 * Features:
 * <ul>
 *     <li>allows null keys and null values</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>does not guarantee a specific iteration order</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put: O(1)</li>
 *     <li>remove: O(1)</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toImmutable: O(1) + a cost distributed across subsequent updates</li>
 *     <li>clone: O(1) + a cost distributed across subsequent updates</li>
 *     <li>iterator.next(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP tree contains nodes that may be shared with other maps, and nodes
 * that are exclusively owned by this map.
 * <p>
 * If a write operation is performed on an exclusively owned node, then this
 * map is allowed to mutate the node (mutate-on-write).
 * If a write operation is performed on a potentially shared node, then this
 * map is forced to create an exclusive copy of the node and of all not (yet)
 * exclusively owned parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP tree has a fixed maximal height, the cost is O(1) in either
 * case.
 * <p>
 * This map can create an immutable copy of itself in O(1) time and O(0) space
 * using method {@link #toImmutable()}. This map loses exclusive ownership of
 * all its tree nodes.
 * Thus, creating an immutable copy increases the constant cost of
 * subsequent writes, until all shared nodes have been gradually replaced by
 * exclusively owned nodes again.
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
public class ChampMap<K, V> extends AbstractMap<K, V> implements Serializable, Cloneable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 2;
    private transient @Nullable UniqueId mutator;
    private transient @NonNull BitmapIndexedNode<K, V> root;
    private transient int size;
    private transient int modCount;

    public ChampMap() {
        this.root = BitmapIndexedNode.emptyNode();
    }

    public ChampMap(@NonNull Map<? extends K, ? extends V> m) {
        if (m instanceof ChampMap) {
            @SuppressWarnings("unchecked")
            ChampMap<K, V> that = (ChampMap<K, V>) m;
            this.mutator = null;
            that.mutator = null;
            this.root = that.root;
            this.size = that.size;
            this.modCount = 0;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            this.putAll(m);
        }
    }

    public ChampMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        this.root = BitmapIndexedNode.emptyNode();
        for (Entry<? extends K, ? extends V> e : m) {
            this.put(e.getKey(), e.getValue());
        }

    }

    public ChampMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        if (m instanceof ImmutableChampMap) {
            @SuppressWarnings("unchecked")
            ImmutableChampMap<K, V> that = (ImmutableChampMap<K, V>) m;
            this.root = that;
            this.size = that.size;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            this.putAll(m.asMap());
        }
    }

    @Override
    public void clear() {
        root = BitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
    }

    @Override
    public @NonNull ChampMap<K, V> clone() {
        try {
            @SuppressWarnings("unchecked") final ChampMap<K, V> that = (ChampMap<K, V>) super.clone();
            that.mutator = null;
            this.mutator = null;
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    boolean containsEntry(final @Nullable Object o) {
        if (o instanceof Entry) {
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            return containsKey(key)
                    && Objects.equals(entry.getValue(), get(key));
        }
        return false;
    }

    @Override
    public boolean containsKey(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.findByKey(key, Objects.hashCode(key), 0, ENTRY_LENGTH, ENTRY_LENGTH) != Node.NO_VALUE;
    }

    /**
     * Dumps the internal structure of this map in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public @NonNull String dump() {
        return new ChampTrieGraphviz<K, V>().dumpTrie(root, ENTRY_LENGTH, true, false);
    }

    @Override
    public @NonNull Set<Entry<K, V>> entrySet() {
        return new WrappedSet<>(
                () -> new FailFastIterator<>(new EntryIterator<K, V>(root, ENTRY_LENGTH, ENTRY_LENGTH,
                        this::persistentRemove, this::persistentPutIfPresent),
                        () -> this.modCount),
                ChampMap.this::size,
                ChampMap.this::containsEntry,
                ChampMap.this::clear,
                ChampMap.this::removeEntry
        );
    }

    private void persistentRemove(K key) {
        mutator = null;
        remove(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(final @NonNull Object o) {
        final K key = (K) o;
        Object result = root.findByKey(key, Objects.hashCode(key), 0, ENTRY_LENGTH, ENTRY_LENGTH);
        return result == Node.NO_VALUE ? null : (V) result;
    }

    private @NonNull UniqueId getOrCreateMutator() {
        if (mutator == null) {
            mutator = new UniqueId();
        }
        return mutator;
    }

    @Override
    public V put(K key, V value) {
        return putAndGiveDetails(key, value).getOldValue();
    }

    @NonNull ChangeEvent<V> putAndGiveDetails(final K key, final V val) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<V> details = new ChangeEvent<>();

        final BitmapIndexedNode<K, V> newRootNode = root
                .update(getOrCreateMutator(), key, val, keyHash, 0, details, ENTRY_LENGTH, Node.NO_SEQUENCE_NUMBER, ENTRY_LENGTH);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                root = newRootNode;
            } else {
                root = newRootNode;
                size += 1;
                modCount++;
            }
        }

        return details;
    }

    private void persistentPutIfPresent(@NonNull K k, V v) {
        if (containsKey(k)) {
            mutator = null;
            put(k, v);
        }
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return removeAndGiveDetails(key).getOldValue();
    }

    @NonNull ChangeEvent<V> removeAndGiveDetails(final K key) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                root.remove(getOrCreateMutator(), key, keyHash, 0, details, ENTRY_LENGTH, ENTRY_LENGTH);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            root = newRootNode;
            size = size - 1;
            modCount++;
        }
        return details;
    }

    boolean removeEntry(final @Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            remove(entry.getKey());
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Returns an immutable copy of this map.
     *
     * @return an immutable copy
     */
    public @NonNull ImmutableChampMap<K, V> toImmutable() {
        if (size == 0) {
            return ImmutableChampMap.of();
        }
        mutator = null;
        return new ImmutableChampMap<>(root, size);
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return new ChampMap<>(deserialized);
        }
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<K, V>(this);
    }
}