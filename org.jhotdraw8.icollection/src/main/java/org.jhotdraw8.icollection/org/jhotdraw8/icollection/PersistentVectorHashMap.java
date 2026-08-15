/*
 * @(#)SequencedChampMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadableSequencedMapFacade;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.impl.champ.ReverseTombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.champ.SequencedData;
import org.jhotdraw8.icollection.impl.champ.SequencedEntry;
import org.jhotdraw8.icollection.impl.champ.TombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.fingertree.FingerTree;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeSpliterator;
import org.jhotdraw8.icollection.persistent.PersistentSequencedMap;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jhotdraw8.icollection.serialization.MapSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;

/// Implements the [PersistentSequencedMap] interface using a Compressed
/// Hash-Array Mapped Prefix-tree (CHAMP) and a bit-mapped trie (Vector).
///
/// Features:
///
///   - supports up to 2<sup>30</sup> entries
///   - allows null keys and null values
///   - is persistent
///   - is thread-safe
///   - iterates in the order, in which keys were inserted
///
///
/// Performance characteristics:
///
///   - put, putFirst, putLast: O(log₃₂ N) in an amortized sense, because we sometimes have to
///     renumber the elements.
///   - remove: O(log₃₂ N) in an amortized sense, because we sometimes have to renumber the elements.
///   - containsKey: O(log₃₂ N)
///   - toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in
///     the mutable copy
///   - clone: O(1)
///   - iterator creation: O(log₃₂ N)
///   - iterator.next: O(1)
///   - getFirst, getLast: O(log₃₂ N)
///
///
/// Implementation details:
///
/// This map performs read and write operations of single elements in O(log N) time,
/// and in O(log N) space, where N is the number of elements in the set.
///
/// The CHAMP trie contains nodes that may be shared with other maps.
///
/// If a write operation is performed on a node, then this set creates a
/// copy of the node and of all parent nodes up to the root (copy-path-on-write).
/// Since the CHAMP trie has a fixed maximal height, the cost is O(1).
///
/// This map can create a mutable copy of itself in O(1) time and O(1) space
/// using method [#toMutable()]. The mutable copy shares its nodes
/// with this map, until it has gradually replaced the nodes with exclusively
/// owned nodes.
///
/// Insertion Order:
///
/// This map uses a counter to keep track of the insertion order.
/// It stores the current value of the counter in the sequence number
/// field of each data entry. If the counter wraps around, it must renumber all
/// sequence numbers.
///
/// The renumbering is why the `add` and `remove` methods are O(1)
/// only in an amortized sense.
///
/// To support iteration, we use a Vector. The Vector has the same contents
/// as the CHAMP trie. However, its elements are stored in insertion order.
///
/// If an element is removed from the CHAMP trie that is not the tree or the
/// last element of the Vector, we replace its corresponding element in
/// the Vector by a tombstone. If the element is at the start or end of the Vector,
/// we remove the element and all its neighboring tombstones from the Vector.
///
/// A tombstone can store the number of neighboring tombstones in ascending and in descending
/// direction. We use these numbers to neighbors tombstones when we iterate over the vector.
/// Since we only allow iteration in ascending or descending order from one of the ends of
/// the vector, we do not need to keep the number of neighbors in all tombstones up to date.
/// It is sufficient, if we update the neighbor with the lowest offset and the one with the
/// highest offset.
///
/// If the number of tombstones exceeds half of the size of the collection, we renumber all
/// sequence numbers, and we create a new Vector.
///
/// References:
///
/// For a similar design, see 'SimplePersistentSequencedMap.scala'. Note, that this code is not a derivative
/// of that code.
/// <dl>
///     <dt>The Scala library. SimplePersistentSequencedMap.scala. Copyright EPFL and Lightbend, Inc. Apache License 2.0.</dt>
///     <dd><a href="https://github.com/scala/scala/blob/28eef15f3cc46f6d3dd1884e94329d7601dc20ee/src/library/scala/collection/persistent/VectorMap.scala">github.com</a>
///     </dd>
/// </dl>
///
/// @param <K> the key type
/// @param <V> the value type
@SuppressWarnings("exports")
public class PersistentVectorHashMap<K, V> implements PersistentSequencedMap<K, V>, Serializable {
    private static final PersistentVectorHashMap<?, ?> EMPTY = new PersistentVectorHashMap<>(
            BitmapIndexedNode.emptyNode(), FingerTreeAPI.of(), 0, 0);
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient BitmapIndexedNode<SequencedEntry<K, V>> hashMap;
    /// Offset of sequence numbers to vector indices.
    /// <pre>vector offset = sequence number + offset</pre>
    final int offset;
    /// The size of the map.
    final int size;
    /// In this vector we store the elements in the order in which they were inserted.
    final FingerTree<Object> vector;

    record OpaqueRecord<K, V>(BitmapIndexedNode<SequencedEntry<K, V>> root,
                              FingerTree<Object> vector,
                              int size, int offset) {
    }

    /// Creates a new instance with the provided privateData data object.
    ///
    /// This constructor is intended to be called from a constructor
    /// of the subclass, that is called from method [#newInstance(PrivateData)].
    ///
    /// @param privateData an privateData data object
    @SuppressWarnings("unchecked")
    protected PersistentVectorHashMap(PrivateData privateData) {
        this(((OpaqueRecord<K, V>) privateData.get()).root,
                ((OpaqueRecord<K, V>) privateData.get()).vector,
                ((OpaqueRecord<K, V>) privateData.get()).size,
                ((OpaqueRecord<K, V>) privateData.get()).offset);
    }

    /// Creates a new instance with the provided privateData object as its internal data structure.
    ///
    /// Subclasses must override this method, and return a new instance of their subclass!
    ///
    /// @param privateData the internal data structure needed by this class for creating the instance.
    /// @return a new instance of the subclass
    protected PersistentVectorHashMap<K, V> newInstance(PrivateData privateData) {
        return new PersistentVectorHashMap<>(privateData);
    }

    private PersistentVectorHashMap<K, V> newInstance(BitmapIndexedNode<SequencedEntry<K, V>> root,
                                                      FingerTree<Object> vector,
                                                      int size, int offset) {
        return new PersistentVectorHashMap<>(new PrivateData(new OpaqueRecord<>(root, vector, size, offset)));
    }

    PersistentVectorHashMap(BitmapIndexedNode<SequencedEntry<K, V>> hashMap,
                            FingerTree<Object> vector,
                            int size, int offset) {
        this.hashMap = hashMap;
        this.size = size;
        this.offset = offset;
        this.vector = Objects.requireNonNull(vector);
    }

    /// Returns an persistent copy of the provided map.
    ///
    /// @param map a map
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return an persistent copy
    public static <K, V> PersistentVectorHashMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> map) {
        return new PersistentVectorHashMapBuilder<K, V>().addEntries(map).build();
    }

    /// Returns an persistent copy of the provided map.
    ///
    /// @param map a map
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return an persistent copy
    public static <K, V> PersistentVectorHashMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        return new PersistentVectorHashMapBuilder<K, V>().addMap(map).build();
    }

    /// Returns an empty persistent map.
    ///
    /// @param <K> the key type
    /// @param <V> the value type
    /// @return an empty persistent map
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentVectorHashMap<K, V> of() {
        return (PersistentVectorHashMap<K, V>) PersistentVectorHashMap.EMPTY;
    }


    /// {@inheritDoc}
    @Override
    public PersistentVectorHashMap<K, V> cleared() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        @SuppressWarnings("unchecked") K key = (K) o;
        return hashMap.find(new SequencedEntry<>(key), SequencedEntry.keyHash(key), 0,
                SequencedEntry::keyEquals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof PersistentVectorHashMap<?, ?> that) {
            return size == that.size && hashMap.equivalent(that.hashMap);
        } else {
            return ReadableMap.mapEquals(this, other);
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
        Object result = hashMap.find(
                new SequencedEntry<>((K) o),
                SequencedEntry.keyHash(o), 0, SequencedEntry::keyEquals);
        return (V) ((result instanceof SequencedEntry<?, ?> entry) ? entry.getValue() : null);
    }

    @Override
    public int hashCode() {
        return ReadableMap.iteratorToHashCode(iterator());
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Map.Entry<K, V>> iterator() {
        if (vector.size() == size) {
            // No skipping iterator needed, because we have no tombstones.
            return FingerTreeAPI.iterator((FingerTree<Map.Entry<K, V>>) (FingerTree<?>) vector);
        }
        return Spliterators.iterator(spliterator());
    }

    @Override
    public int maxSize() {
        return 1 << 30;
    }

    @Override
    public PersistentVectorHashMap<K, V> putting(K key, @Nullable V value) {
        return putLast(key, value, false);
    }

    @Override
    public PersistentVectorHashMap<K, V> puttingAll(Map<? extends K, ? extends V> m) {
        return (PersistentVectorHashMap<K, V>) PersistentSequencedMap.super.puttingAll(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentVectorHashMap<K, V> puttingAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        var m = toMutable();
        return m.putAll(c) ? m.toPersistent() : this;
    }

    @Override
    public PersistentVectorHashMap<K, V> puttingFirst(K key, @Nullable V value) {
        return putFirst(key, value, true);
    }

    private PersistentVectorHashMap<K, V> putFirst(K key, @Nullable V value, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, -offset - 1);
        var newRoot = hashMap.put(null, newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToFirst ? SequencedEntry::updateAndMoveToFirst : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().sequenceNumber() == details.getNewDataNonNull().sequenceNumber()) {
            // If we have replaced the entry in the tree, but the sequence number is still the same.
            // Then we replace the entry in the vector.
            var newVector = FingerTreeAPI.setAt(vector, details.getNewDataNonNull().sequenceNumber() - offset, details.getNewDataNonNull());
            return newInstance(newRoot, newVector.tree(), size, offset);
        }
        if (details.isModified()) {
            var newVector = vector;
            int newSize = size;
            if (details.isReplaced()) {
                // If we have replaced the entry in the tree, but the sequence number has changed.
                // Then we remove the old entry from the vector (this may result in a new tombstone in the vector)
                if (moveToFirst) {
                    var result = SequencedData.vecRemove(newVector, details.getOldDataNonNull(), offset);
                    newVector = result.tree();
                }
            } else {
                // If we have inserted the entry in the tree.
                // Then we increase the size.
                newSize++;
            }
            // We insert the new entry at the start of the vector.
            int newOffset = offset + 1;
            newVector = FingerTreeAPI.addFirst(newEntry, newVector);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    private PersistentVectorHashMap<K, V> putLast(K key, @Nullable V value, boolean moveToLast) {
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        var newEntry = new SequencedEntry<>(key, value, vector.size() - offset);
        var newRoot = hashMap.put(null, newEntry,
                SequencedEntry.keyHash(key), 0, details,
                moveToLast ? SequencedEntry::updateAndMoveToLast : SequencedEntry::update,
                SequencedEntry::keyEquals, SequencedEntry::entryKeyHash);
        if (details.isReplaced()
                && details.getOldDataNonNull().sequenceNumber() == details.getNewDataNonNull().sequenceNumber()) {
            var newVector = FingerTreeAPI.setAt(vector, details.getNewDataNonNull().sequenceNumber() - offset, details.getNewDataNonNull());
            return newInstance(newRoot, newVector.tree(), size, offset);
        }
        if (details.isModified()) {
            var newVector = vector;
            int newOffset = offset;
            int newSize = size;
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldElem = details.getOldDataNonNull();
                    var result = SequencedData.vecRemove(newVector, oldElem, newOffset);
                    newVector = result.tree();
                    newOffset = result.offset();
                }
            } else {
                newSize++;
            }
            newVector = FingerTreeAPI.addLast(newVector, newEntry);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    @Override
    public PersistentVectorHashMap<K, V> puttingLast(K key, @Nullable V value) {
        return putLast(key, value, true);
    }

    @Override
    public ReadableSequencedMap<K, V> readableReversed() {
        return new ReadableSequencedMapFacade<>(
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
    public PersistentVectorHashMap<K, V> removing(K key) {
        int keyHash = SequencedEntry.keyHash(key);
        var details = new ChangeEvent<SequencedEntry<K, V>>();
        BitmapIndexedNode<SequencedEntry<K, V>> newRoot = hashMap.remove(null,
                new SequencedEntry<>(key),
                keyHash, 0, details, SequencedEntry::keyEquals);
        if (details.isModified()) {
            var oldElem = details.getOldDataNonNull();
            var result = SequencedData.vecRemove(vector, oldElem, offset);
            return size == 1 ? PersistentVectorHashMap.of() : renumber(newRoot, result.tree(), size - 1, result.offset());
        }
        return this;
    }


    @Override
    public PersistentVectorHashMap<K, V> removingAll(Iterable<? extends K> c) {
        var t = toMutable();
        return t.removeAll(c) ? t.toPersistent() : this;
    }

    private PersistentVectorHashMap<K, V> renumber(
            BitmapIndexedNode<SequencedEntry<K, V>> root,
            FingerTree<Object> vector,
            int size, int offset) {

        if (SequencedData.vecMustRenumber(size, offset, this.vector.size())) {
            // center the numbers around 0 so that we have the interval [-size/2,size]
            int newOffset = size / -2;
            var b = new PersistentVectorHashMapBuilder<K, V>(newOffset);
            b.addEntries(new TombSkippingVectorSpliterator<>(
                    new FingerTreeSpliterator<>(vector),
                    e -> ((Map.Entry<K, V>) e),
                    0, size(), vector.size(),
                    Spliterator.NONNULL | characteristics()));
            var tmp = b.build();
            assert tmp.size() == size;
            return newInstance(tmp.hashMap, tmp.vector, size, newOffset);
        }
        return newInstance(root, vector, size, offset);
    }

    @Override
    public PersistentVectorHashMap<K, V> retainingAll(Iterable<? extends K> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    Iterator<Map.Entry<K, V>> reverseIterator() {
        return Spliterators.iterator(reverseSpliterator());
    }

    @SuppressWarnings("unchecked")
    Spliterator<Map.Entry<K, V>> reverseSpliterator() {
        return new ReverseTombSkippingVectorSpliterator<>(vector,
                e -> ((SequencedEntry<K, V>) e),
                size(), Spliterator.NONNULL | characteristics());
    }


    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public Spliterator<Map.Entry<K, V>> spliterator() {
        return new TombSkippingVectorSpliterator<>(
                new FingerTreeSpliterator<>(vector),
                e -> ((Map.Entry<K, V>) e),
                0, size(), vector.size(),
                Spliterator.NONNULL | characteristics());
    }

    /// Creates a mutable copy of this map.
    ///
    /// @return a mutable sequenced CHAMP map
    @Override
    public MutableVectorHashMap<K, V> toMutable() {
        return new MutableVectorHashMap<>(this);
    }

    @Override
    public MutableVectorHashMap<K, V> asMap() {
        return new MutableVectorHashMap<>(this);
    }

    /// Returns a string representation of this map.
    ///
    /// The string representation is consistent with the one produced
    /// by [AbstractMap#toString()].
    ///
    /// @return a string representation
    @Override
    public String toString() {
        return ReadableMap.mapToString(this);
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
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
        protected Object readResolve() {
            return PersistentVectorHashMap.of().puttingAll(deserializedEntries);
        }
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED;
    }

}
