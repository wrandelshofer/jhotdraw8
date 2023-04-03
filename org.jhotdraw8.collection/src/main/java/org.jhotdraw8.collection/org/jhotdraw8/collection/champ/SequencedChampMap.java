/*
 * @(#)ImmutableSequencedChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.collection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;

import static org.jhotdraw8.collection.champ.SequencedData.mustRenumber;
import static org.jhotdraw8.collection.champ.SequencedData.seqHash;

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
 *     <li>copyPut, copyPutFirst, copyPutLast: O(1) amortized, due to
 *     renumbering</li>
 *     <li>copyRemove: O(1) amortized, due to renumbering</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(1) with bucket sort, O(log N) with heap sort</li>
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
public class SequencedChampMap<K, V> extends BitmapIndexedNode<SequencedEntry<K, V>> implements ImmutableSequencedMap<K, V>, Serializable {
    private static final @NonNull SequencedChampMap<?, ?> EMPTY = new SequencedChampMap<>(BitmapIndexedNode.emptyNode(), BitmapIndexedNode.emptyNode(), 0, -1, 0);
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
    final transient int size;

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

    static <K, V> BitmapIndexedNode<SequencedEntry<K, V>> buildSequenceRoot(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root, @NonNull IdentityObject mutator) {
        BitmapIndexedNode<SequencedEntry<K, V>> seqRoot = emptyNode();
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        for (var i = new KeySpliterator<>(root, Function.identity(), 0, 0); i.moveNext(); ) {
            SequencedEntry<K, V> elem = i.current();
            seqRoot = seqRoot.update(mutator, elem, seqHash(elem.getSequenceNumber()),
                    0, details, (oldK, newK) -> oldK, SequencedData::seqEquals, SequencedData::seqHash);
        }
        return seqRoot;
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

    @NonNull
    private static <K, V> SequencedEntry<K, V> update(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue()) ? oldK :
                new SequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber());
    }

    @NonNull
    private static <K, V> SequencedEntry<K, V> updateAndMoveToFirst(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() + 1 ? oldK : newK;
    }

    @NonNull
    private static <K, V> SequencedEntry<K, V> updateAndMoveToLast(@NonNull SequencedEntry<K, V> oldK, @NonNull SequencedEntry<K, V> newK) {
        return Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() - 1 ? oldK : newK;
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
        return find(new SequencedEntry<>(key), Objects.hashCode(key), 0,
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
                Objects.hashCode(o), 0, SequencedEntry::keyEquals);
        return (V) ((result instanceof SequencedEntry<?, ?> entry) ? entry.getValue() : null);
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iterableToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return new IteratorFacade<>(new KeySpliterator<SequencedEntry<K, V>, Map.Entry<K, V>>(sequenceRoot, Map.Entry.class::cast, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size()), null);
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
        var newRoot = update(null,
                newEntry,
                Objects.hashCode(key), 0, details,
                moveToFirst ? SequencedChampMap::updateAndMoveToFirst : SequencedChampMap::update,
                SequencedEntry::keyEquals, SequencedEntry::keyHash);
        if (details.isModified()) {
            var newSeqRoot = sequenceRoot;
            int newSize = size;
            int newFirst = first;
            int newLast = last;
            var mutator = new IdentityObject();
            if (details.isReplaced()) {
                var oldEntry = details.getData();
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldEntry, seqHash(oldEntry.getSequenceNumber()), 0, details,
                        SequencedData::seqEquals);
                newFirst = details.getData().getSequenceNumber() == newFirst ? newFirst : newFirst - 1;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast - 1 : newLast;
            } else {
                newFirst--;
                newSize++;
            }
            newSeqRoot = newSeqRoot.update(mutator,
                    newEntry, seqHash(first - 1), 0, details,
                    SequencedChampMap::update,
                    SequencedData::seqEquals, SequencedData::seqHash);
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
        var newRoot = update(null,
                newEntry,
                Objects.hashCode(key), 0, details,
                moveToLast ? SequencedChampMap::updateAndMoveToLast : SequencedChampMap::update,
                SequencedEntry::keyEquals, SequencedEntry::keyHash);
        if (details.isModified()) {
            var newSeqRoot = sequenceRoot;
            int newFirst = first;
            int newLast = last;
            int newSize = size;
            var mutator = new IdentityObject();
            if (details.isReplaced()) {
                var oldEntry = details.getData();
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldEntry, seqHash(oldEntry.getSequenceNumber()), 0, details,
                        SequencedData::seqEquals);
                newFirst = details.getData().getSequenceNumber() == newFirst - 1 ? newFirst - 1 : newFirst;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast : newLast + 1;
            } else {
                newSize++;
                newLast++;
            }
            newSeqRoot = newSeqRoot.update(mutator,
                    newEntry, seqHash(last), 0, details,
                    SequencedChampMap::update,
                    SequencedData::seqEquals, SequencedData::seqHash);
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
        int keyHash = Objects.hashCode(key);
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRoot =
                remove(null, new SequencedEntry<>(key), keyHash, 0, details, SequencedEntry::keyEquals);
        BitmapIndexedNode<SequencedEntry<K, V>> newSeqRoot = sequenceRoot;
        if (details.isModified()) {
            var oldEntry = details.getData();
            int seq = oldEntry.getSequenceNumber();
            newSeqRoot = newSeqRoot.remove(null,
                    oldEntry,
                    seqHash(seq), 0, details, SequencedData::seqEquals);
            if (seq == newFirst) {
                newFirst++;
            }
            if (seq == newLast - 1) {
                newLast--;
            }
            return renumber(newRoot, newSeqRoot, size - 1, newFirst, newLast);
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
            IdentityObject mutator = new IdentityObject();
            BitmapIndexedNode<SequencedEntry<K, V>> renumberedRoot = SequencedData.renumber(
                    size, root, seqRoot, mutator,
                    SequencedEntry::keyHash, SequencedEntry::keyEquals,
                    (e, seq) -> new SequencedEntry<>(e.getKey(), e.getValue(), seq));
            BitmapIndexedNode<SequencedEntry<K, V>> renumberedSeqRoot = buildSequenceRoot(renumberedRoot, mutator);
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
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @NonNull Iterator<Map.Entry<K, V>> reverseIterator() {
        return new IteratorFacade<>(new ReversedKeySpliterator<SequencedEntry<K, V>, Map.Entry<K, V>>(sequenceRoot, Map.Entry.class::cast, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size()), null);
    }

    @Override
    public int size() {
        return size;
    }

    public @NonNull Spliterator<Map.Entry<K, V>> spliterator() {
        return new KeySpliterator<>(this, e -> e, Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.ORDERED, size());
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
    public @NonNull String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    private @NonNull Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return SequencedChampMap.of().putAll(deserialized);
        }
    }
}
