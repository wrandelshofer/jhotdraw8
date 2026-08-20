/*
 * @(#)SimplePersistentSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.alt.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champset.ChangeEvent;
import org.jhotdraw8.icollection.alt.impl.champset.Node;
import org.jhotdraw8.icollection.alt.impl.champset.ReverseTombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.alt.impl.champset.SequencedData;
import org.jhotdraw8.icollection.alt.impl.champset.SequencedElement;
import org.jhotdraw8.icollection.alt.impl.champset.TombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.facade.ReadableSequencedSetFacade;
import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeAPI;
import org.jhotdraw8.icollection.impl.fingertree.FingerTreeSpliterator;
import org.jhotdraw8.icollection.impl.iteration.MappedIterator;
import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;


/// Implements the [PersistentSequencedSet] interface using a Compressed
/// Hash-Array Mapped Prefix-tree (CHAMP) and a bit-mapped trie (Vector).
///
/// Features:
///
///   - supports up to 2<sup>30</sup> elements
///   - allows null elements
///   - is persistent
///   - is thread-safe
///   - iterates in the order, in which elements were inserted
///
///
/// Performance characteristics:
///
///   - add: O(log₃₂ N) in an amortized sense, because we sometimes have to
///     renumber the elements.
///   - remove: O(log₃₂ N) in an amortized sense, because we sometimes have to
///     renumber the elements.
///   - contains: O(log₃₂ N)
///   - toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in
///     the mutable copy
///   - clone: O(1)
///   - iterator creation: O(log₃₂ N)
///   - iterator.next: O(1)
///   - getFirst(), getLast(): O(log₃₂ N)
///
///
/// Implementation details:
///
/// This set performs read and write operations of single elements in O(log N) time,
/// and in O(log N) space, where N is the number of elements in the set.
///
/// The CHAMP trie contains nodes that may be shared with other sets.
///
/// If a write operation is performed on a node, then this set creates a
/// copy of the node and of all parent nodes up to the root (copy-path-on-write).
/// Since the CHAMP trie has a fixed maximal height, the cost is O(1).
///
/// This set can create a mutable copy of itself in O(1) time and O(1) space
/// using method [#toMutable()]. The mutable copy shares its nodes
/// with this set, until it has gradually replaced the nodes with exclusively
/// owned nodes.
///
/// Insertion Order:
///
/// This set uses a counter to keep track of the insertion order.
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
/// It is sufficient, if we update the neighbor with the lowest index and the one with the
/// highest index.
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
/// @param <E> the element type
@SuppressWarnings("exports")
public class PersistentVectorHashSet<E> implements Serializable, PersistentSequencedSet<E> {
    private static final PersistentVectorHashSet<?> EMPTY = new PersistentVectorHashSet<>(
            BitmapIndexedNode.emptyNode(), FingerTreeAPI.of(), 0, 0);
    @Serial
    private static final long serialVersionUID = 0L;
    @SuppressWarnings("TransientFieldNotInitialized")
    final transient BitmapIndexedNode<SequencedElement<E>> hashSet;
    /// Sequence number of the first element.
    /// `vector index = sequence number - offset`
    final int offset;
    /// The size of the set.
    final int size;

    /// In this vector we store the elements in the order in which they were inserted.
    final PersistentVectorList<Object> vector;

    private PersistentVectorHashSet<E> newInstance(BitmapIndexedNode<SequencedElement<E>> root,
                                                   PersistentVectorList<Object> vector,
                                                   int size, int offset) {
        return new PersistentVectorHashSet<>(root, vector, size, offset);
    }

    PersistentVectorHashSet(
            BitmapIndexedNode<SequencedElement<E>> hashSet,
            PersistentVectorList<Object> vector,
            int size, int offset) {
        this.hashSet = hashSet;
        this.size = size;
        this.offset = offset;
        this.vector = Objects.requireNonNull(vector);
    }


    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param c   an iterable
    /// @param <E> the element type
    /// @return a persistent set of the provided elements
    public static <E> PersistentVectorHashSet<E> copyOf(Iterable<? extends E> c) {
        return new PersistentVectorHashSetBuilder<E>().addAll(c).build();

    }


    /// Returns an empty persistent set.
    ///
    /// @param <E> the element type
    /// @return an empty persistent set
    @SuppressWarnings("unchecked")
    public static <E> PersistentVectorHashSet<E> of() {
        return ((PersistentVectorHashSet<E>) PersistentVectorHashSet.EMPTY);
    }

    /// Returns a persistent set that contains the provided elements.
    ///
    /// @param elements elements
    /// @param <E>      the element type
    /// @return a persistent set of the provided elements
    @SuppressWarnings({"unchecked", "varargs"})
    @SafeVarargs
    public static <E> PersistentVectorHashSet<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new PersistentVectorHashSetBuilder<E>().addArray(elements).build();
    }

    @Override
    public PersistentVectorHashSet<E> adding(@Nullable E key) {
        return addLast(key, false);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public PersistentVectorHashSet<E> addingAll(Iterable<? extends E> c) {
        if (isEmpty() && c instanceof PersistentVectorHashSet<? extends E> s) {
            return (PersistentVectorHashSet<E>) s;
        }
        var m = toMutable();
        return m.addAll(c) ? m.toPersistent() : this;
    }

    public PersistentVectorHashSet<E> addingFirst(@Nullable E element) {
        return addingFirst(element, true);
    }

    private PersistentVectorHashSet<E> addingFirst(@Nullable E e, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, offset - 1);
        var newRoot = hashSet.put(null, newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToFirst ? SequencedElement::putAndMoveToFirst : SequencedElement::keepOldValue,
                Objects::equals, SequencedElement::elementKeyHash);
        if (details.isModified()) {
            var newVector = vector;
            int newSize = size;

            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = SequencedData.vecRemove(newVector, details.getOldDataNonNull(), offset);
                    newVector = result.tree();
                }
            } else {
                newSize++;
            }
            int newOffset = offset - 1;
            newVector = FingerTreeAPI.addFirst(newVector, newElem);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    @Override
    public PersistentVectorHashSet<E> addingLast(@Nullable E element) {
        return addLast(element, true);
    }

    private PersistentVectorHashSet<E> addLast(@Nullable E e,
                                               boolean moveToLast) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, vector.size() + offset);
        var newRoot = hashSet.put(null, newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToLast ? SequencedElement::putAndMoveToLast : SequencedElement::keepOldValue,
                Objects::equals, SequencedElement::elementKeyHash);
        if (details.isModified()) {
            var newVector = vector;
            int newOffset = offset;
            int newSize = size;
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldElem = details.getOldData();
                    var result = SequencedData.vecRemove(newVector, oldElem, newOffset);
                    newVector = result.tree();
                    newOffset = result.offset();
                }
            } else {
                newSize++;
            }
            newVector = FingerTreeAPI.addLast(newVector, newElem);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    /// {@inheritDoc}
    @Override
    public PersistentVectorHashSet<E> cleared() {
        return PersistentVectorHashSet.<E>of();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        @SuppressWarnings("unchecked") E key = (E) o;
        return hashSet.find(new SequencedElement<>(key), SequencedElement.keyHash(key), 0, Objects::equals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof PersistentVectorHashSet<?> that) {
            return size == that.size && hashSet.equivalent(that.hashSet);
        } else {
            return ReadableSet.setEquals(this, other);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getFirst() {
        return ((SequencedElement<E>) vector.getFirst()).getElement();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getLast() {
        return ((SequencedElement<E>) vector.getLast()).getElement();
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iterator());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<E> iterator() {
        if (vector.size() == size) {
            // No skipping iterator needed, because we have no tombstones.
            return new MappedIterator<>(
                    FingerTreeAPI.iterator((PersistentVectorList<SequencedElement<E>>) (PersistentVectorList<?>) vector),
                    SequencedElement::getElement
            );
        }
        // FIXME - must implement iterator directly to obtain a good performance
        return Spliterators.iterator(spliterator());
    }

    @Override
    public int maxSize() {
        return 1 << 30;
    }

    @Override
    public ReadableSequencedSet<E> readableReversed() {
        return new ReadableSequencedSetFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst,
                Spliterator.IMMUTABLE);
    }

    @Override
    public PersistentVectorHashSet<E> removing(@Nullable E key) {
        int keyHash = SequencedElement.keyHash(key);
        var details = new ChangeEvent<SequencedElement<E>>();
        BitmapIndexedNode<SequencedElement<E>> newRoot = hashSet.remove(null,
                new SequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            var removedElem = details.getOldDataNonNull();
            var result = SequencedData.vecRemove(vector, removedElem, offset);
            return size == 1 ? PersistentVectorHashSet.of() : renumber(newRoot, result.tree(), size - 1,
                    result.offset());
        }
        return this;
    }


    @Override
    public PersistentVectorHashSet<E> removingAll(Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toPersistent() : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentVectorHashSet<E> removingFirst() {
        return this.removing(getFirst());
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentVectorHashSet<E> removingLast() {
        return this.removing(getLast());
    }

    /// Renumbers the sequenced elements in the trie if necessary.
    ///
    /// @param root   the root of the trie
    /// @param vector the root of the vector
    /// @param size   the size of the trie
    /// @param offset the offset that must be added to a sequence number to get the offset into the vector
    /// @return a new [PersistentVectorHashSet] instance
    private PersistentVectorHashSet<E> renumber(
            BitmapIndexedNode<SequencedElement<E>> root,
            PersistentVectorList<Object> vector,
            int size, int offset) {

        if (SequencedData.vecMustRenumber(size, offset, this.vector.size())) {
            // center the sequence numbers around 0 so that they are the interval [-size/2,size]
            int newOffset = size / -2;
            var b = new PersistentVectorHashSetBuilder<E>(new MutabilityOwnership(), newOffset);
            b.addSpliterator(new TombSkippingVectorSpliterator<>(
                    new FingerTreeSpliterator<>(vector),
                    e -> ((SequencedElement<E>) e).getElement(),
                    0, size(), vector.size(),
                    Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE));
            var tmp = b.build();
            assert tmp.size() == size;
            return newInstance(tmp.hashSet, tmp.vector, tmp.size, newOffset);
        }
        return newInstance(root, vector, size, offset);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentVectorHashSet<E> retainingAll(Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toPersistent() : this;
    }

    Iterator<E> reverseIterator() {
        return Spliterators.iterator(reverseSpliterator());
    }

    @SuppressWarnings("unchecked")
    Spliterator<E> reverseSpliterator() {
        return new ReverseTombSkippingVectorSpliterator<>(vector,
                e -> ((SequencedElement<E>) e).getElement(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> spliterator() {
        return new TombSkippingVectorSpliterator<>(
                new FingerTreeSpliterator<>(vector),
                e -> ((SequencedElement<E>) e).getElement(),
                0, size(), vector.size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    @Override
    public MutableVectorHashSet<E> toMutable() {
        return new MutableVectorHashSet<>(this);
    }

    /// Returns a string representation of this set.
    ///
    /// The string representation is consistent with the one produced
    /// by [AbstractSet#toString()].
    ///
    /// @return a string representation
    @Override
    public String toString() {
        return ReadableCollection.iterableToString(this);
    }

    @Serial
    private Object writeReplace() {
        return new SerializationProxy<>(toMutable());
    }

    private static class SerializationProxy<E> extends SetSerializationProxy<E> {
        @Serial
        private static final long serialVersionUID = 0L;

        protected SerializationProxy(Set<E> target) {
            super(target);
        }

        @Serial
        @Override
        protected Object readResolve() {
            return PersistentVectorHashSet.copyOf(deserializedElements);
        }
    }

}