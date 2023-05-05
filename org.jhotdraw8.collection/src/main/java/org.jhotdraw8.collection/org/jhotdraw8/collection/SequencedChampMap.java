/*
 * @(#)SequencedChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.collection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.collection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ.ChampSequencedData;
import org.jhotdraw8.collection.impl.champ.ChampSpliterator;
import org.jhotdraw8.collection.impl.champ.ChangeEvent;
import org.jhotdraw8.collection.impl.champ.Node;
import org.jhotdraw8.collection.impl.champ.ReverseChampSpliterator;
import org.jhotdraw8.collection.impl.champ.SequencedEntry;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;

import static org.jhotdraw8.collection.impl.champ.ChampSequencedData.mustRenumber;
import static org.jhotdraw8.collection.impl.champ.ChampSequencedData.seqHash;

/**
 * Implements an immutable map using two Compressed Hash-Array Mapped Prefix-trees
 * (CHAMP), with predictable iteration order.
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> entries</li>
 *     <li>allows null keys and null values</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order, in which keys were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put, putFirst, putLast: O(1) in an amortized sense, because we sometimes have to
 *  *     renumber the elements.</li>
 *     <li>remove: O(1) in an amortized sense, because we sometimes have to renumber the elements.</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst, getLast: O(1)</li>
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
 * Insertion Order:
 * <p>
 * This map uses a counter to keep track of the insertion order.
 * It stores the current value of the counter in the sequence number
 * field of each data entry. If the counter wraps around, it must renumber all
 * sequence numbers.
 * <p>
 * The renumbering is why the {@code put} and {@code remove} methods are
 * O(1) only in an amortized sense.
 * <p>
 * <p>
 * To support iteration, a second CHAMP trie is maintained. The second CHAMP
 * trie has the same contents as the first. However, we use the sequence number
 * for computing the hash code of an element.
 * <p>
 * In this implementation, a hash code has a length of
 * 32 bits, and is split up in little-endian order into 7 parts of
 * 5 bits (the last part contains the remaining bits).
 * <p>
 * We convert the sequence number to unsigned 32 by adding Integer.MIN_VALUE
 * to it. And then we reorder its bits from
 * 66666555554444433333222221111100 to 00111112222233333444445555566666.
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
public class SequencedChampMap<K, V> extends BitmapIndexedNode<SequencedEntry<K, V>> implements ImmutableSequencedMap<K, V>, Serializable {
    private static final @NonNull SequencedChampMap<?, ?> EMPTY = new SequencedChampMap<>(BitmapIndexedNode.emptyNode(), BitmapIndexedNode.emptyNode(), 0, -1, 0);
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement after a new entry has been added to the start of the sequence.
     */
    final int first;
    /**
     * Counter for the sequence number of the last entry.
     * The counter is incremented after a new entry is added to the end of the
     * sequence.
     */
    final int last;
    /**
     * The root of the CHAMP trie for the sequence numbers.
     */
    final @NonNull BitmapIndexedNode<SequencedEntry<K, V>> sequenceRoot;
    /**
     * The size of the map.
     */
    final int size;

    SequencedChampMap(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root,
                      @NonNull BitmapIndexedNode<SequencedEntry<K, V>> sequenceRoot,
                      int size, int first, int last) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        assert (long) last - first >= size : "size=" + size + " first=" + first + " last=" + last;
        this.size = size;
        this.first = first;
        this.last = last;
        this.sequenceRoot = Objects.requireNonNull(sequenceRoot);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    public static <K, V> @NonNull SequencedChampMap<K, V> copyOf(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> map) {
        return SequencedChampMap.<K, V>of().putAll(map);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    public static <K, V> @NonNull SequencedChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return SequencedChampMap.<K, V>of().putAll(map);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull SequencedChampMap<K, V> of() {
        return (SequencedChampMap<K, V>) SequencedChampMap.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull SequencedChampMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return find(new SequencedEntry<>(key), SequencedEntry.keyHash(key), 0,
                SequencedEntry::keyEquals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof SequencedChampMap) {
            SequencedChampMap<?, ?> that = (SequencedChampMap<?, ?>) other;
            return size == that.size && equivalent(that);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        Object result = find(
                new SequencedEntry<>((K) o),
                SequencedEntry.keyHash(o), 0, SequencedEntry::keyEquals);
        return (V) ((result instanceof SequencedEntry<?, ?> entry) ? entry.getValue() : null);
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
        return new IteratorFacade<>(new ChampSpliterator<SequencedEntry<K, V>, Map.Entry<K, V>>(sequenceRoot, Map.Entry.class::cast, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size()), null);
    }

    @Override
    public @NonNull SequencedChampMap<K, V> put(@NonNull K key, @Nullable V value) {
        return putLast(key, value, false);
    }

    @Override
    public @NonNull SequencedChampMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (SequencedChampMap<K, V>) ImmutableSequencedMap.super.putAll(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull SequencedChampMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        if (isEmpty() && (entries instanceof SequencedChampMap<?, ?> that)) {
            return (SequencedChampMap<K, V>) that;
        }
        var t = this.toMutable();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            var details = t.putLast(entry.getKey(), entry.getValue(), false);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @NonNull
    private SequencedChampMap<K, V> putFirst(@NonNull K key, @Nullable V value, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, first);
        var newRoot = put(null,
                newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToFirst ? SequencedEntry::updateAndMoveToFirst : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            var newSeqRoot = ChampSequencedData.seqUpdate(sequenceRoot, null, details.getNewDataNonNull(), details,
                    SequencedEntry::update);
            return new SequencedChampMap<>(newRoot, newSeqRoot, size, first, last);
        }
        if (details.isModified()) {
            var newSeqRoot = sequenceRoot;
            int newSize = size;
            int newFirst = first;
            int newLast = last;
            var owner = new IdentityObject();
            if (details.isReplaced()) {
                if (moveToFirst) {
                    var oldEntry = details.getOldDataNonNull();
                    newSeqRoot = ChampSequencedData.seqRemove(newSeqRoot, owner, oldEntry, details);
                    newLast = oldEntry.getSequenceNumber() == newLast - 1 ? newLast - 1 : newLast;
                    newFirst--;
                }
            } else {
                newFirst--;
                newSize++;
            }
            newSeqRoot = ChampSequencedData.seqUpdate(newSeqRoot, owner, details.getNewDataNonNull(), details, SequencedEntry::update);
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    @Override
    public @NonNull SequencedChampMap<K, V> putFirst(@NonNull K key, @Nullable V value) {
        return putFirst(key, value, true);
    }

    @NonNull
    private SequencedChampMap<K, V> putLast(@NonNull K key, @Nullable V value, boolean moveToLast) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, last);
        var newRoot = put(null,
                newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToLast ? SequencedEntry::updateAndMoveToLast : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            var newSeqRoot = ChampSequencedData.seqUpdate(sequenceRoot, null, details.getNewDataNonNull(), details,
                    SequencedEntry::update);
            return new SequencedChampMap<>(newRoot, newSeqRoot, size, first, last);
        }
        if (details.isModified()) {
            var newSeqRoot = sequenceRoot;
            int newFirst = first;
            int newLast = last;
            int newSize = size;
            var owner = new IdentityObject();
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldEntry = details.getOldDataNonNull();
                    newSeqRoot = ChampSequencedData.seqRemove(newSeqRoot, owner, oldEntry, details);
                    newFirst = oldEntry.getSequenceNumber() == newFirst + 1 ? newFirst + 1 : newFirst;
                    newLast++;
                }
            } else {
                newLast++;
                newSize++;
            }
            newSeqRoot = ChampSequencedData.seqUpdate(newSeqRoot, owner, details.getNewDataNonNull(), details,
                    SequencedEntry::update);
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    @Override
    public @NonNull SequencedChampMap<K, V> putLast(@NonNull K key, @Nullable V value) {
        return putLast(key, value, true);
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return new ReadOnlySequencedMapFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::containsKey,
                this::get,
                this::lastEntry,
                this::firstEntry
        );
    }

    private @NonNull SequencedChampMap<K, V> remove(@NonNull K key, int newFirst, int newLast) {
        int keyHash = SequencedEntry.keyHash(key);
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRoot =
                remove(null, new SequencedEntry<>(key), keyHash, 0, details, SequencedEntry::keyEquals);
        BitmapIndexedNode<SequencedEntry<K, V>> newSeqRoot = sequenceRoot;
        if (details.isModified()) {
            var oldEntry = details.getOldData();
            int seq = oldEntry.getSequenceNumber();
            newSeqRoot = newSeqRoot.remove(null,
                    oldEntry,
                    seqHash(seq), 0, details, ChampSequencedData::seqEquals);
            if (seq == newFirst) {
                newFirst++;
            }
            if (seq == newLast - 1) {
                newLast--;
            }
            return size == 1 ? SequencedChampMap.of() : renumber(newRoot, newSeqRoot, size - 1, newFirst, newLast);
        }
        return this;
    }

    @Override
    public @NonNull SequencedChampMap<K, V> remove(@NonNull K key) {
        return remove(key, first, last);
    }

    @Override
    public @NonNull SequencedChampMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }
        var t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<SequencedEntry<K, V>> details = t.removeAndGiveDetails(key);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @NonNull
    private SequencedChampMap<K, V> renumber(
            BitmapIndexedNode<SequencedEntry<K, V>> root,
            BitmapIndexedNode<SequencedEntry<K, V>> seqRoot,
            int size, int first, int last) {
        if (mustRenumber(size, first, last)) {
            IdentityObject owner = new IdentityObject();
            BitmapIndexedNode<SequencedEntry<K, V>> renumberedRoot = ChampSequencedData.renumber(
                    size, root, seqRoot, owner,
                    SequencedEntry::entryKeyHash, SequencedEntry::keyEquals,
                    (e, seq) -> new SequencedEntry<>(e.getKey(), e.getValue(), seq));
            BitmapIndexedNode<SequencedEntry<K, V>> renumberedSeqRoot = ChampSequencedData.buildSequencedTrie(renumberedRoot, owner);
            return new SequencedChampMap<>(renumberedRoot, renumberedSeqRoot,
                    size, -1, size);
        }
        return new SequencedChampMap<>(root, seqRoot, size, first, last);
    }

    @Override
    public @NonNull SequencedChampMap<K, V> retainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        var t = this.toMutable();
        boolean modified = false;
        for (K key : readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.remove(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @NonNull Iterator<Map.Entry<K, V>> reverseIterator() {
        return new IteratorFacade<>(new ReverseChampSpliterator<SequencedEntry<K, V>, Map.Entry<K, V>>(sequenceRoot, Map.Entry.class::cast, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size()), null);
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull Spliterator<Map.Entry<K, V>> spliterator() {
        return new ChampSpliterator<>(this, e -> e, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.ORDERED, size());
    }

    /**
     * Creates a mutable copy of this map.
     *
     * @return a mutable sequenced CHAMP map
     */
    @Override
    public @NonNull MutableSequencedChampMap<K, V> toMutable() {
        return new MutableSequencedChampMap<>(this);
    }

    @Override
    public @NonNull MutableSequencedChampMap<K, V> asMap() {
        return new MutableSequencedChampMap<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    @Serial
    private @NonNull Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Serial
        @Override
        protected @NonNull Object readResolve() {
            return SequencedChampMap.of().putAll(deserialized);
        }
    }
}
