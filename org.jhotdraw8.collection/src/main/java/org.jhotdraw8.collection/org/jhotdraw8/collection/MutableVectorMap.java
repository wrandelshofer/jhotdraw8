/*
 * @(#)MutableSequencedChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.collection.facade.SequencedMapFacade;
import org.jhotdraw8.collection.facade.SequencedSetFacade;
import org.jhotdraw8.collection.impl.champ.ChampAbstractMutableChampMap;
import org.jhotdraw8.collection.impl.champ.ChampBitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ.ChampChangeEvent;
import org.jhotdraw8.collection.impl.champ.ChampNode;
import org.jhotdraw8.collection.impl.champ.ChampReversedSequenceVectorSpliterator;
import org.jhotdraw8.collection.impl.champ.ChampSeqVectorSpliterator;
import org.jhotdraw8.collection.impl.champ.ChampSequencedData;
import org.jhotdraw8.collection.impl.champ.ChampSequencedEntry;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.sequenced.AbstractSequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedSet;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.Serial;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;

/**
 * Implements a mutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP) and a bit-mapped trie (Vector).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> entries</li>
 *     <li>allows null keys and null values</li>
 *     <li>is mutable</li>
 *     <li>is not thread-safe</li>
 *     <li>iterates in the order, in which keys were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>put, putFirst, putLast: O(1) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>remove: O(1) in an amortized sense, because we sometimes have to renumber the elements.</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toImmutable: O(1) + O(log N) distributed across subsequent updates in
 *     this mutable map</li>
 *     <li>clone: O(1) + O(log N) distributed across subsequent updates in this
 *     mutable map and in the clone</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst, getLast: O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * See description at {@link VectorMap}.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. BSD-2-Clause License</dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class MutableVectorMap<K, V> extends ChampAbstractMutableChampMap<K, V, ChampSequencedEntry<K, V>>
        implements SequencedMap<K, V>, ReadOnlySequencedMap<K, V> {
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * Offset of sequence numbers to vector indices.
     *
     * <pre>vector index = sequence number + offset</pre>
     */
    private int offset = 0;
    /**
     * In this vector we store the elements in the order in which they were inserted.
     */
    private @NonNull VectorList<Object> vector;


    /**
     * Constructs a new empty map.
     */
    public MutableVectorMap() {
        root = ChampBitmapIndexedNode.emptyNode();
        vector = VectorList.of();
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Map}.
     *
     * @param c a map
     */
    @SuppressWarnings("unchecked")
    public MutableVectorMap(@NonNull Map<? extends K, ? extends V> c) {
        this((c instanceof MutableVectorMap<?, ?> mvm)
                ? ((MutableVectorMap<K, V>) mvm).toImmutable()
                : c.entrySet());
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Iterable}.
     *
     * @param c an iterable
     */
    @SuppressWarnings("unchecked")
    public MutableVectorMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> c) {
        if (c instanceof VectorMap<?, ?>) {
            VectorMap<K, V> that = (VectorMap<K, V>) c;
            this.root = that;
            this.size = that.size;
            this.offset = that.offset;
            this.vector = that.vector;
        } else {
            this.root = ChampBitmapIndexedNode.emptyNode();
            this.vector = VectorList.of();
            putAll(c);
        }

    }


    /**
     * Removes all entries from this map.
     */
    @Override
    public void clear() {
        root = ChampBitmapIndexedNode.emptyNode();
        vector = VectorList.of();
        size = 0;
        modCount++;
        offset = -1;
    }

    /**
     * Returns a shallow copy of this map.
     */
    @Override
    public @NonNull MutableVectorMap<K, V> clone() {
        return (MutableVectorMap<K, V>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(@Nullable Object o) {
        return ChampNode.NO_DATA != root.find(new ChampSequencedEntry<>((K) o),
                Objects.hashCode(o), 0,
                ChampSequencedEntry::keyEquals);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return new FailFastIterator<>(new IteratorFacade<>(spliterator(),
                this::iteratorRemove), () -> modCount);
    }

    private @NonNull Iterator<Map.Entry<K, V>> reversedIterator() {
        return new FailFastIterator<>(new IteratorFacade<>(reversedSpliterator(),
                this::iteratorRemove), () -> modCount);
    }

    @SuppressWarnings("unchecked")
    private @NonNull EnumeratorSpliterator<Entry<K, V>> reversedSpliterator() {
        return new ChampReversedSequenceVectorSpliterator<Entry<K, V>>(vector,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent,
                        ((ChampSequencedEntry<K, V>) e).getKey(), ((ChampSequencedEntry<K, V>) e).getValue()),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull EnumeratorSpliterator<Entry<K, V>> spliterator() {
        return new ChampSeqVectorSpliterator<Entry<K, V>>(vector,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent,
                        ((ChampSequencedEntry<K, V>) e).getKey(), ((ChampSequencedEntry<K, V>) e).getValue()),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
    }


    /**
     * Returns a {@link Set} view of the entries contained in this map.
     *
     * @return a view of the entries contained in this map
     */
    @Override
    public @NonNull SequencedSet<Entry<K, V>> entrySet() {
        return sequencedEntrySet();
    }

    /**
     * Returns a {@link SequencedSet} view of the entries contained in this map.
     *
     * @return a view of the entries contained in this map
     */
    @Override
    public @NonNull SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return new SequencedSetFacade<>(
                this::iterator,
                this::spliterator,
                this::reversedIterator,
                this::reversedSpliterator,
                this::size,
                this::containsEntry,
                this::clear,
                this::removeEntry,
                this::firstEntry,
                this::lastEntry, null, null, null, null
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Entry<K, V> firstEntry() {
        return isEmpty() ? null : (ChampSequencedEntry<K, V>) vector.getFirst();
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no entry for the key.
     *
     * @param o the key whose associated value is to be returned
     * @return the associated value or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        Object result = root.find(
                new ChampSequencedEntry<>((K) o),
                Objects.hashCode(o), 0, ChampSequencedEntry::keyEquals);
        return (result instanceof ChampSequencedEntry<?, ?>) ? ((ChampSequencedEntry<K, V>) result).getValue() : null;
    }


    private void iteratorPutIfPresent(@NonNull K k, V v) {
        if (containsKey(k)) {
            mutator = null;
            put(k, v);
        }
    }

    private void iteratorRemove(Entry<K, V> entry) {
        mutator = null;
        remove(entry.getKey());
    }

    @Override
    public @NonNull SequencedSet<K> sequencedKeySet() {
        return AbstractSequencedMap.createKeySet(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : (ChampSequencedEntry<K, V>) vector.getLast();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        var e = firstEntry();
        if (e == null) return null;
        remove(e.getKey());
        return e;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        var e = lastEntry();
        if (e == null) return null;
        remove(e.getKey());
        return e;
    }

    @Override
    public V put(K key, V value) {
        var oldData = putLast(key, value, false).getOldData();
        return oldData == null ? null : oldData.getValue();
    }

    V putFirstFalse(K key, V value) {
        var oldData = putFirst(key, value, false).getOldData();
        return oldData == null ? null : oldData.getValue();
    }

    @Override
    public V putFirst(K key, V value) {
        var oldData = putFirst(key, value, true).getOldData();
        return oldData == null ? null : oldData.getValue();
    }

    private @NonNull ChampChangeEvent<ChampSequencedEntry<K, V>> putFirst(K key, V val, boolean moveToFirst) {
        var details = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
        var newEntry = new ChampSequencedEntry<>(key, val, -offset - 1);
        var mutator = getOrCreateIdentity();
        root = root.update(mutator, newEntry,
                Objects.hashCode(key), 0, details,
                moveToFirst ? ChampSequencedEntry::updateAndMoveToFirst : ChampSequencedEntry::update,
                ChampSequencedEntry::keyEquals, ChampSequencedEntry::keyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            vector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return details;
        }
        if (details.isModified()) {
            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = ChampSequencedData.vecRemove(vector, mutator, details.getOldDataNonNull(), new ChampChangeEvent<ChampSequencedEntry<K, V>>(), offset);
                    vector = result.first();
                }
            } else {
                modCount++;
                size++;
            }
            offset++;
            vector = vector.addFirst(newEntry);
            renumber();
        }
        return details;
    }

    @Override
    public V putLast(K key, V value) {
        var oldData = putLast(key, value, true).getOldData();
        return oldData == null ? null : oldData.getValue();
    }

    @NonNull
    ChampChangeEvent<ChampSequencedEntry<K, V>> putLast(final K key, V value, boolean moveToLast) {
        var details = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
        var newEntry = new ChampSequencedEntry<>(key, value, vector.size() - offset);
        var mutator = getOrCreateIdentity();
        root = root.update(mutator, newEntry,
                Objects.hashCode(key), 0, details,
                moveToLast ? ChampSequencedEntry::updateAndMoveToLast : ChampSequencedEntry::update,
                ChampSequencedEntry::keyEquals, ChampSequencedEntry::keyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            vector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return details;
        }
        if (details.isModified()) {
            if (details.isReplaced()) {
                var result = ChampSequencedData.vecRemove(vector, mutator, details.getOldDataNonNull(), new ChampChangeEvent<ChampSequencedEntry<K, V>>(), offset);
                vector = result.first();
                offset = result.second();
            } else {
                size++;
            }
            modCount++;
            vector = vector.add(newEntry);
            renumber();
        }
        return details;
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return new ReadOnlySequencedMapFacade<>(
                this::iterator,
                this::reversedIterator,
                this::size,
                this::containsKey,
                this::get,
                this::lastEntry,
                this::firstEntry
        );
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        ChampChangeEvent<ChampSequencedEntry<K, V>> details = removeAndGiveDetails(key);
        if (details.isModified()) {
            return details.getOldData().getValue();
        }
        return null;
    }

    @NonNull
    ChampChangeEvent<ChampSequencedEntry<K, V>> removeAndGiveDetails(K key) {
        var details = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
        root = root.remove(null,
                new ChampSequencedEntry<>(key),
                Objects.hashCode(key), 0, details, ChampSequencedEntry::keyEquals);
        if (details.isModified()) {
            var oldElem = details.getOldDataNonNull();
            var result = ChampSequencedData.vecRemove(vector, null, oldElem, new ChampChangeEvent<>(), offset);
            vector = result.first();
            offset = result.second();
            size--;
            modCount++;
            renumber();
        }
        return details;
    }


    /**
     * Renumbers the sequence numbers if they have overflown,
     * or if the extent of the sequence numbers is more than
     * 4 times the size of the set.
     */
    private void renumber() {
        if (ChampSequencedData.vecMustRenumber(size, offset, vector.size())) {
            IdentityObject mutator = getOrCreateIdentity();
            var result = ChampSequencedData.vecRenumber(size, root, vector, mutator,
                    ChampSequencedEntry::keyHash, ChampSequencedEntry::keyEquals,
                    (e, seq) -> new ChampSequencedEntry<>(e.getKey(), e.getValue(), seq));
            root = result.first();
            vector = result.second();
            offset = 0;
        }
    }

    @Override
    public @NonNull SequencedMap<K, V> reversed() {
        return new SequencedMapFacade<>(
                this::reversedIterator,
                this::iterator,
                this::size,
                this::containsKey,
                this::get,
                this::clear,
                this::remove,
                this::lastEntry,
                this::firstEntry,
                this::putFirstFalse,
                this::putLast,
                this::putFirst
        );
    }

    /**
     * Returns an immutable copy of this map.
     *
     * @return an immutable copy
     */
    public @NonNull VectorMap<K, V> toImmutable() {
        mutator = null;
        return size == 0 ? VectorMap.of() : new VectorMap<>(root, vector, size, offset);
    }

    @Override
    public @NonNull SequencedCollection<V> sequencedValues() {
        return AbstractSequencedMap.createValues(this);
    }

    @Serial
    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
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
            return new MutableVectorMap<>(deserialized);
        }
    }
}