/*
 * @(#)SequencedChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.FailFastIterator;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.MutableMapEntry;
import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.collection.facade.SequencedMapFacade;
import org.jhotdraw8.collection.facade.SequencedSetFacade;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.sequenced.AbstractSequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedSet;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.champ.ChampChampImmutableSequencedSet.seqHash;

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
 *     <li>put, putFirst, putLast: O(1) amortized, due to renumbering</li>
 *     <li>remove: O(1) amortized, due to renumbering</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toImmutable: O(1) + O(log N) distributed across subsequent updates in
 *     this mutable map</li>
 *     <li>clone: O(1) + O(log N) distributed across subsequent updates in this
 *     mutable map and in the clone</li>
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
 * The CHAMP trie contains nodes that may be shared with other maps, and nodes
 * that are exclusively owned by this map.
 * <p>
 * If a write operation is performed on an exclusively owned node, then this
 * map is allowed to mutate the node (mutate-on-write).
 * If a write operation is performed on a potentially shared node, then this
 * map is forced to create an exclusive copy of the node and of all not (yet)
 * exclusively owned parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1) in either
 * case.
 * <p>
 * This map can create an immutable copy of itself in O(1) time and O(1) space
 * using method {@link #toImmutable()}. This map loses exclusive ownership of
 * all its tree nodes.
 * Thus, creating an immutable copy increases the constant cost of
 * subsequent writes, until all shared nodes have been gradually replaced by
 * exclusively owned nodes again.
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
public class ChampChampSequencedMap<K, V> extends AbstractChampMap<K, V, SequencedEntry<K, V>>
        implements SequencedMap<K, V>, ReadOnlySequencedMap<K, V> {
    private final static long serialVersionUID = 0L;
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
    private @NonNull BitmapIndexedNode<SequencedEntry<K, V>> sequenceRoot;


    public ChampChampSequencedMap() {
        root = BitmapIndexedNode.emptyNode();
        sequenceRoot = BitmapIndexedNode.emptyNode();
    }

    /**
     * Constructs a map containing the same mappings as in the specified
     * {@link Map}.
     *
     * @param m a map
     */
    public ChampChampSequencedMap(@NonNull Map<? extends K, ? extends V> m) {
        if (m instanceof ChampChampSequencedMap<?, ?>) {
            @SuppressWarnings("unchecked")
            ChampChampSequencedMap<K, V> that = (ChampChampSequencedMap<K, V>) m;
            this.mutator = null;
            that.mutator = null;
            this.root = that.root;
            this.size = that.size;
            this.modCount = 0;
            this.first = that.first;
            this.last = that.last;
            this.sequenceRoot = Objects.requireNonNull(that.sequenceRoot);
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            this.sequenceRoot = BitmapIndexedNode.emptyNode();
            this.putAll(m);
        }
    }

    /**
     * Constructs a map containing the same mappings as in the specified
     * {@link Iterable}.
     *
     * @param m an iterable
     */
    public ChampChampSequencedMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> m) {
        this.root = BitmapIndexedNode.emptyNode();
        this.sequenceRoot = BitmapIndexedNode.emptyNode();
        for (Entry<? extends K, ? extends V> e : m) {
            this.put(e.getKey(), e.getValue());
        }
    }

    /**
     * Constructs a map containing the same mappings as in the specified
     * {@link ReadOnlyMap}.
     *
     * @param m a read-only map
     */
    public ChampChampSequencedMap(@NonNull ReadOnlyMap<? extends K, ? extends V> m) {
        if (m instanceof ChampChampImmutableSequencedMap) {
            @SuppressWarnings("unchecked")
            ChampChampImmutableSequencedMap<K, V> that = (ChampChampImmutableSequencedMap<K, V>) m;
            this.root = that;
            this.size = that.size;
            this.first = that.first;
            this.last = that.last;
            this.sequenceRoot = that.sequenceRoot;
        } else {
            this.root = BitmapIndexedNode.emptyNode();
            this.sequenceRoot = BitmapIndexedNode.emptyNode();
            this.putAll(m.asMap());
        }
    }

    @Override
    public void clear() {
        root = BitmapIndexedNode.emptyNode();
        sequenceRoot = BitmapIndexedNode.emptyNode();
        size = 0;
        modCount++;
        first = -1;
        last = 0;
    }

    /**
     * Returns a shallow copy of this map.
     */
    @Override
    public @NonNull ChampChampSequencedMap<K, V> clone() {
        return (ChampChampSequencedMap<K, V>) super.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(@NonNull Object o) {
        K key = (K) o;
        return Node.NO_DATA != root.find(new SequencedEntry<>(key),
                Objects.hashCode(key), 0,
                getEqualsFunction());
    }

    private @NonNull Iterator<Entry<K, V>> entryIterator(boolean reversed) {
        Enumerator<Entry<K, V>> i;
        if (reversed) {
            i = new ReversedKeyEnumeratorSpliterator<>(sequenceRoot,
                    e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                    Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
        } else {
            i = new KeyEnumeratorSpliterator<>(sequenceRoot,
                    e -> new MutableMapEntry<>(this::iteratorPutIfPresent, e.getKey(), e.getValue()),
                    Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
        }
        return new FailFastIterator<>(new IteratorFacade<>(i, this::iteratorRemove), () -> ChampChampSequencedMap.this.modCount);
    }


    private @NonNull Spliterator<Entry<K, V>> entrySpliterator(boolean reversed) {
        Spliterator<Entry<K, V>> i;
        if (reversed) {
            i = new ReversedKeyEnumeratorSpliterator<>(sequenceRoot, e -> e, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
        } else {
            i = new KeyEnumeratorSpliterator<>(sequenceRoot, e -> e, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, size());
        }
        return i;
    }

    private @NonNull Spliterator<Entry<K, V>> entrySpliterator() {
        return entrySpliterator(false);
    }

    @Override
    public @NonNull SequencedSet<Entry<K, V>> entrySet() {
        return sequencedEntrySet();
    }

    @Override
    public @NonNull SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return new SequencedSetFacade<>(
                () -> entryIterator(false),
                () -> entrySpliterator(false),
                () -> entryIterator(true),
                () -> entrySpliterator(false),
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
        return isEmpty() ? null : Node.getFirst(sequenceRoot);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        Object result = root.find(
                new SequencedEntry<>((K) o),
                Objects.hashCode(o), 0, getEqualsFunction());
        return (result instanceof SequencedEntry<?, ?>) ? ((SequencedEntry<K, V>) result).getValue() : null;
    }

    @NonNull
    private BiPredicate<SequencedEntry<K, V>, SequencedEntry<K, V>> getEqualsFunction() {
        return (a, b) -> Objects.equals(a.getKey(), b.getKey());
    }

    @NonNull
    private ToIntFunction<SequencedEntry<K, V>> getHashFunction() {
        return (a) -> Objects.hashCode(a.getKey());
    }

    @NonNull
    private BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> getUpdateAndMoveToFirstFunction() {
        return (oldK, newK) -> (Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() + 1) ? oldK : newK;
    }

    @NonNull
    private BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> getUpdateAndMoveToLastFunction() {
        return (oldK, newK) -> (Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() - 1) ? oldK : newK;
    }

    @NonNull
    private BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> getUpdateFunction() {
        return (oldv, newv) -> Objects.equals(oldv.getValue(), newv.getValue()) ? oldv : newv;
    }

    private void iteratorPutIfPresent(@NonNull K k, V v) {
        if (containsKey(k)) {
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
        return isEmpty() ? null : Node.getLast(sequenceRoot);
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        if (isEmpty()) {
            return null;
        }
        SequencedEntry<K, V> entry = HeapSequencedIterator.getFirst(root, first, last);
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
        SequencedEntry<K, V> entry = HeapSequencedIterator.getLast(root, first, last);
        remove(entry.getKey());
        last = entry.getSequenceNumber();
        renumber();
        return entry;
    }

    @Override
    public V put(K key, V value) {
        SequencedEntry<K, V> oldValue = this.putLast(key, value, false).getData();
        return oldValue == null ? null : oldValue.getValue();
    }

    @Override
    public V putFirst(K key, V value) {
        SequencedEntry<K, V> oldValue = putFirst(key, value, true).getData();
        return oldValue == null ? null : oldValue.getValue();
    }

    private @NonNull ChangeEvent<SequencedEntry<K, V>> putFirst(K key, V val,
                                                                boolean moveToFirst) {
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        SequencedEntry<K, V> newElem = new SequencedEntry<>(key, val, first);
        IdentityObject mutator = getOrCreateMutator();
        root = root.update(mutator,
                newElem, Objects.hashCode(key), 0, details,
                moveToFirst ? getUpdateAndMoveToFirstFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        if (details.isModified()) {
            SequencedEntry<K, V> oldElem = details.getData();
            boolean isUpdated = details.isUpdated();
            sequenceRoot = sequenceRoot.update(mutator,
                    newElem, seqHash(first - 1), 0, details,
                    getUpdateFunction(),
                    Objects::equals, ChampChampImmutableSequencedMap::seqHashCode);
            if (isUpdated) {
                sequenceRoot = sequenceRoot.remove(mutator,
                        oldElem, seqHash(oldElem.getSequenceNumber()), 0, details,
                        Objects::equals);

                first = details.getData().getSequenceNumber() == first ? first : first - 1;
                last = details.getData().getSequenceNumber() == last ? last - 1 : last;
            } else {
                modCount++;
                first--;
                size++;
            }
            renumber();
        }
        return details;
    }

    @Override
    public V putLast(K key, V value) {
        SequencedEntry<K, V> oldValue = putLast(key, value, true).getData();
        return oldValue == null ? null : oldValue.getValue();
    }

    @NonNull ChangeEvent<SequencedEntry<K, V>> putLast(
            final K key, V val, boolean moveToLast) {
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        SequencedEntry<K, V> newElem = new SequencedEntry<>(key, val, last);
        IdentityObject mutator = getOrCreateMutator();
        root = root.update(mutator,
                newElem, Objects.hashCode(key), 0, details,
                moveToLast ? getUpdateAndMoveToLastFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        if (details.isModified()) {
            SequencedEntry<K, V> oldElem = details.getData();
            boolean isUpdated = details.isUpdated();
            sequenceRoot = sequenceRoot.update(mutator,
                    newElem, seqHash(last), 0, details,
                    getUpdateFunction(),
                    Objects::equals, ChampChampImmutableSequencedMap::seqHashCode);
            if (isUpdated) {
                sequenceRoot = sequenceRoot.remove(mutator,
                        oldElem, seqHash(oldElem.getSequenceNumber()), 0, details,
                        Objects::equals);

                first = details.getData().getSequenceNumber() == first - 1 ? first - 1 : first;
                last = details.getData().getSequenceNumber() == last ? last : last + 1;
            } else {
                modCount++;
                size++;
                last++;
            }
            renumber();
        }
        return details;
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return new ReadOnlySequencedMapFacade<>(
                () -> entryIterator(true),
                () -> entryIterator(false),
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
            return details.getData().getValue();
        }
        return null;
    }

    @NonNull ChangeEvent<SequencedEntry<K, V>> removeAndGiveDetails(K key) {
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        IdentityObject mutator = getOrCreateMutator();
        root = root.remove(mutator,
                new SequencedEntry<>(key), Objects.hashCode(key), 0, details,
                getEqualsFunction());
        if (details.isModified()) {
            size--;
            modCount++;
            var elem = details.getData();
            int seq = elem.getSequenceNumber();
            sequenceRoot = sequenceRoot.remove(mutator,
                    elem,
                    seqHash(seq), 0, details, Objects::equals);
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
        if (ChampImmutableSequencedSet.mustRenumber(size, first, last)) {
            root = SequencedEntry.renumber(size, root, getOrCreateMutator(),
                    getHashFunction(), getEqualsFunction());
            last = size;
            first = -1;
        }
    }

    @Override
    public @NonNull SequencedMap<K, V> reversed() {
        return new SequencedMapFacade<>(
                () -> entryIterator(true),
                () -> entryIterator(false),
                this::size,
                this::containsKey,
                this::get,
                this::clear,
                this::remove,
                this::lastEntry,
                this::firstEntry,
                this::put,
                this::putLast,
                this::putFirst
        );
    }

    /**
     * Returns an immutable copy of this map.
     *
     * @return an immutable copy
     */
    public @NonNull ChampChampImmutableSequencedMap<K, V> toImmutable() {
        mutator = null;
        return size == 0 ? ChampChampImmutableSequencedMap.of() : new ChampChampImmutableSequencedMap<>(root, sequenceRoot, size, first, last);
    }

    @Override
    public @NonNull SequencedCollection<V> sequencedValues() {
        return AbstractSequencedMap.createValues(this);
    }

    private @NonNull Object writeReplace() {
        return new SerializationProxy<>(this);
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return new ChampChampSequencedMap<>(deserialized);
        }
    }
}