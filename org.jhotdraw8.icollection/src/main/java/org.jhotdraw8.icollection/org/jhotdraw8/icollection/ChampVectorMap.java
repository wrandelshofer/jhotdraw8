/*
 * @(#)SequencedChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.impl.champ.ReverseTombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.champ.SequencedData;
import org.jhotdraw8.icollection.impl.champ.SequencedEntry;
import org.jhotdraw8.icollection.impl.champ.TombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.icollection.serialization.MapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Implements the {@link ImmutableSequencedMap} interface using a Compressed
 * Hash-Array Mapped Prefix-tree (CHAMP) and a bit-mapped trie (Vector).
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
 *     <li>put, putFirst, putLast: O(log₃₂ N) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>remove: O(log₃₂ N) in an amortized sense, because we sometimes have to renumber the elements.</li>
 *     <li>containsKey: O(log₃₂ N)</li>
 *     <li>toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(log₃₂ N)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst, getLast: O(log₃₂ N)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This map performs read and write operations of single elements in O(log N) time,
 * and in O(log N) space, where N is the number of elements in the set.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other maps.
 * <p>
 * If a write operation is performed on a node, then this set creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1).
 * <p>
 * This map can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}. The mutable copy shares its nodes
 * with this map, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * Insertion Order:
 * <p>
 * This map uses a counter to keep track of the insertion order.
 * It stores the current value of the counter in the sequence number
 * field of each data entry. If the counter wraps around, it must renumber all
 * sequence numbers.
 * <p>
 * The renumbering is why the {@code add} and {@code remove} methods are O(1)
 * only in an amortized sense.
 * <p>
 * To support iteration, we use a Vector. The Vector has the same contents
 * as the CHAMP trie. However, its elements are stored in insertion order.
 * <p>
 * If an element is removed from the CHAMP trie that is not the first or the
 * last element of the Vector, we replace its corresponding element in
 * the Vector by a tombstone. If the element is at the start or end of the Vector,
 * we remove the element and all its neighboring tombstones from the Vector.
 * <p>
 * A tombstone can store the number of neighboring tombstones in ascending and in descending
 * direction. We use these numbers to skip tombstones when we iterate over the vector.
 * Since we only allow iteration in ascending or descending order from one of the ends of
 * the vector, we do not need to keep the number of neighbors in all tombstones up to date.
 * It is sufficient, if we update the neighbor with the lowest index and the one with the
 * highest index.
 * <p>
 * If the number of tombstones exceeds half of the size of the collection, we renumber all
 * sequence numbers, and we create a new Vector.
 * <p>
 * The immutable version of this set extends from the non-public class
 * {@code ChampBitmapIndexNode}. This design safes 16 bytes for every instance,
 * and reduces the number of redirections for finding an element in the
 * collection by 1.
 * <p>
 * References:
 * <p>
 * For a similar design, see 'SimpleImmutableSequencedMap.scala'. Note, that this code is not a derivative
 * of that code.
 * <dl>
 *     <dt>The Scala library. SimpleImmutableSequencedMap.scala. Copyright EPFL and Lightbend, Inc. Apache License 2.0.</dt>
 *     <dd><a href="https://github.com/scala/scala/blob/28eef15f3cc46f6d3dd1884e94329d7601dc20ee/src/library/scala/collection/immutable/VectorMap.scala">github.com</a>
 *     </dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class ChampVectorMap<K, V> implements ImmutableSequencedMap<K, V>, Serializable {
    private static final @NonNull ChampVectorMap<?, ?> EMPTY = new ChampVectorMap<>(
            BitmapIndexedNode.emptyNode(), VectorList.of(), 0, 0);
    @Serial
    private static final long serialVersionUID = 0L;
    final transient @NonNull BitmapIndexedNode<SequencedEntry<K, V>> root;
    /**
     * Offset of sequence numbers to vector indices.
     *
     * <pre>vector index = sequence number + offset</pre>
     */
    final int offset;
    /**
     * The size of the map.
     */
    final int size;
    /**
     * In this vector we store the elements in the order in which they were inserted.
     */
    final @NonNull VectorList<Object> vector;

    private record OpaqueRecord<K, V>(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root,
                                      @NonNull VectorList<Object> vector,
                                      int size, int offset) {
    }

    /**
     * Creates a new instance with the provided privateData data object.
     * <p>
     * This constructor is intended to be called from a constructor
     * of the subclass, that is called from method {@link #newInstance(PrivateData)}.
     *
     * @param privateData an privateData data object
     */
    @SuppressWarnings("unchecked")
    protected ChampVectorMap(@NonNull PrivateData privateData) {
        this(((OpaqueRecord<K, V>) privateData.get()).root,
                ((OpaqueRecord<K, V>) privateData.get()).vector,
                ((OpaqueRecord<K, V>) privateData.get()).size,
                ((OpaqueRecord<K, V>) privateData.get()).offset);
    }

    /**
     * Creates a new instance with the provided privateData object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param privateData the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
    protected @NonNull ChampVectorMap<K, V> newInstance(@NonNull PrivateData privateData) {
        return new ChampVectorMap<>(privateData);
    }

    private @NonNull ChampVectorMap<K, V> newInstance(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root,
                                                      @NonNull VectorList<Object> vector,
                                                      int size, int offset) {
        return new ChampVectorMap<>(new PrivateData(new OpaqueRecord<>(root, vector, size, offset)));
    }

    ChampVectorMap(@NonNull BitmapIndexedNode<SequencedEntry<K, V>> root,
                   @NonNull VectorList<Object> vector,
                   int size, int offset) {
        this.root = root;
        this.size = size;
        this.offset = offset;
        this.vector = Objects.requireNonNull(vector);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    public static <K, V> @NonNull ChampVectorMap<K, V> copyOf(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> map) {
        return ChampVectorMap.<K, V>of().putAll(map);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    public static <K, V> @NonNull ChampVectorMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return ChampVectorMap.<K, V>of().putAll(map);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull ChampVectorMap<K, V> of() {
        return (ChampVectorMap<K, V>) ChampVectorMap.EMPTY;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull ChampVectorMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.find(new SequencedEntry<>(key), SequencedEntry.keyHash(key), 0,
                SequencedEntry::keyEquals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ChampVectorMap<?, ?> that) {
            return size == that.size && root.equivalent(that.root);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @SuppressWarnings("unchecked")
    public Map.@Nullable Entry<K, V> firstEntry() {
        return isEmpty() ? null : (Map.Entry<K, V>) vector.getFirst();
    }

    @SuppressWarnings("unchecked")
    public Map.@Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : (Map.Entry<K, V>) vector.getLast();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable V get(Object o) {
        Object result = root.find(
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
        return Spliterators.iterator(spliterator());
    }

    @Override
    public int maxSize() {
        return 1 << 30;
    }

    @Override
    public @NonNull ChampVectorMap<K, V> put(@NonNull K key, @Nullable V value) {
        return putLast(key, value, false);
    }

    @Override
    public @NonNull ChampVectorMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (ChampVectorMap<K, V>) ImmutableSequencedMap.super.putAll(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ChampVectorMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        var m = toMutable();
        return m.putAll(c) ? m.toImmutable() : this;
    }

    @Override
    public @NonNull ChampVectorMap<K, V> putFirst(@NonNull K key, @Nullable V value) {
        return putFirst(key, value, true);
    }

    @NonNull
    private ChampVectorMap<K, V> putFirst(@NonNull K key, @Nullable V value, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, -offset - 1);
        var newRoot = root.put(null, newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToFirst ? SequencedEntry::updateAndMoveToFirst : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            // If we have replaced the entry in the tree, but the sequence number is still the same.
            // Then we replace the entry in the vector.
            var newVector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return newInstance(newRoot, newVector, size, offset);
        }
        if (details.isModified()) {
            var newVector = vector;
            int newSize = size;
            if (details.isReplaced()) {
                // If we have replaced the entry in the tree, but the sequence number has changed.
                // Then we remove the old entry from the vector (this may result in a new tombstone in the vector)
                if (moveToFirst) {
                    var result = SequencedData.vecRemove(newVector, details.getOldDataNonNull(), offset);
                    newVector = result.first();
                }
            } else {
                // If we have inserted the entry in the tree.
                // Then we increase the size.
                newSize++;
            }
            // We insert the new entry at the start of the vector.
            int newOffset = offset + 1;
            newVector = newVector.addFirst(newEntry);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    @NonNull
    private ChampVectorMap<K, V> putLast(@NonNull K key, @Nullable V value, boolean moveToLast) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, vector.size() - offset);
        var newRoot = root.put(null, newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToLast ? SequencedEntry::updateAndMoveToLast : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            var newVector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return newInstance(newRoot, newVector, size, offset);
        }
        if (details.isModified()) {
            var newVector = vector;
            int newOffset = offset;
            int newSize = size;
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldElem = details.getOldDataNonNull();
                    var result = SequencedData.vecRemove(newVector, oldElem, newOffset);
                    newVector = result.first();
                    newOffset = result.second();
                }
            } else {
                newSize++;
            }
            newVector = newVector.addLast(newEntry);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    @Override
    public @NonNull ChampVectorMap<K, V> putLast(@NonNull K key, @Nullable V value) {
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
                this::firstEntry,
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED, null);
    }

    @Override
    public @NonNull ChampVectorMap<K, V> remove(@NonNull K key) {
        int keyHash = SequencedEntry.keyHash(key);
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRoot = root.remove(null,
                new SequencedEntry<>(key),
                keyHash, 0, details, SequencedEntry::keyEquals);
        if (details.isModified()) {
            var oldElem = details.getOldDataNonNull();
            var result = SequencedData.vecRemove(vector, oldElem, offset);
            return size == 1 ? ChampVectorMap.of() : renumber(newRoot, result.first(), size - 1, result.second());
        }
        return this;
    }


    @Override
    public @NonNull ChampVectorMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        var t = toMutable();
        return t.removeAll(c) ? t.toImmutable() : this;
    }

    @NonNull
    private ChampVectorMap<K, V> renumber(
            BitmapIndexedNode<SequencedEntry<K, V>> root,
            VectorList<Object> vector,
            int size, int offset) {

        if (SequencedData.vecMustRenumber(size, offset, this.vector.size())) {
            var owner = new IdentityObject();
            var result = SequencedData.vecRenumber(
                    owner, size, vector.size(), root, vector.trie, SequencedEntry::entryKeyHash, SequencedEntry::keyEquals,
                    (e, seq) -> new SequencedEntry<>(e.getKey(), e.getValue(), seq));
            return newInstance(
                    result.first(), result.second(),
                    size, 0);
        }
        return newInstance(root, vector, size, offset);
    }

    @Override
    public @NonNull ChampVectorMap<K, V> retainAll(@NonNull Iterable<? extends K> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toImmutable() : this;
    }

    @NonNull Iterator<Map.Entry<K, V>> reverseIterator() {
        return Spliterators.iterator(reverseSpliterator());
    }

    @SuppressWarnings("unchecked")
    @NonNull Spliterator<Map.Entry<K, V>> reverseSpliterator() {
        return new ReverseTombSkippingVectorSpliterator<>(vector,
                e -> ((SequencedEntry<K, V>) e),
                size(), Spliterator.NONNULL | characteristics());
    }


    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public @NonNull Spliterator<Map.Entry<K, V>> spliterator() {
        return new TombSkippingVectorSpliterator<>(vector.trie,
                e -> ((Map.Entry<K, V>) e),
                0, size(), vector.size(),
                Spliterator.NONNULL | characteristics());
    }

    /**
     * Creates a mutable copy of this map.
     *
     * @return a mutable sequenced CHAMP map
     */
    @Override
    public @NonNull MutableChampVectorMap<K, V> toMutable() {
        return new MutableChampVectorMap<>(this);
    }

    @Override
    public @NonNull MutableChampVectorMap<K, V> asMap() {
        return new MutableChampVectorMap<>(this);
    }

    /**
     * Returns a string representation of this map.
     * <p>
     * The string representation is consistent with the one produced
     * by {@link AbstractMap#toString()}.
     *
     * @return a string representation
     */
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
            return ChampVectorMap.of().putAll(deserializedEntries);
        }
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED;
    }

}
