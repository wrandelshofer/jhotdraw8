/*
 * @(#)MutableSequencedChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.pcollection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.pcollection.facade.SequencedMapFacade;
import org.jhotdraw8.pcollection.facade.SequencedSetFacade;
import org.jhotdraw8.pcollection.impl.champ.*;
import org.jhotdraw8.pcollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.pcollection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.pcollection.sequenced.ReversedSequencedMapView;
import org.jhotdraw8.pcollection.sequenced.SequencedCollection;
import org.jhotdraw8.pcollection.sequenced.SequencedMap;
import org.jhotdraw8.pcollection.sequenced.SequencedSet;
import org.jhotdraw8.pcollection.serialization.MapSerializationProxy;

import java.io.Serial;
import java.util.*;

/**
 * Implements the {@code SequencedMap} interface using a Compressed
 * Hash-Array Mapped Prefix-tree (CHAMP) and a bit-mapped trie (Vector).
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
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class MutableVectorMap<K, V> extends AbstractMutableChampMap<K, V, SequencedEntry<K, V>>
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
        root = BitmapIndexedNode.emptyNode();
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
            this.root = BitmapIndexedNode.emptyNode();
            this.vector = VectorList.of();
            putAll(c);
        }

    }


    /**
     * Removes all entries from this map.
     */
    @Override
    public void clear() {
        root = BitmapIndexedNode.emptyNode();
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
        return Node.NO_DATA != root.find(new SequencedEntry<>((K) o),
                SequencedEntry.keyHash(o), 0,
                SequencedEntry::keyEquals);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return new FailFastIterator<>(Spliterators.iterator(spliterator()),
                this::iteratorRemove, () -> modCount);
    }

    private @NonNull Iterator<Map.Entry<K, V>> reverseIterator() {
        return new FailFastIterator<>(Spliterators.iterator(reverseSpliterator()),
                this::iteratorRemove, () -> modCount);
    }

    @SuppressWarnings("unchecked")
    private @NonNull Spliterator<Entry<K, V>> reverseSpliterator() {
        return new ReverseTombSkippingVectorSpliterator<Entry<K, V>>(vector,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent,
                        ((SequencedEntry<K, V>) e).getKey(), ((SequencedEntry<K, V>) e).getValue()),
                size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Spliterator<Entry<K, V>> spliterator() {
        return new TombSkippingVectorSpliterator<Entry<K, V>>(vector,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent,
                        ((SequencedEntry<K, V>) e).getKey(), ((SequencedEntry<K, V>) e).getValue()),
                0, size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED);
    }


    /**
     * Returns a {@link Set} view of the entries contained in this map.
     *
     * @return a view of the entries contained in this map
     */
    @Override
    public @NonNull SequencedSet<Entry<K, V>> entrySet() {
        return _sequencedEntrySet();
    }

    /**
     * Returns a {@link SequencedSet} view of the entries contained in this map.
     *
     * @return a view of the entries contained in this map
     */
    @Override
    public @NonNull SequencedSet<Entry<K, V>> _sequencedEntrySet() {
        return new SequencedSetFacade<>(
                this::iterator,
                this::spliterator,
                this::reverseIterator,
                this::reverseSpliterator,
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
        return isEmpty() ? null : (SequencedEntry<K, V>) vector.getFirst();
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
                new SequencedEntry<>((K) o),
                SequencedEntry.keyHash(o), 0, SequencedEntry::keyEquals);
        return (result instanceof SequencedEntry<?, ?>) ? ((SequencedEntry<K, V>) result).getValue() : null;
    }


    private void iteratorPutIfPresent(@NonNull K k, V v) {
        if (containsKey(k)) {
            owner = null;
            put(k, v);
        }
    }

    private void iteratorRemove(Entry<K, V> entry) {
        owner = null;
        remove(entry.getKey());
    }

    @Override
    public @NonNull SequencedSet<K> _sequencedKeySet() {
        return SequencedMapFacade.createKeySet(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : (SequencedEntry<K, V>) vector.getLast();
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

    private @NonNull ChangeEvent<SequencedEntry<K, V>> putFirst(K key, V val, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, val, -offset - 1);
        root = root.put(getOrCreateOwner(), newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToFirst ? SequencedEntry::updateAndMoveToFirst : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            vector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return details;
        }
        if (details.isModified()) {
            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = SequencedData.vecRemove(vector, details.getOldDataNonNull(), offset);
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

    @SuppressWarnings("unchecked")
    @Override
    public boolean putAll(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> c) {
        if (c instanceof MutableVectorMap<?, ?> that) {
            c = (Iterable<? extends Entry<? extends K, ? extends V>>) that.toImmutable();
        }
        if (isEmpty() && c instanceof VectorMap<?, ?> that) {
            if (that.isEmpty()) {
                return false;
            }
            root = (BitmapIndexedNode<SequencedEntry<K, V>>) (BitmapIndexedNode<?>) that;
            vector = that.vector;
            offset = that.offset;
            size = that.size;
            modCount++;
            return true;
        }
        return super.putAll(c);
    }

    @Override
    public V putLast(K key, V value) {
        var oldData = putLast(key, value, true).getOldData();
        return oldData == null ? null : oldData.getValue();
    }

    @NonNull
    ChangeEvent<SequencedEntry<K, V>> putLast(final K key, V value, boolean moveToLast) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, vector.size() - offset);
        root = root.put(getOrCreateOwner(), newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToLast ? SequencedEntry::updateAndMoveToLast : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            vector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return details;
        }
        if (details.isModified()) {
            if (details.isReplaced()) {
                var result = SequencedData.vecRemove(vector, details.getOldDataNonNull(), offset);
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
                this::reverseIterator,
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
        ChangeEvent<SequencedEntry<K, V>> details = removeAndGiveDetails(key);
        if (details.isModified()) {
            return details.getOldData().getValue();
        }
        return null;
    }

    @NonNull
    ChangeEvent<SequencedEntry<K, V>> removeAndGiveDetails(K key) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        root = root.remove(getOrCreateOwner(),
                new SequencedEntry<>(key),
                SequencedEntry.keyHash(key), 0, details, SequencedEntry::keyEquals);
        if (details.isModified()) {
            var oldElem = details.getOldDataNonNull();
            var result = SequencedData.vecRemove(vector, oldElem, offset);
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
        if (SequencedData.vecMustRenumber(size, offset, vector.size())) {
            var result = SequencedData.vecRenumber(getOrCreateOwner(), size, root, vector,
                    SequencedEntry::entryKeyHash, SequencedEntry::keyEquals,
                    (e, seq) -> new SequencedEntry<>(e.getKey(), e.getValue(), seq));
            root = result.first();
            vector = result.second();
            offset = 0;
        }
    }

    @Override
    public @NonNull SequencedMap<K, V> _reversed() {
        return new ReversedSequencedMapView<>(this);
    }

    /**
     * Returns an immutable copy of this map.
     *
     * @return an immutable copy
     */
    public @NonNull VectorMap<K, V> toImmutable() {
        owner = null;
        return size == 0 ? VectorMap.of()
                : root instanceof VectorMap<K, V> m ? m : new VectorMap<>(root, vector, size, offset);
    }

    @Override
    public @NonNull SequencedCollection<V> _sequencedValues() {
        return SequencedMapFacade.createValues(this);
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