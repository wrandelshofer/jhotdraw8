/*
 * @(#)SequencedChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.collection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.collection.impl.champ.*;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;
import org.jhotdraw8.collection.serialization.MapSerializationProxy;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Implements an immutable map using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP) and a bit-mapped trie (Vector).
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
 *     <li>put, putFirst, putLast: O(log N) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>remove: O(log N) in an amortized sense, because we sometimes have to renumber the elements.</li>
 *     <li>containsKey: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(log N)</li>
 *     <li>getFirst, getLast: O(log N)</li>
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
 * using method {@link #toMutable()}}. The mutable copy shares its nodes
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
 * Portions of the code in this class has been derived from 'vavr' Vector.java.
 * <p>
 * The design of this class is inspired by 'VectorMap.scala'.
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *      </dd>
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. <a href="https://github.com/usethesource/capsule/blob/3856cd65fa4735c94bcfa94ec9ecf408429b54f4/LICENSE">BSD-2-Clause License</a></dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 *      </dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@SuppressWarnings("exports")
public class VectorMap<K, V> extends ChampBitmapIndexedNode<ChampSequencedEntry<K, V>> implements ImmutableSequencedMap<K, V>, Serializable {
    private static final @NonNull VectorMap<?, ?> EMPTY = new VectorMap<>(
            ChampBitmapIndexedNode.emptyNode(), VectorList.of(), 0, 0);
    @Serial
    private static final long serialVersionUID = 0L;
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

    VectorMap(@NonNull ChampBitmapIndexedNode<ChampSequencedEntry<K, V>> root,
              @NonNull VectorList<Object> vector,
              int size, int offset) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
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
    public static <K, V> @NonNull VectorMap<K, V> copyOf(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> map) {
        return VectorMap.<K, V>of().putAll(map);
    }

    /**
     * Returns an immutable copy of the provided map.
     *
     * @param map a map
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable copy
     */
    public static <K, V> @NonNull VectorMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return VectorMap.<K, V>of().putAll(map);
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> @NonNull VectorMap<K, V> of() {
        return (VectorMap<K, V>) VectorMap.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull VectorMap<K, V> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return find(new ChampSequencedEntry<>(key), Objects.hashCode(key), 0,
                ChampSequencedEntry::keyEquals) != ChampNode.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof VectorMap) {
            VectorMap<?, ?> that = (VectorMap<?, ?>) other;
            return size == that.size && equivalent(that);
        } else {
            return ReadOnlyMap.mapEquals(this, other);
        }
    }

    @SuppressWarnings("unchecked")
    public @Nullable Map.Entry<K, V> firstEntry() {
        return isEmpty() ? null : (Map.Entry<K, V>) vector.getFirst();
    }

    @SuppressWarnings("unchecked")
    public @Nullable Map.Entry<K, V> lastEntry() {
        return isEmpty() ? null : (Map.Entry<K, V>) vector.getLast();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        Object result = find(
                new ChampSequencedEntry<>((K) o),
                Objects.hashCode(o), 0, ChampSequencedEntry::keyEquals);
        return (V) ((result instanceof ChampSequencedEntry<?, ?> entry) ? entry.getValue() : null);
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
        return new IteratorFacade<>(spliterator(), null);
    }

    @Override
    public @NonNull VectorMap<K, V> put(@NonNull K key, @Nullable V value) {
        return putLast(key, value, false);
    }

    @Override
    public @NonNull VectorMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (VectorMap<K, V>) ImmutableSequencedMap.super.putAll(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull VectorMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        if (isEmpty() && (entries instanceof VectorMap<?, ?> that)) {
            return (VectorMap<K, V>) that;
        }
        // XXX if the other set is a VectorSet, we should merge the trees
        // See kotlinx collections:
        // https://github.com/Kotlin/kotlinx.collections.immutable/blob/d7b83a13fed459c032dab1b4665eda20a04c740f/core/commonMain/src/implementations/immutableSet/TrieNode.kt#L338
        var t = this.toMutable();
        boolean modified = false;
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            var details = t.putLast(entry.getKey(), entry.getValue(), false);
            modified |= details.isModified();
        }
        return modified ? t.toImmutable() : this;
    }

    @NonNull
    private VectorMap<K, V> putFirst(@NonNull K key, @Nullable V value, boolean moveToFirst) {
        var details = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
        var newEntry = new ChampSequencedEntry<>(key, value, -offset - 1);
        var newRoot = update(null, newEntry,
                Objects.hashCode(key), 0, details,
                moveToFirst ? ChampSequencedEntry::updateAndMoveToFirst : ChampSequencedEntry::update,
                ChampSequencedEntry::keyEquals, ChampSequencedEntry::keyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            var newVector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return new VectorMap<>(newRoot, newVector, size, offset);
        }
        if (details.isModified()) {
            var newVector = vector;
            int newSize = size;
            IdentityObject mutator = new IdentityObject();
            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = ChampSequencedData.vecRemove(newVector, mutator, details.getOldDataNonNull(), details, offset);
                    newVector = result.first();
                }
            } else {
                newSize++;
            }
            int newOffset = offset + 1;
            newVector = newVector.addFirst(newEntry);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    @Override
    public @NonNull VectorMap<K, V> putFirst(@NonNull K key, @Nullable V value) {
        return putFirst(key, value, true);
    }

    @NonNull
    private VectorMap<K, V> putLast(@NonNull K key, @Nullable V value, boolean moveToLast) {
        var details = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
        var newEntry = new ChampSequencedEntry<>(key, value, vector.size() - offset);
        var newRoot = update(null, newEntry,
                Objects.hashCode(key), 0, details,
                moveToLast ? ChampSequencedEntry::updateAndMoveToLast : ChampSequencedEntry::update,
                ChampSequencedEntry::keyEquals, ChampSequencedEntry::keyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().getSequenceNumber() == details.getNewDataNonNull().getSequenceNumber()) {
            var newVector = vector.set(details.getNewDataNonNull().getSequenceNumber() - offset, details.getNewDataNonNull());
            return new VectorMap<>(newRoot, newVector, size, offset);
        }
        if (details.isModified()) {
            var newVector = vector;
            int newOffset = offset;
            int newSize = size;
            var mutator = new IdentityObject();
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldElem = details.getOldDataNonNull();
                    var result = ChampSequencedData.vecRemove(newVector, mutator, oldElem, details, newOffset);
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
    public @NonNull VectorMap<K, V> putLast(@NonNull K key, @Nullable V value) {
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

    @Override
    public @NonNull VectorMap<K, V> remove(@NonNull K key) {
        int keyHash = Objects.hashCode(key);
        var details = new ChampChangeEvent<ChampSequencedEntry<K, V>>();
        ChampBitmapIndexedNode<ChampSequencedEntry<K, V>> newRoot = remove(null,
                new ChampSequencedEntry<>(key),
                keyHash, 0, details, ChampSequencedEntry::keyEquals);
        if (details.isModified()) {
            var oldElem = details.getOldDataNonNull();
            var result = ChampSequencedData.vecRemove(vector, null, oldElem, details, offset);
            return renumber(newRoot, result.first(), size - 1, result.second());
        }
        return this;
    }


    @Override
    public @NonNull VectorMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        var t = toMutable();
        return t.removeAll(c) ? t.toImmutable() : this;
    }

    @NonNull
    private VectorMap<K, V> renumber(
            ChampBitmapIndexedNode<ChampSequencedEntry<K, V>> root,
            VectorList<Object> vector,
            int size, int offset) {

        if (ChampSequencedData.vecMustRenumber(size, offset, this.vector.size())) {
            var mutator = new IdentityObject();
            var result = ChampSequencedData.<ChampSequencedEntry<K, V>>vecRenumber(
                    size, root, vector, mutator, ChampSequencedEntry::keyHash, ChampSequencedEntry::keyEquals,
                    (e, seq) -> new ChampSequencedEntry<>(e.getKey(), e.getValue(), seq));
            return new VectorMap<>(
                    result.first(), result.second(),
                    size, 0);
        }
        return new VectorMap<>(root, vector, size, offset);
    }

    @Override
    public @NonNull VectorMap<K, V> retainAll(@NonNull Collection<? extends K> c) {
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

    public @NonNull Iterator<Map.Entry<K, V>> reverseIterator() {
        return new IteratorFacade<>(reverseSpliterator(), null);
    }

    @SuppressWarnings("unchecked")
    private @NonNull EnumeratorSpliterator<Map.Entry<K, V>> reverseSpliterator() {
        return new ChampReverseSequencedVectorSpliterator<>(vector,
                e -> ((ChampSequencedEntry<K, V>) e),
                size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }


    @Override
    public int size() {
        return size;
    }

    public @NonNull EnumeratorSpliterator<Map.Entry<K, V>> spliterator() {
        return new ChampSequencedVectorSpliterator<>(vector,
                e -> ((Map.Entry<K, V>) e),
                size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Creates a mutable copy of this map.
     *
     * @return a mutable sequenced CHAMP map
     */
    @Override
    public @NonNull MutableVectorMap<K, V> toMutable() {
        return new MutableVectorMap<>(this);
    }

    @Override
    public @NonNull MutableVectorMap<K, V> asMap() {
        return new MutableVectorMap<>(this);
    }

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
            return VectorMap.of().putAll(deserialized);
        }
    }
}
