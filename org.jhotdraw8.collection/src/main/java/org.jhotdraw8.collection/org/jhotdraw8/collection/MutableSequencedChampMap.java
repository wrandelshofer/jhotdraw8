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
import org.jhotdraw8.collection.impl.champ.*;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.sequenced.AbstractSequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedSet;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.Serial;
import java.util.*;

import static org.jhotdraw8.collection.impl.champ.ChampSequencedData.seqHash;

/**
 * Implements a mutable map using two Compressed Hash-Array Mapped Prefix-trees
 * (CHAMP), with predictable iteration order.
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
 *     <li>put, putFirst, putLast: O(1) in an amortized sense, because we
 *     sometimes have to renumber the elements.</li>
 *     <li>remove: O(1) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
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
 * See description at {@link SequencedChampMap}.
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
public class MutableSequencedChampMap<K, V> extends ChampAbstractMutableChampMap<K, V, ChampSequencedEntry<K, V>>
        implements SequencedMap<K, V>, ReadOnlySequencedMap<K, V> {
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * Counter for the sequence number of the last element. The counter is
     * incremented after a new entry is added to the end of the sequence.
     */
    private transient int last = 0;

    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement after a new entry has been added to the start of the sequence.
     */
    private int first = -1;
    /**
     * The root of the CHAMP trie for the sequence numbers.
     */
    private @NonNull ChampBitmapIndexedNode<ChampSequencedEntry<K, V>> sequenceRoot;


    /**
     * Constructs a new empty map.
     */
    public MutableSequencedChampMap() {
        root = ChampBitmapIndexedNode.emptyNode();
        sequenceRoot = ChampBitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Map}.
     *
     * @param m a map
     */
    public MutableSequencedChampMap(@NonNull Map<? extends K, ? extends V> m) {
        if (m instanceof MutableSequencedChampMap<?, ?>) {
            @SuppressWarnings("unchecked")
            MutableSequencedChampMap<K, V> that = (MutableSequencedChampMap<K, V>) m;
            this.mutator = null;
            that.mutator = null;
            this.root = that.root;
            this.size = that.size;
            this.modCount = 0;
            this.first = that.first;
            this.last = that.last;
            this.sequenceRoot = Objects.requireNonNull(that.sequenceRoot);
        } else {
            this.root = ChampBitmapIndexedNode.emptyNode();
            this.sequenceRoot = ChampBitmapIndexedNode.emptyNode();
            this.putAll(m);
        }
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link Iterable}.
     *
     * @param m an iterable
     */
    public MutableSequencedChampMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        this.root = ChampBitmapIndexedNode.emptyNode();
        this.sequenceRoot = ChampBitmapIndexedNode.emptyNode();
        for (Entry<? extends K, ? extends V> e : m) {
            this.put(e.getKey(), e.getValue());
        }
    }

    /**
     * Constructs a map containing the same entries as in the specified
     * {@link ReadOnlyMap}.
     *
     * @param m a read-only map
     */
    public MutableSequencedChampMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        if (m instanceof SequencedChampMap) {
            @SuppressWarnings("unchecked")
            SequencedChampMap<K, V> that = (SequencedChampMap<K, V>) m;
            this.root = that;
            this.size = that.size;
            this.first = that.first;
            this.last = that.last;
            this.sequenceRoot = that.sequenceRoot;
        } else {
            this.root = ChampBitmapIndexedNode.emptyNode();
            this.sequenceRoot = ChampBitmapIndexedNode.emptyNode();
            this.putAll(m.asMap());
        }
    }

    /**
     * Removes all entries from this map.
     */
    @Override
    public void clear() {
        root = ChampBitmapIndexedNode.emptyNode();
        sequenceRoot = ChampBitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
        first = -1;
        last = 0;
    }

    /**
     * Returns a shallow copy of this map.
     */
    @Override
    public @NonNull MutableSequencedChampMap<K, V> clone() {
        return (MutableSequencedChampMap<K, V>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(@Nullable Object o) {
        K key = (K) o;
        return ChampNode.NO_DATA != root.find(new ChampSequencedEntry<>(key),
                Objects.hashCode(key), 0,
                ChampSequencedEntry::keyEquals);
    }

    @Override
    public @NonNull Iterator<Entry<K, V>> iterator() {
        return new FailFastIterator<>(
                new IteratorFacade<>(new ChampSpliterator<>(sequenceRoot,
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                        Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size()), this::iteratorRemove),
                this::getModCount
        );
    }

    private @NonNull EnumeratorSpliterator<Entry<K, V>> reverseSpliterator() {
        return new FailFastSpliterator<>(new ChampReversedChampSpliterator<>(sequenceRoot,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size()),
                this::getModCount);
    }

    private @NonNull Iterator<Entry<K, V>> reverseIterator() {
        return new FailFastIterator<>(
                new IteratorFacade<>(new ChampReversedChampSpliterator<>(sequenceRoot,
                        e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                        Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size()), this::iteratorRemove),
                this::getModCount
        );
    }
    @Override
    public @NonNull EnumeratorSpliterator<Entry<K, V>> spliterator() {
        return new FailFastSpliterator<>(new ChampSpliterator<>(sequenceRoot,
                e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size()),
                () -> this.modCount);
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

    @Override
    public @Nullable Entry<K, V> firstEntry() {
        return isEmpty() ? null : ChampNode.getFirst(sequenceRoot);
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

    private void iteratorRemove(Map.Entry<K, V> entry) {
        mutator = null;
        remove(entry.getKey());
    }

    @Override
    public @NonNull SequencedSet<K> sequencedKeySet() {
        return AbstractSequencedMap.createKeySet(this);
    }

    @Override
    public @Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : ChampNode.getLast(sequenceRoot);
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        if (isEmpty()) {
            return null;
        }
        ChampSequencedEntry<K, V> entry = ChampNode.getFirst(sequenceRoot);
        remove(entry.getKey());
        first = entry.getSequenceNumber();
        renumber();
        return entry;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        if (isEmpty()) {
            return null;
        }
        ChampSequencedEntry<K, V> entry = ChampNode.getLast(sequenceRoot);
        remove(entry.getKey());
        last = entry.getSequenceNumber();
        renumber();
        return entry;
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

    private @NonNull ChampChangeEvent<ChampSequencedEntry<K, V>> putFirst(K key, V val,
                                                                          boolean moveToFirst) {
        var details = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
        var newEntry = new ChampSequencedEntry<>(key, val, first);
        IdentityObject mutator = getOrCreateIdentity();
        root = root.update(mutator,
                newEntry, Objects.hashCode(key), 0, details,
                moveToFirst ? ChampSequencedEntry::updateAndMoveToFirst : ChampSequencedEntry::update,
                ChampSequencedEntry::keyEquals, ChampSequencedEntry::keyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            sequenceRoot = ChampSequencedData.seqUpdate(sequenceRoot, null, details.getNewDataNonNull(), details,
                    ChampSequencedEntry::update);
            return details;
        }
        if (details.isModified()) {
            var seqDetails = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
            if (details.isReplaced()) {
                if (moveToFirst) {
                    ChampSequencedEntry<K, V> oldEntry = details.getOldDataNonNull();
                    sequenceRoot = ChampSequencedData.seqRemove(sequenceRoot, mutator, oldEntry, seqDetails);
                    last = oldEntry.getSequenceNumber() == last - 1 ? last - 1 : last;
                    first--;
                    modCount++;
                }
            } else {
                size++;
                first--;
                modCount++;
            }
            sequenceRoot = ChampSequencedData.seqUpdate(sequenceRoot, mutator, details.getNewDataNonNull(), seqDetails, ChampSequencedEntry::update);
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
    public ChampChangeEvent<ChampSequencedEntry<K, V>> putLast(final K key, V val, boolean moveToLast) {
        ChampChangeEvent<ChampSequencedEntry<K, V>> details = new ChampChangeEvent<>();
        ChampSequencedEntry<K, V> newEntry = new ChampSequencedEntry<>(key, val, last);
        IdentityObject mutator = getOrCreateIdentity();
        root = root.update(mutator,
                newEntry, Objects.hashCode(key), 0, details,
                moveToLast ? ChampSequencedEntry::updateAndMoveToLast : ChampSequencedEntry::update,
                ChampSequencedEntry::keyEquals, ChampSequencedEntry::keyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            sequenceRoot = ChampSequencedData.seqUpdate(sequenceRoot, null, details.getNewDataNonNull(), details,
                    ChampSequencedEntry::update);
            return details;
        }
        if (details.isModified()) {
            var seqDetails = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldEntry = details.getOldDataNonNull();
                    sequenceRoot = ChampSequencedData.seqRemove(sequenceRoot, mutator, oldEntry, seqDetails);
                    first = oldEntry.getSequenceNumber() == first + 1 ? first + 1 : first;
                    last++;
                    modCount++;
                }
            } else {
                last++;
                size++;
                modCount++;
            }
            sequenceRoot = ChampSequencedData.seqUpdate(sequenceRoot, mutator, details.getNewDataNonNull(), seqDetails, ChampSequencedEntry::update);
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
        ChampChangeEvent<ChampSequencedEntry<K, V>> details = removeAndGiveDetails(key);
        if (details.isModified() && details.getOldData() != null) {
            return details.getOldData().getValue();
        }
        return null;
    }

    @NonNull
    ChampChangeEvent<ChampSequencedEntry<K, V>> removeAndGiveDetails(K key) {
        ChampChangeEvent<ChampSequencedEntry<K, V>> details = new ChampChangeEvent<>();
        IdentityObject mutator = getOrCreateIdentity();
        root = root.remove(mutator,
                new ChampSequencedEntry<>(key), Objects.hashCode(key), 0, details,
                ChampSequencedEntry::keyEquals);
        if (details.isModified()) {
            size--;
            modCount++;
            var elem = details.getOldData();
            int seq = elem.getSequenceNumber();
            sequenceRoot = sequenceRoot.remove(mutator,
                    elem,
                    seqHash(seq), 0, details, ChampSequencedData::seqEquals);
            if (seq == last - 1) {
                last--;
            }
            if (seq == first) {
                first++;
            }
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
        if (ChampSequencedData.mustRenumber(size, first, last)) {
            IdentityObject mutator = getOrCreateIdentity();
            root = ChampSequencedData.renumber(size, root, sequenceRoot, mutator,
                    ChampSequencedEntry::keyHash, ChampSequencedEntry::keyEquals,
                    (e, seq) -> new ChampSequencedEntry<>(e.getKey(), e.getValue(), seq));
            sequenceRoot = ChampSequencedData.buildSequencedTrie(root, mutator);
            last = size;
            first = -1;
        }
    }

    @Override
    public @NonNull SequencedMap<K, V> reversed() {
        return new SequencedMapFacade<>(
                this::reverseIterator,
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
    public @NonNull SequencedChampMap<K, V> toImmutable() {
        mutator = null;
        return size == 0 ? SequencedChampMap.of() : new SequencedChampMap<>(root, sequenceRoot, size, first, last);
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
            return new MutableSequencedChampMap<>(deserialized);
        }
    }
}