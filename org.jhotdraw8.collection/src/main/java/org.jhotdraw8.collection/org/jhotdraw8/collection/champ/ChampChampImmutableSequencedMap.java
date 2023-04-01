/*
 * @(#)ImmutableSequencedChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.immutable.ImmutableMap;
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
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.collection.champ.ChampChampImmutableSequencedSet.seqHash;

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
public class ChampChampImmutableSequencedMap<K, V> extends BitmapIndexedNode<SequencedEntry<K, V>> implements ImmutableSequencedMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private static final @NonNull ChampChampImmutableSequencedMap<?, ?> EMPTY = new ChampChampImmutableSequencedMap<>(BitmapIndexedNode.emptyNode(), BitmapIndexedNode.emptyNode(), 0, -1, 0);
    /**
     * Counter for the sequence number of the last entry.
     * The counter is incremented after a new entry is added to the end of the
     * sequence.
     */
    final int last;
    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement after a new entry has been added to the start of the sequence.
     */
    final int first;
    final transient int size;
    final @NonNull BitmapIndexedNode<SequencedEntry<K, V>> sequenceRoot;

    ChampChampImmutableSequencedMap(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root,
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
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        for (KeyIterator<SequencedEntry<K, V>> i = new KeyIterator<>(root, null); i.hasNext(); ) {
            SequencedEntry<K, V> elem = i.next();
            seqRoot = seqRoot.update(mutator, elem, seqHash(elem.getSequenceNumber()),
                    0, details, (oldK, newK) -> oldK, Object::equals, ChampChampImmutableSequencedMap::seqHashCode);
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
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampChampImmutableSequencedMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return ((ChampChampImmutableSequencedMap<K, V>) ChampChampImmutableSequencedMap.EMPTY).putAll(map);
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
    public static <K, V> @NonNull ChampChampImmutableSequencedMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ((ChampChampImmutableSequencedMap<K, V>) ChampChampImmutableSequencedMap.EMPTY).putAll(map);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampChampImmutableSequencedMap<K, V> of() {
        return (ChampChampImmutableSequencedMap<K, V>) ChampChampImmutableSequencedMap.EMPTY;
    }

    /**
     * Returns an immutable map that contains the provided entries.
     *
     * @param entries map entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return an immutable map of the provided entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampChampImmutableSequencedMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return (ChampChampImmutableSequencedMap<K, V>) of().putAll(entries);
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return find(new SequencedEntry<>(key), Objects.hashCode(key), 0,
                getEqualsFunction()) != Node.NO_DATA;
    }


    @NonNull
    private ChampChampImmutableSequencedMap<K, V> copyPutFirst(@NonNull K key, @Nullable V value, boolean moveToFirst) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        SequencedEntry<K, V> newEntry = new SequencedEntry<>(key, value, first);
        BitmapIndexedNode<SequencedEntry<K, V>> newRoot = update(null,
                newEntry,
                keyHash, 0, details,
                moveToFirst ? getUpdateAndMoveToFirstFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        var newSeqRoot = sequenceRoot;
        int newSize = size;
        int newFirst = first;
        int newLast = last;
        if (details.isModified()) {
            IdentityObject mutator = new IdentityObject();
            SequencedEntry<K, V> oldEntry = details.getData();
            boolean isUpdated = details.isUpdated();
            newSeqRoot = newSeqRoot.update(mutator,
                    newEntry, seqHash(first - 1), 0, details,
                    getUpdateFunction(),
                    Objects::equals, ChampChampImmutableSequencedMap::seqHashCode);
            if (isUpdated) {
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldEntry, seqHash(oldEntry.getSequenceNumber()), 0, details,
                        Objects::equals);

                newFirst = details.getData().getSequenceNumber() == newFirst ? newFirst : newFirst - 1;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast - 1 : newLast;
            } else {
                newFirst--;
                newSize++;
            }
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> putLast(@NonNull K key, @Nullable V value) {
        return copyPutLast(key, value, true);
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> putFirst(@NonNull K key, @Nullable V value) {
        return copyPutFirst(key, value, true);
    }

    @NonNull
    private ChampChampImmutableSequencedMap<K, V> copyPutLast(@NonNull K key, @Nullable V value, boolean moveToLast) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        SequencedEntry<K, V> newEntry = new SequencedEntry<>(key, value, last);
        BitmapIndexedNode<SequencedEntry<K, V>> newRoot = update(null,
                newEntry,
                keyHash, 0, details,
                moveToLast ? getUpdateAndMoveToLastFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        var newSeqRoot = sequenceRoot;
        int newFirst = first;
        int newLast = last;
        int newSize = size;
        if (details.isModified()) {
            IdentityObject mutator = new IdentityObject();
            SequencedEntry<K, V> oldEntry = details.getData();
            boolean isUpdated = details.isUpdated();
            newSeqRoot = newSeqRoot.update(mutator,
                    newEntry, seqHash(last), 0, details,
                    getUpdateFunction(),
                    Objects::equals, ChampChampImmutableSequencedMap::seqHashCode);
            if (isUpdated) {
                newSeqRoot = newSeqRoot.remove(mutator,
                        oldEntry, seqHash(oldEntry.getSequenceNumber()), 0, details,
                        Objects::equals);

                newFirst = details.getData().getSequenceNumber() == newFirst - 1 ? newFirst - 1 : newFirst;
                newLast = details.getData().getSequenceNumber() == newLast ? newLast : newLast + 1;
            } else {
                newSize++;
                newLast++;
            }
            return renumber(newRoot, newSeqRoot, newSize, newFirst, newLast);
        }
        return this;
    }

    private @NonNull ChampChampImmutableSequencedMap<K, V> copyRemove(@NonNull K key, int newFirst, int newLast) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRoot =
                remove(null, new SequencedEntry<>(key), keyHash, 0, details, getEqualsFunction());
        BitmapIndexedNode<SequencedEntry<K, V>> newSeqRoot = sequenceRoot;
        if (details.isModified()) {
            var oldEntry = details.getData();
            int seq = oldEntry.getSequenceNumber();
            newSeqRoot = newSeqRoot.remove(null,
                    oldEntry,
                    seqHash(seq), 0, details, Objects::equals);
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
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof ChampChampImmutableSequencedMap) {
            ChampChampImmutableSequencedMap<?, ?> that = (ChampChampImmutableSequencedMap<?, ?>) other;
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
        return (oldK, newK) -> Objects.equals(oldK.getValue(), newK.getValue()) ? oldK :
                new SequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber());
    }

    @Override
    public int hashCode() {
        return ReadOnlyMap.iterableToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    private @NonNull Iterator<Map.Entry<K, V>> iterator(boolean reversed) {
        Enumerator<Map.Entry<K, V>> i;
        if (reversed) {
            i = new ReversedKeyEnumeratorSpliterator<>(sequenceRoot, Map.Entry.class::cast, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
        } else {
            i = new KeyEnumeratorSpliterator<>(sequenceRoot, Map.Entry.class::cast, Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
        }
        return new IteratorFacade<>(i, null);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return iterator(false);
    }

    public @NonNull Spliterator<Map.Entry<K, V>> spliterator() {
        return Spliterators.spliterator(iterator(false), size, Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT);
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> put(@NonNull K key, @Nullable V value) {
        return copyPutLast(key, value, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof ChampChampSequencedMap)) {
            return ((ChampChampSequencedMap<K, V>) m).toImmutable();
        }
        return putAll(m.entrySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> putAll(@NonNull ImmutableMap<? extends K, ? extends V> m) {
        if (m == this || isEmpty() && (m instanceof ChampChampImmutableSequencedMap)) {
            return (ChampChampImmutableSequencedMap<K, V>) m;
        }
        return putAll(m.readOnlyEntrySet());
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        ChampChampSequencedMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            ChangeEvent<SequencedEntry<K, V>> details = t.putLast(entry.getKey(), entry.getValue(), false);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> putAll(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ChampChampImmutableSequencedMap<K, V>) ImmutableSequencedMap.super.putAll(map);
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return this;//FIXME implement me
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> remove(@NonNull K key) {
        return copyRemove(key, first, last);
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }
        ChampChampSequencedMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<SequencedEntry<K, V>> details = t.removeAndGiveDetails(key);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @NonNull
    private ChampChampImmutableSequencedMap<K, V> renumber(
            BitmapIndexedNode<SequencedEntry<K, V>> root,
            BitmapIndexedNode<SequencedEntry<K, V>> seqRoot,
            int size, int first, int last) {
        if (ChampChampImmutableSequencedSet.mustRenumber(size, first, last)) {
            IdentityObject mutator = new IdentityObject();
            BitmapIndexedNode<SequencedEntry<K, V>> renumberedRoot = SequencedEntry.renumber(size, root, mutator, Objects::hashCode, Objects::equals);
            BitmapIndexedNode<SequencedEntry<K, V>> renumberedSeqRoot = buildSequenceRoot(renumberedRoot, mutator);
            return new ChampChampImmutableSequencedMap<>(renumberedRoot, renumberedSeqRoot,
                    size, -1, size);
        }
        return new ChampChampImmutableSequencedMap<>(root, seqRoot, size, first, last);
    }

    @Override
    public @NonNull ChampChampImmutableSequencedMap<K, V> retainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        ChampChampSequencedMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : readOnlyKeySet()) {
            if (!c.contains(key)) {
                t.removeAndGiveDetails(key);
                modified = true;
            }
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull ChampChampSequencedMap<K, V> toMutable() {
        return new ChampChampSequencedMap<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyMap.mapToString(this);
    }

    private @NonNull Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(this.toMutable());
    }

    private static class SerializationProxy<K, V> extends MapSerializationProxy<K, V> {
        private final static long serialVersionUID = 0L;

        protected SerializationProxy(Map<K, V> target) {
            super(target);
        }

        @Override
        protected @NonNull Object readResolve() {
            return ChampChampImmutableSequencedMap.of().putAll(deserialized);
        }
    }

    static <K, V> int seqHashCode(SequencedEntry<K, V> e) {
        return seqHash(e.getSequenceNumber());
    }
}
