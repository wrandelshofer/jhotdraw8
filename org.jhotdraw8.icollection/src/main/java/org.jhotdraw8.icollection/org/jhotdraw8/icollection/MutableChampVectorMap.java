/*
 * @(#)MutableSequencedChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedMapFacade;
import org.jhotdraw8.icollection.facade.SequencedMapFacade;
import org.jhotdraw8.icollection.facade.SequencedSetFacade;
import org.jhotdraw8.icollection.impl.champ.AbstractMutableChampMap;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.impl.champ.ReverseTombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.champ.SequencedData;
import org.jhotdraw8.icollection.impl.champ.SequencedEntry;
import org.jhotdraw8.icollection.impl.champ.TombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.iteration.FailFastIterator;
import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedMapView;
import org.jhotdraw8.icollection.serialization.MapSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Implements the {@link SequencedMap} interface using a Compressed
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
 *     <li>toPersistent: O(1) + O(log N) distributed across subsequent updates in
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
 * See description at {@link ChampVectorMap}.
 * <p>
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Persistent Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-persistent-collections">michael.steindorfer.name</a>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class MutableChampVectorMap<K, V> extends AbstractMutableChampMap<K, V, SequencedEntry<K, V>>
        implements SequencedMap<K, V>, ReadableSequencedMap<K, V> {
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
    private VectorList<Object> vector;


    /**
     * Constructs a new empty map.
     */
    public MutableChampVectorMap() {
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
    public MutableChampVectorMap(Map<? extends K, ? extends V> c) {
        this((c instanceof MutableChampVectorMap<?, ?> mvm)
                ? ((MutableChampVectorMap<K, V>) mvm).toPersistent()
                : c.entrySet());
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Iterable}.
     *
     * @param c an iterable
     */
    @SuppressWarnings({"unchecked", "this-escape"})
    public MutableChampVectorMap(Iterable<? extends Entry<? extends K, ? extends V>> c) {
        if (c instanceof ChampVectorMap<?, ?>) {
            ChampVectorMap<K, V> that = (ChampVectorMap<K, V>) c;
            this.root = that.root;
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
    public MutableChampVectorMap<K, V> clone() {
        return (MutableChampVectorMap<K, V>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(@Nullable Object o) {
        return Node.NO_DATA != root.find(new SequencedEntry<>((K) o),
                SequencedEntry.keyHash(o), 0,
                SequencedEntry::keyEquals);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new FailFastIterator<>(Spliterators.iterator(spliterator()),
                this::iteratorRemove, () -> modCount);
    }

    private Iterator<Map.Entry<K, V>> reverseIterator() {
        return new FailFastIterator<>(Spliterators.iterator(reverseSpliterator()),
                this::iteratorRemove, () -> modCount);
    }

    @SuppressWarnings("unchecked")
    private Spliterator<Entry<K, V>> reverseSpliterator() {
        return new ReverseTombSkippingVectorSpliterator<>(vector,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent,
                        ((SequencedEntry<K, V>) e).getKey(), ((SequencedEntry<K, V>) e).getValue()),
                size(), characteristics() | Spliterator.NONNULL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        return new TombSkippingVectorSpliterator<>(vector.trie,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent,
                        ((SequencedEntry<K, V>) e).getKey(), ((SequencedEntry<K, V>) e).getValue()),
                0, size(), vector.size(), characteristics() | Spliterator.NONNULL);
    }


    @Override
    public Set<Entry<K, V>> entrySet() {
        return sequencedEntrySet();
    }

    @Override
    public Set<K> keySet() {
        return sequencedKeySet();
    }

    @Override
    public Collection<V> values() {
        return sequencedValues();
    }

    /**
     * Returns a {@link SequencedSet} view of the entries contained in this map.
     *
     * @return a view of the entries contained in this map
     */
    @Override
    public SequencedSet<Entry<K, V>> sequencedEntrySet() {
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
    public @Nullable V get(Object o) {
        Object result = root.find(
                new SequencedEntry<>((K) o),
                SequencedEntry.keyHash(o), 0, SequencedEntry::keyEquals);
        return (result instanceof SequencedEntry<?, ?>) ? ((SequencedEntry<K, V>) result).getValue() : null;
    }


    private void iteratorPutIfPresent(K k, V v) {
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
    public SequencedSet<K> sequencedKeySet() {
        return SequencedMapFacade.createKeySet(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : (SequencedEntry<K, V>) vector.getLast();
    }

    @Override
    public @Nullable Entry<K, V> pollFirstEntry() {
        var e = firstEntry();
        if (e == null) {
            return null;
        }
        remove(e.getKey());
        return e;
    }

    @Override
    public @Nullable Entry<K, V> pollLastEntry() {
        var e = lastEntry();
        if (e == null) {
            return null;
        }
        remove(e.getKey());
        return e;
    }

    @Override
    public @Nullable V put(K key, V value) {
        var oldData = putLast(key, value, false).getOldData();
        return oldData == null ? null : oldData.getValue();
    }


    @Override
    public @Nullable V putFirst(K key, V value) {
        var oldData = putFirst(key, value, true).getOldData();
        return oldData == null ? null : oldData.getValue();
    }

    private ChangeEvent<SequencedEntry<K, V>> putFirst(K key, V val, boolean moveToFirst) {
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
    public boolean putAll(Iterable<? extends Entry<? extends K, ? extends V>> c) {
        if (c instanceof MutableChampVectorMap<?, ?> that) {
            c = (Iterable<? extends Entry<? extends K, ? extends V>>) that.toPersistent();
        }
        if (isEmpty() && c instanceof ChampVectorMap<?, ?> that) {
            if (that.isEmpty()) {
                return false;
            }
            root = (BitmapIndexedNode<SequencedEntry<K, V>>) (BitmapIndexedNode<?>) that.root;
            vector = that.vector;
            offset = that.offset;
            size = that.size;
            modCount++;
            return true;
        }
        return super.putAll(c);
    }

    @Override
    public @Nullable V putLast(K key, V value) {
        var oldData = putLast(key, value, true).getOldData();
        return oldData == null ? null : oldData.getValue();
    }

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
    public ReadableSequencedMap<K, V> readableReversed() {
        return new ReadableSequencedMapFacade<>(
                this::iterator,
                this::reverseIterator,
                this::size,
                this::containsKey,
                this::get,
                this::lastEntry,
                this::firstEntry,
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, null);
    }

    @Override
    public @Nullable V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        ChangeEvent<SequencedEntry<K, V>> details = removeAndGiveDetails(key);
        if (details.isModified()) {
            return details.getOldData().getValue();
        }
        return null;
    }

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
            var result = SequencedData.vecRenumber(getOrCreateOwner(), size, vector.size(), root, vector.trie,
                    SequencedEntry::entryKeyHash, SequencedEntry::keyEquals,
                    (e, seq) -> new SequencedEntry<>(e.getKey(), e.getValue(), seq));
            root = result.first();
            vector = result.second();
            offset = 0;
        }
    }

    @Override
    public SequencedMap<K, V> reversed() {
        return new ReversedSequencedMapView<>(this);
    }

    /**
     * Returns an persistent copy of this map.
     *
     * @return an persistent copy
     */
    public ChampVectorMap<K, V> toPersistent() {
        owner = null;
        return size == 0 ? ChampVectorMap.of()
                : new ChampVectorMap<>(root, vector, size, offset);
    }

    @Override
    public SequencedCollection<V> sequencedValues() {
        return SequencedMapFacade.createValues(this);
    }

    @Serial
    private Object writeReplace() {
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
        protected Object readResolve() {
            return new MutableChampVectorMap<>(deserializedEntries);
        }
    }
}