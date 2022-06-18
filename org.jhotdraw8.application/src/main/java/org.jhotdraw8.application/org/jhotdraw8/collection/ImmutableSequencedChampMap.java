/*
 * @(#)ImmutableSeqChampMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.champ.BucketSequencedIterator;
import org.jhotdraw8.collection.champ.ChangeEvent;
import org.jhotdraw8.collection.champ.HeapSequencedIterator;
import org.jhotdraw8.collection.champ.Node;
import org.jhotdraw8.collection.champ.Sequenced;
import org.jhotdraw8.collection.champ.SequencedEntry;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
 *     <li>copyPut, copyPutFirst, copyPutLast: O(1) amortized due to
 *     renumbering</li>
 *     <li>copyRemove: O(1) amortized due to renumbering</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + a cost distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator.next(): O(log N)</li>
 *     <li>getFirst, getLast: O(N)</li>
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
@SuppressWarnings("exports")
public class ImmutableSequencedChampMap<K, V> extends BitmapIndexedNode<SequencedEntry<K, V>> implements ImmutableSequencedMap<K, V>, Serializable {
    private final static long serialVersionUID = 0L;
    private final static int ENTRY_LENGTH = 3;
    private static final ImmutableSequencedChampMap<?, ?> EMPTY = new ImmutableSequencedChampMap<>(BitmapIndexedNode.emptyNode(), 0, -1, 0);
    final transient int size;
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

    ImmutableSequencedChampMap(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root, int size,
                               int first, int last) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        assert (long) last - first >= size : "size=" + size + " first=" + first + " last=" + last;
        this.size = size;
        this.first = first;
        this.last = last;
    }

    @Override
    public @NonNull ReadOnlySequencedMap<K, V> readOnlyReversed() {
        return this;//FIXME implement me
    }

    @NonNull
    private ImmutableSequencedChampMap<K, V> renumber(BitmapIndexedNode<SequencedEntry<K, V>> root, int size, int first, int last) {
        if (size == 0) {
            return of();
        }
        if (Sequenced.mustRenumber(size, first, last)) {
            root = SequencedEntry.renumber(size, root, new UniqueId(), Objects::hashCode, Objects::equals);
            return new ImmutableSequencedChampMap<>(root, size, -1, size);
        }
        return new ImmutableSequencedChampMap<>(root, size, first, last);
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
    public static <K, V> @NonNull ImmutableSequencedChampMap<K, V> copyOf(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
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
    public static <K, V> @NonNull ImmutableSequencedChampMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ((ImmutableSequencedChampMap<K, V>) ImmutableSequencedChampMap.EMPTY).copyPutAll(map);
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
    public static <K, V> @NonNull ImmutableSequencedChampMap<K, V> ofEntries(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        return (ImmutableSequencedChampMap<K, V>) of().copyPutAll(entries);
    }

    @Override
    public boolean containsKey(final @Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return findByKey(new SequencedEntry<>(key), Objects.hashCode(key), 0,
                getEqualsFunction()) != Node.NO_VALUE;
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyClear() {
        return isEmpty() ? this : of();
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyPut(@NonNull K key, @Nullable V value) {
        return copyPutLast(key, value, false);
    }

    //@Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyPutFirst(@NonNull K key, @Nullable V value) {
        return copyPutFirst(key, value, true);
    }

    public @NonNull ImmutableSequencedChampMap<K, V> copyPutLast(@NonNull K key, @Nullable V value) {
        return copyPutLast(key, value, true);
    }

    @NonNull
    private ImmutableSequencedChampMap<K, V> copyPutLast(@NonNull K key, @Nullable V value, boolean moveToLast) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRootNode = update(null,
                new SequencedEntry<>(key, value, last),
                keyHash, 0, details,
                moveToLast ? getUpdateAndMoveToLastFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        if (details.updated) {
            return moveToLast
                    ? renumber(newRootNode, size,
                    details.getOldValue().getSequenceNumber() == first ? first + 1 : first,
                    details.getOldValue().getSequenceNumber() == last ? last : last + 1)
                    : new ImmutableSequencedChampMap<>(newRootNode, size, first, last);
        }
        return details.modified ? renumber(newRootNode, size + 1, first, last + 1) : this;
    }

    @NonNull
    private ImmutableSequencedChampMap<K, V> copyPutFirst(@NonNull K key, @Nullable V value, boolean moveToFirst) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRootNode = update(null,
                new SequencedEntry<>(key, value, first),
                keyHash, 0, details,
                moveToFirst ? getUpdateAndMoveToFirstFunction() : getUpdateFunction(),
                getEqualsFunction(), getHashFunction());
        if (details.updated) {
            return moveToFirst
                    ? renumber(newRootNode, size,
                    details.getOldValue().getSequenceNumber() == first ? first : first - 1,
                    details.getOldValue().getSequenceNumber() == last ? last - 1 : last)
                    : new ImmutableSequencedChampMap<>(newRootNode, size, first, last);
        }
        return details.modified ? renumber(newRootNode, size + 1, first - 1, last) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyPutAll(@NonNull Map<? extends K, ? extends V> m) {
        if (isEmpty() && (m instanceof SequencedChampMap)) {
            return ((SequencedChampMap<K, V>) m).toImmutable();
        }
        return copyPutAll(m.entrySet());
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyPutAll(@NonNull ImmutableMap<? extends K, ? extends V> m) {
        if (m == this) {
            return this;
        }
        return copyPutAll(m.readOnlyEntrySet());
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyPutAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        final SequencedChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            ChangeEvent<SequencedEntry<K, V>> details = t.putLast(entry.getKey(), entry.getValue(), false);
            modified |= details.modified;
        }
        return modified ? t.toImmutable() : this;
    }

    @Override
    public @NonNull ImmutableSequencedChampMap<K, V> copyRemove(@NonNull K key) {
        return copyRemove(key, first, last);
    }

    private @NonNull ImmutableSequencedChampMap<K, V> copyRemove(@NonNull K key, int newFirst, int newLast) {
        final int keyHash = Objects.hashCode(key);
        final ChangeEvent<SequencedEntry<K, V>> details = new ChangeEvent<>();
        final BitmapIndexedNode<SequencedEntry<K, V>> newRootNode =
                remove(null, new SequencedEntry<>(key), keyHash, 0, details, getEqualsFunction());
        if (details.isModified()) {
            int seq = details.getOldValue().getSequenceNumber();
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
    public @NonNull ImmutableSequencedChampMap<K, V> copyRemoveAll(@NonNull Iterable<? extends K> c) {
        if (this.isEmpty()) {
            return this;
        }
        final SequencedChampMap<K, V> t = this.toMutable();
        boolean modified = false;
        for (K key : c) {
            ChangeEvent<SequencedEntry<K, V>> details = t.removeAndGiveDetails(key);
            modified |= details.modified;
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

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return entries(false);
    }

    public @NonNull Iterator<Map.Entry<K, V>> entries(boolean reversed) {
        return BucketSequencedIterator.isSupported(size, first, last)
                ? new BucketSequencedIterator<SequencedEntry<K, V>, Map.Entry<K, V>>(size, first, last, this, reversed,
                null, Map.Entry.class::cast)
                : new HeapSequencedIterator<SequencedEntry<K, V>, Map.Entry<K, V>>(size, this, reversed,
                null, Map.Entry.class::cast);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
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
            return this.equivalent(that);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(final Object o) {
        Object result = findByKey(
                new SequencedEntry<>((K) o),
                Objects.hashCode(o), 0, getEqualsFunction());
        return (result instanceof SequencedEntry<?, ?>) ? ((SequencedEntry<K, V>) result).getValue() : null;

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
    public int size() {
        return size;
    }

    @Override
    public @NonNull SequencedChampMap<K, V> toMutable() {
        return new SequencedChampMap<>(this);
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
            return ImmutableSequencedChampMap.of().copyPutAll(deserialized);
        }
    }

    @NonNull
    private ToIntFunction<SequencedEntry<K, V>> getHashFunction() {
        return (a) -> Objects.hashCode(a.getKey());
    }

    @NonNull
    private BiPredicate<SequencedEntry<K, V>, SequencedEntry<K, V>> getEqualsFunction() {
        return (a, b) -> Objects.equals(a.getKey(), b.getKey());
    }

    @NonNull
    private BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> getUpdateFunction() {
        return (oldK, newK) -> Objects.equals(oldK.getValue(), newK.getValue()) ? oldK :
                new SequencedEntry<>(oldK.getKey(), newK.getValue(), oldK.getSequenceNumber());
    }

    @NonNull
    private BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> getUpdateAndMoveToLastFunction() {
        return (oldK, newK) -> (Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() - 1) ? oldK : newK;
    }

    @NonNull
    private BiFunction<SequencedEntry<K, V>, SequencedEntry<K, V>, SequencedEntry<K, V>> getUpdateAndMoveToFirstFunction() {
        return (oldK, newK) -> (Objects.equals(oldK.getValue(), newK.getValue())
                && oldK.getSequenceNumber() == newK.getSequenceNumber() + 1) ? oldK : newK;
    }
}
