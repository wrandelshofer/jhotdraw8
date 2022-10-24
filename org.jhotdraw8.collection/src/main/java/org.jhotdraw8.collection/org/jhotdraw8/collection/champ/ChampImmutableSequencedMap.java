/*
 * @(#)ImmutableSequencedChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.UniqueId;
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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Implements an immutable map using a Compressed Hash-Array Mapped Prefix-tree
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
 *     <li>copyPut, copyPutFirst, copyPutLast: O(1) amortized due to
 *     renumbering</li>
 *     <li>copyRemove: O(1) amortized due to renumbering</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(N)</li>
 *     <li>iterator.next: O(1) with bucket sort, O(log N) with heap sort</li>
 *     <li>getFirst, getLast: O(N)</li>
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
@SuppressWarnings("exports")
public class ChampImmutableSequencedMap<K, V> extends BitmapIndexedNode<SequencedEntry<K, V>> implements ImmutableSequencedMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private static final ChampImmutableSequencedMap<?, ?> EMPTY = new ChampImmutableSequencedMap<>(BitmapIndexedNode.emptyNode(), 0, -1, 0);
    /**
     * Counter for the sequence number of the last entry.
     * The counter is incremented after a new entry is added to the end of the
     * sequence.
     */
    protected transient final int last;
    /**
     * Counter for the sequence number of the first element. The counter is
     * decrement after a new entry has been added to the start of the sequence.
     */
    protected transient final int first;
    final transient int size;

    ChampImmutableSequencedMap(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root, int size,
                               int first, int last) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        assert (long) last - first >= size : "size=" + size + " first=" + first + " last=" + last;
        this.size = size;
        this.first = first;
        this.last = last;
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
    public static <K, V> @NonNull ChampImmutableSequencedMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return ((ChampImmutableSequencedMap<K, V>) ChampImmutableSequencedMap.EMPTY).putAll(map);
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
    public static <K, V> @NonNull ChampImmutableSequencedMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ((ChampImmutableSequencedMap<K, V>) ChampImmutableSequencedMap.EMPTY).putAll(map);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampImmutableSequencedMap<K, V> of() {
        return (ChampImmutableSequencedMap<K, V>) ChampImmutableSequencedMap.EMPTY;
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
    public static <K, V> @NonNull ChampImmutableSequencedMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return (ChampImmutableSequencedMap<K, V>) of().putAll(entries);
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return find(new SequencedEntry<>(key), Objects.hashCode(key), 0,
                getEqualsFunction()) != Node.NO_DATA;
    }

    //@Override
    public @NonNull ChampImmutableSequencedMap<K, V> copyPutFirst(@NonNull K key, @Nullable V value) {
        return copyPutFirst(key, value, true);
    }

    @NonNull
    private ChampImmutableSequencedMap<K, V> copyPutFirst(@NonNull K key, @Nullable V value, boolean moveToFirst) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRootNode = update(null,
                new SequencedEntry<>(key, value, first),
                keyHash, 0, details,
                moveToFirst ? getUpdateAndMoveToFirstFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        if (details.isUpdated()) {
            return moveToFirst
                    ? renumber(newRootNode, size,
                    details.getData().getSequenceNumber() == first ? first : first - 1,
                    details.getData().getSequenceNumber() == last ? last - 1 : last)
                    : new ChampImmutableSequencedMap<>(newRootNode, size, first - 1, last);
        }
        return details.isModified() ? renumber(newRootNode, size + 1, first - 1, last) : this;
    }

    public @NonNull ChampImmutableSequencedMap<K, V> copyPutLast(@NonNull K key, @Nullable V value) {
        return copyPutLast(key, value, true);
    }

    @NonNull
    private ChampImmutableSequencedMap<K, V> copyPutLast(@NonNull K key, @Nullable V value, boolean moveToLast) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRootNode = update(null,
                new SequencedEntry<>(key, value, last),
                keyHash, 0, details,
                moveToLast ? getUpdateAndMoveToLastFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        if (details.isUpdated()) {
            return moveToLast
                    ? renumber(newRootNode, size,
                    details.getData().getSequenceNumber() == first ? first + 1 : first,
                    details.getData().getSequenceNumber() == last ? last : last + 1)
                    : new ChampImmutableSequencedMap<>(newRootNode, size, first, last + 1);
        }
        return details.isModified() ? renumber(newRootNode, size + 1, first, last + 1) : this;
    }

    private @NonNull ChampImmutableSequencedMap<K, V> copyRemove(@NonNull K key, int newFirst, int newLast) {
        int keyHash = Objects.hashCode(key);
        ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRootNode =
                remove(null, new SequencedEntry<>(key), keyHash, 0, details, getEqualsFunction());
        if (details.isModified()) {
            int seq = details.getData().getSequenceNumber();
            if (seq == newFirst) {
                newFirst++;
            }
            if (seq == newLast) {
                newLast--;
            }
            return renumber(newRootNode, size - 1, newFirst, newLast);
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
        if (other instanceof ChampImmutableSequencedMap) {
            ChampImmutableSequencedMap<?, ?> that = (ChampImmutableSequencedMap<?, ?>) other;
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

    public @NonNull Iterator<Map.Entry<K, V>> iterator(boolean reversed) {
        @SuppressWarnings("unchecked")// Java 17 requires this suppression
        Function<SequencedEntry<K, V>, Map.Entry<K, V>> castEntry = Map.Entry.class::<K, V>cast;
        return BucketSequencedIterator.isSupported(size, first, last)
                ? new BucketSequencedIterator<SequencedEntry<K, V>, Map.Entry<K, V>>(
                size, first, last, this, reversed,
                null, castEntry)
                : new HeapSequencedIterator<SequencedEntry<K, V>, Map.Entry<K, V>>(
                size, this, reversed,
                null, castEntry);
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return iterator(false);
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> put(@NonNull K key, @Nullable V value) {
        return copyPutLast(key, value, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof ChampSequencedMap)) {
            return ((ChampSequencedMap<K, V>) m).toImmutable();
        }
        return putAll(m.entrySet());
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> putAll(@NonNull ImmutableMap<? extends K, ? extends V> m) {
        if (m == this) {
            return this;
        }
        return putAll(m.readOnlyEntrySet());
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        ChampSequencedMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            ChangeEvent<SequencedEntry<K, V>> details = t.putLast(entry.getKey(), entry.getValue(), false);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> putAll(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ChampImmutableSequencedMap<K, V>) ImmutableSequencedMap.super.putAll(map);
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return this;//FIXME implement me
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> remove(@NonNull K key) {
        return copyRemove(key, first, last);
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }
        ChampSequencedMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<SequencedEntry<K, V>> details = t.removeAndGiveDetails(key);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @NonNull
    private ChampImmutableSequencedMap<K, V> renumber(BitmapIndexedNode<SequencedEntry<K, V>> root, int size, int first, int last) {
        if (SequencedData.mustRenumber(size, first, last)) {
            root = SequencedEntry.renumber(size, root, new UniqueId(), Objects::hashCode, Objects::equals);
            return new ChampImmutableSequencedMap<>(root, size, -1, size);
        }
        return new ChampImmutableSequencedMap<>(root, size, first, last);
    }

    @Override
    public @NonNull ChampImmutableSequencedMap<K, V> retainAll(@NonNull Collection<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        if (c.isEmpty()) {
            return of();
        }
        ChampSequencedMap<K, V> t = this.toMutable();
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
    public @NonNull ChampSequencedMap<K, V> toMutable() {
        return new ChampSequencedMap<>(this);
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
            return ChampImmutableSequencedMap.of().putAll(deserialized);
        }
    }
}
