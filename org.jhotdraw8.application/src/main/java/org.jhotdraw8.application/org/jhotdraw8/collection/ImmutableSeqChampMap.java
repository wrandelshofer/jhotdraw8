/*
 * @(#)ImmutableSeqTrieMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.ChampTrie;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.Node;
import org.jhotdraw8.collection.champ.SequencedTrieIterator;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Implements an immutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP), with predictable iteration order.
 * <p>
 * Features:
 * <ul>
 *     <li>allows null keys and null values</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order, in which keys were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>copyPut: O(1) amortized</li>
 *     <li>copyRemove: O(1)</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + O(log n) distributed across subsequent updates</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(log n)</li>
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
 * Insertion Order:
 * <p>
 * This map uses a counter to keep track of the insertion order.
 * It stores the current value of the counter in the sequence number
 * field of each data entry. If the counter wraps around, it must renumber all
 * sequence numbers.
 * <p>
 * The renumbering is why the {@code copyPut} is O(1) only in an amortized sense.
 * <p>
 * The iterator of the map is a priority queue, that orders the entries by
 * their stored insertion counter value. This is why {@code iterator.next()}
 * is O(log n).
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
public class ImmutableSeqChampMap<K, V> extends BitmapIndexedNode<K, V> implements ImmutableMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 3;
    private static final ImmutableSeqChampMap<?, ?> EMPTY = new ImmutableSeqChampMap<>(BitmapIndexedNode.emptyNode(), 0, 0);
    final transient int size;
    /**
     * Counter for the sequence number of the last entry.
     * The counter is incremented when a new entry is added to the end of the
     * sequence.
     * <p>
     * The counter is in the range from {@code 0} to
     * {@link Integer#MAX_VALUE} - 1.
     * When the counter reaches {@link Integer#MAX_VALUE}, all
     * sequence numbers are renumbered, and the counter is reset to
     * {@code size}.
     */
    private transient final int lastSequenceNumber;
    private transient final ToIntFunction<K> hashFunction = Objects::hashCode;

    ImmutableSeqChampMap(@NonNull BitmapIndexedNode<K, V> root, int size,
                         int lastSequenceNumber) {
        super(root.nodeMap(), root.dataMap(), root.mixed, ENTRY_LENGTH);
        this.size = size;
        this.lastSequenceNumber = lastSequenceNumber;
    }

    /**
     * Returns an immutable map that contains the provided entries.
     *
     * @param k1  the key of the first entry
     * @param v1  the value of the first entry
     * @param kv  additional entries k2,v2, k3,v3, k4,v4, ... .
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable map of the provided entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableSeqChampMap<K, V> of(K k1, V v1, Object... kv) {
        return (ImmutableSeqChampMap<K, V>) ((ImmutableSeqChampMap<K, V>) ImmutableSeqChampMap.EMPTY)
                .copyPut(k1, v1).copyPutKeyValues(kv);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableSeqChampMap<K, V> of() {
        return (ImmutableSeqChampMap<K, V>) ImmutableSeqChampMap.EMPTY;
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
    public static <K, V> ImmutableSeqChampMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ImmutableSeqChampMap<K, V>) ((ImmutableSeqChampMap<K, V>) ImmutableSeqChampMap.EMPTY).copyPutAll(map);
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
    public static <K, V> ImmutableSeqChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return (ImmutableSeqChampMap<K, V>) ((ImmutableSeqChampMap<K, V>) ImmutableSeqChampMap.EMPTY).copyPutAll(map);
    }

    /**
     * Returns an immutable map that contains the provided entries.
     *
     * @param entries map entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return an immutable map of the provided entries
     */
    @SafeVarargs
    @SuppressWarnings({"unchecked", "varargs"})
    public static <K, V> @NonNull ImmutableSeqChampMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V>... entries) {
        return (ImmutableSeqChampMap<K, V>) ((ImmutableSeqChampMap<K, V>) ImmutableSeqChampMap.EMPTY).copyPutAll(Arrays.asList(entries));
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(key, hashFunction.applyAsInt(key), 0, ENTRY_LENGTH, ENTRY_LENGTH - 1) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableSeqChampMap<K, V> copyClear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableSeqChampMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        BitmapIndexedNode<K, V> newRootNode = update(null, key, value,
                keyHash, 0, details, ENTRY_LENGTH, lastSequenceNumber + 1, ENTRY_LENGTH - 1);
        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                return new ImmutableSeqChampMap<>(newRootNode, size, lastSequenceNumber + 1);
            }
            if (lastSequenceNumber + 1 == Node.NO_SEQUENCE_NUMBER) {
                return renumber(newRootNode);
            } else {
                return new ImmutableSeqChampMap<>(newRootNode, size + 1, lastSequenceNumber + 1);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof SeqChampMap)) {
            return ((SeqChampMap<K, V>) m).toImmutable();
        }
        return copyPutAll(m.entrySet().iterator());
    }

    @Override
    public @NonNull ImmutableSeqChampMap<K, V> copyPutAll(@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> entries) {
        final SeqChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        while (entries.hasNext()) {
            Map.Entry<? extends K, ? extends V> entry = entries.next();
            ChangeEvent<V> details = t.putAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    public @NonNull ImmutableSeqChampMap<K, V> copyRemove(@NonNull K key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                remove(null, key, keyHash, 0, details, ENTRY_LENGTH, ENTRY_LENGTH - 1);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            return new ImmutableSeqChampMap<>(newRootNode, size - 1, lastSequenceNumber + 1);
        }
        return this;
    }

    @Override
    public @NonNull ImmutableSeqChampMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }
        final SeqChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<V> details = t.removeAndGiveDetails(key);
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableSeqChampMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        final SeqChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : this.readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    /**
     * Dumps the internal structure of this map in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public String dump() {
        return new ChampTrieGraphviz<K, V>().dumpTrie(this, ENTRY_LENGTH, true, true);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> entries() {
        return new EntryIterator<>(size, this, ENTRY_LENGTH);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof ImmutableSeqChampMap) {
            ImmutableSeqChampMap<?, ?> that = (ImmutableSeqChampMap<?, ?>) other;
            if (this.size != that.size) {
                return false;
            }
            return this.equivalent(that, ENTRY_LENGTH, ENTRY_LENGTH - 1);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final @NonNull Object o) {
        K key = (K) o;
        Object result = findByKey(key, hashFunction.applyAsInt(key), 0, ENTRY_LENGTH, ENTRY_LENGTH - 1);
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
        return new KeyIterator<>(size, this, ENTRY_LENGTH);
    }

    @NonNull
    private ImmutableSeqChampMap<K, V> renumber(BitmapIndexedNode<K, V> newRootNode) {
        newRootNode = ChampTrie.renumber(size, newRootNode, new UniqueId(), ENTRY_LENGTH);
        return new ImmutableSeqChampMap<>(newRootNode, size + 1, size);
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
    public SeqChampMap<K, V> toMutable() {
        return new SeqChampMap<>(this);
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

        @Override
        protected Object readResolve() {
            return ImmutableSeqChampMap.of().copyPutAll(deserialized.iterator());
        }
    }

    static class EntryIterator<K, V> extends SequencedTrieIterator<K, V>
            implements Iterator<Map.Entry<K, V>> {

        public EntryIterator(int size, Node<K, V> rootNode, int entryLength) {
            super(size, rootNode, entryLength);
        }

        @Override
        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    static class KeyIterator<K, V> extends SequencedTrieIterator<K, V>
            implements Iterator<K> {

        public KeyIterator(int size, Node<K, V> rootNode, int entryLength) {
            super(size, rootNode, entryLength);
        }

        @Override
        public K next() {
            return nextEntry().getKey();
        }
    }
}
