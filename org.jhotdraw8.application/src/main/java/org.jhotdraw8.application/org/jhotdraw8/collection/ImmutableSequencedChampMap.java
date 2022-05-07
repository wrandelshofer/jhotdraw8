/*
 * @(#)ImmutableSeqChampMap.java
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
import org.jhotdraw8.collection.champ.SequencedEntryIterator;

import java.io.ObjectStreamException;
import java.io.Serializable;
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
 *     <li>toMutable: O(1) + a cost distributed across subsequent updates in the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(log n)</li>
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
public class ImmutableSequencedChampMap<K, V> extends BitmapIndexedNode<K, V> implements ImmutableMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 3;
    private static final ImmutableSequencedChampMap<?, ?> EMPTY = new ImmutableSequencedChampMap<>(BitmapIndexedNode.emptyNode(), 0, 0);
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

    ImmutableSequencedChampMap(@NonNull BitmapIndexedNode<K, V> root, int size,
                               int lastSequenceNumber) {
        super(root.nodeMap(), root.dataMap(), root.mixed, ENTRY_LENGTH);
        this.size = size;
        this.lastSequenceNumber = lastSequenceNumber;
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ImmutableSequencedChampMap<K, V> of() {
        return (ImmutableSequencedChampMap<K, V>) ImmutableSequencedChampMap.EMPTY;
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
    public static <K, V> ImmutableSequencedChampMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ImmutableSequencedChampMap<K, V>) ((ImmutableSequencedChampMap<K, V>) ImmutableSequencedChampMap.EMPTY).copyPutAll(map);
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
    public static <K, V> ImmutableSequencedChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return (ImmutableSequencedChampMap<K, V>) ((ImmutableSequencedChampMap<K, V>) ImmutableSequencedChampMap.EMPTY).copyPutAll(map);
    }

    /**
     * Returns an immutable map that contains the provided entries.
     *
     * @param entries map entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return an immutable map of the provided entries
     */
    public static <K, V> @NonNull ImmutableChampMap<K, V> ofEntries(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return (ImmutableChampMap<K, V>) of().copyPutAll(entries);
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(key, hashFunction.applyAsInt(key), 0, ENTRY_LENGTH, ENTRY_LENGTH - 1) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyClear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        BitmapIndexedNode<K, V> newRootNode = update(null, key, value,
                keyHash, 0, details, ENTRY_LENGTH, lastSequenceNumber + 1, ENTRY_LENGTH - 1);
        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                return new ImmutableSequencedChampMap<>(newRootNode, size, lastSequenceNumber + 1);
            }
            if (lastSequenceNumber + 1 == Node.NO_SEQUENCE_NUMBER) {
                return renumber(newRootNode);
            } else {
                return new ImmutableSequencedChampMap<>(newRootNode, size + 1, lastSequenceNumber + 1);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof SequencedChampMap)) {
            return ((SequencedChampMap<K, V>) m).toImmutable();
        }
        return copyPutAll(m.entrySet().iterator());
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyPutAll(@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> entries) {
        final SequencedChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        while (entries.hasNext()) {
            Map.Entry<? extends K, ? extends V> entry = entries.next();
            ChangeEvent<V> details = t.putLastAndGiveDetails(entry.getKey(), entry.getValue());
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyRemove(@NonNull K key) {
        final int keyHash = hashFunction.applyAsInt(key);
        final ChangeEvent<V> details = new ChangeEvent<>();
        final BitmapIndexedNode<K, V> newRootNode =
                remove(null, key, keyHash, 0, details, ENTRY_LENGTH, ENTRY_LENGTH - 1);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            return new ImmutableSequencedChampMap<>(newRootNode, size - 1, lastSequenceNumber + 1);
        }
        return this;
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }
        final SequencedChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<V> details = t.removeAndGiveDetails(key);
            modified |= details.isModified;
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyRetainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        final SequencedChampMap<K, V> t = this.toMutable();
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
     * Dumps the internal structure of this map in the Graphviz DOT Language.
     *
     * @return a dump of the internal structure
     */
    public String dump() {
        return new ChampTrieGraphviz<K, V>().dumpTrie(this, ENTRY_LENGTH, true, true);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return entries(false);
    }

    public @NonNull Iterator<Map.Entry<K, V>> entries(boolean reversed) {
        return new SequencedEntryIterator<K, V>(size, this, ENTRY_LENGTH, ENTRY_LENGTH - 1, reversed, null, null);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof ImmutableSequencedChampMap) {
            ImmutableSequencedChampMap<?, ?> that = (ImmutableSequencedChampMap<?, ?>) other;
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
        return ReadOnlyMap.iterableToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @NonNull
    private ImmutableSequencedChampMap<K, V> renumber(BitmapIndexedNode<K, V> newRootNode) {
        newRootNode = ChampTrie.renumber(size, newRootNode, new UniqueId(), ENTRY_LENGTH);
        return new ImmutableSequencedChampMap<>(newRootNode, size + 1, size);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull SequencedChampMap<K, V> toMutable() {
        return new SequencedChampMap<>(this);
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
            return ImmutableSequencedChampMap.of().copyPutAll(deserialized.iterator());
        }
    }
}
