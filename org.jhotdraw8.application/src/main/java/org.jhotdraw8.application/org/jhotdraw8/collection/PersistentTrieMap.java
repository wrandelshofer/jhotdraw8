/*
 * @(#)PersistentTrieMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BaseTrieIterator;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.KeyIterator;
import org.jhotdraw8.collection.champ.Node;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
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
public class PersistentTrieMap<K, V> extends BitmapIndexedNode<K, V>
        implements PersistentMap<K, V>, ImmutableMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 2;

    private static final PersistentTrieMap<?, ?> EMPTY = new PersistentTrieMap<>(BitmapIndexedNode.emptyNode(), 0);

    final transient int size;
    private final transient ToIntFunction<K> hashFunction = Objects::hashCode;

    PersistentTrieMap(@NonNull BitmapIndexedNode<K, V> root, int size) {
        super(root.nodeMap(), root.dataMap(), root.mixed, ENTRY_LENGTH);
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTrieMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (PersistentTrieMap<K, V>) ((PersistentTrieMap<K, V>) PersistentTrieMap.EMPTY).copyPutAll(map);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> PersistentTrieMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return (PersistentTrieMap<K, V>) ((PersistentTrieMap<K, V>) PersistentTrieMap.EMPTY).copyPutAll(map);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull PersistentTrieMap<K, V> of(K k, V v, Object... kv) {
        return (PersistentTrieMap<K, V>) ((PersistentTrieMap<K, V>) PersistentTrieMap.EMPTY).copyPut(k, v).copyPutKeyValues(kv);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull PersistentTrieMap<K, V> of() {
        return (PersistentTrieMap<K, V>) ((PersistentTrieMap<K, V>) PersistentTrieMap.EMPTY);
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(key, hashFunction.applyAsInt(key), 0, ENTRY_LENGTH, ENTRY_LENGTH) != Node.NO_VALUE;
    }

    @Override
    public @NonNull PersistentMap<K, V> copyClear() {
        return isEmpty() ? this : of();
    }

    public @NonNull PersistentTrieMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();

        final BitmapIndexedNode<K, V> newRootNode = update(null, key, value,
                keyHash, 0, details, ENTRY_LENGTH, Node.NO_SEQUENCE_NUMBER, ENTRY_LENGTH);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                return new PersistentTrieMap<>(newRootNode,
                        size);
            }

            return new PersistentTrieMap<>(newRootNode, size + 1);
        }

        return this;
    }

    public @NonNull PersistentTrieMap<K, V> copyPutAll(@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> entries) {
        final TrieMap<K, V> t = this.toMutable();
        boolean modified = false;
        while (entries.hasNext()) {
            Map.Entry<? extends K, ? extends V> entry = entries.next();
            ChangeEvent<V> details = t.putAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified;
        }
        return modified ? t.toPersistent() : this;
    }

    public @NonNull PersistentTrieMap<K, V> copyRemove(@NonNull K key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                remove(null, key, keyHash, 0, details, ENTRY_LENGTH, ENTRY_LENGTH);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            return new PersistentTrieMap<>(newRootNode, size - 1);
        }
        return this;
    }

    @Override
    public @NonNull PersistentTrieMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }

        final TrieMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<V> details = t.removeAndGiveDetails(key);
            modified |= details.isModified;
        }
        return modified ? t.toPersistent() : this;
    }

    @Override
    public @NonNull PersistentTrieMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
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

    /**
     * Dumps the internal structure of this set in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public String dump() {
        return new ChampTrieGraphviz<K, V>().dumpTrie(this, ENTRY_LENGTH, true, false);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> entries() {
        return new EntryIterator<>(this, ENTRY_LENGTH);
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
            return this.equivalent(that, ENTRY_LENGTH, ENTRY_LENGTH);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final @NonNull Object o) {
        K key = (K) o;
        Object result = findByKey(key, hashFunction.applyAsInt(key), 0, ENTRY_LENGTH, ENTRY_LENGTH);
        return result == Node.NO_VALUE ? null : (V) result;
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
        return new KeyIterator<>(this, ENTRY_LENGTH);
    }

    @Override
    public int size() {
        return size;
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
    public TrieMap<K, V> toMutable() {
        return new TrieMap<>(this);
    }

    @Override
    public String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        protected Object readResolve() {
            return PersistentTrieMap.of().copyPutAll(deserialized.iterator());
        }
    }

    static class EntryIterator<K, V> extends BaseTrieIterator<K, V>
            implements Iterator<Map.Entry<K, V>> {

        public EntryIterator(Node<K, V> rootNode, int entryLength) {
            super(rootNode, entryLength);
        }

        @Override
        public Map.Entry<K, V> next() {
            return nextEntry(AbstractMap.SimpleImmutableEntry::new);
        }
    }
}
