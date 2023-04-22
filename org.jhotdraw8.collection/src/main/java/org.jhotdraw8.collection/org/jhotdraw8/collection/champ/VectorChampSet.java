/*
 * @(#)SequencedChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.champ;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.IdentityObject;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.collection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;
import org.jhotdraw8.collection.vector.VectorList;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;


/**
 * Implements a mutable set using a Compressed Hash-Array Mapped Prefix-tree
 * (CHAMP) and a bit-mapped trie (Vector).
 * <p>
 * Features:
 * <ul>
 *     <li>supports up to 2<sup>30</sup> elements</li>
 *     <li>allows null elements</li>
 *     <li>is immutable</li>
 *     <li>is thread-safe</li>
 *     <li>iterates in the order, in which elements were inserted</li>
 * </ul>
 * <p>
 * Performance characteristics:
 * <ul>
 *     <li>add: O(1) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>remove: O(1) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst(), getLast(): O(1)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(1) time,
 * and in O(1) space.
 * <p>
 * The CHAMP trie contains nodes that may be shared with other sets.
 * <p>
 * If a write operation is performed on a node, then this set creates a
 * copy of the node and of all parent nodes up to the root (copy-path-on-write).
 * Since the CHAMP trie has a fixed maximal height, the cost is O(1).
 * <p>
 * This set can create a mutable copy of itself in O(1) time and O(1) space
 * using method {@link #toMutable()}}. The mutable copy shares its nodes
 * with this set, until it has gradually replaced the nodes with exclusively
 * owned nodes.
 * <p>
 * Insertion Order:
 * <p>
 * This set uses a counter to keep track of the insertion order.
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
 * References:
 * <dl>
 *      <dt>Michael J. Steindorfer (2017).
 *      Efficient Immutable Collections.</dt>
 *      <dd><a href="https://michael.steindorfer.name/publications/phd-thesis-efficient-immutable-collections">michael.steindorfer.name</a>
 *
 *      <dt>The Capsule Hash Trie Collections Library.
 *      <br>Copyright (c) Michael Steindorfer. BSD-2-Clause License</dt>
 *      <dd><a href="https://github.com/usethesource/capsule">github.com</a>
 *
 *      <dt>VectorMap.scala
 *      <br>The Scala library. Copyright EPFL and Lightbend, Inc. Apache License 2.0.</dt>
 *      <dd><a href="https://github.com/scala/scala/blob/28eef15f3cc46f6d3dd1884e94329d7601dc20ee/src/library/scala/collection/immutable/VectorMap.scala">github.com</a></dd>
 *
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class VectorChampSet<E>
        extends BitmapIndexedNode<SequencedElement<E>>
        implements Serializable, ImmutableSequencedSet<E> {
    @Serial
    private static final long serialVersionUID = 0L;

    private static final @NonNull VectorChampSet<?> EMPTY = new VectorChampSet<>(
            BitmapIndexedNode.emptyNode(), VectorList.of(), 0, 0);

    /**
     * In this vector we store the elements in the order in which they were inserted.
     */
    final @NonNull VectorList<Object> vector;

    final int size;

    /**
     * Offset of sequence numbers to vector indices.
     *
     * <pre>vector index = sequence number + offset</pre>
     */
    final int offset;

    VectorChampSet(
            @NonNull BitmapIndexedNode<SequencedElement<E>> root,
            @NonNull VectorList<Object> vector,
            int size, int offset) {
        super(root.nodeMap(), root.dataMap(), root.mixed);
        this.size = size;
        this.offset = offset;
        this.vector = Objects.requireNonNull(vector);
    }


    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param iterable an iterable
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull VectorChampSet<E> copyOf(@NonNull Iterable<? extends E> iterable) {
        if (iterable instanceof VectorChampSet) {
            return (VectorChampSet<E>) iterable;
        } else if (iterable instanceof MutableVectorChampSet) {
            return ((MutableVectorChampSet<E>) iterable).toImmutable();
        }
        MutableVectorChampSet<E> tr = new MutableVectorChampSet<>(of());
        tr.addAll(iterable);
        return tr.toImmutable();
    }


    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull VectorChampSet<E> of() {
        return ((VectorChampSet<E>) VectorChampSet.EMPTY);
    }


    /**
     * Returns an immutable set that contains the provided elements.
     *
     * @param elements elements
     * @param <E>      the element type
     * @return an immutable set of the provided elements
     */

    @SuppressWarnings({"unchecked", "varargs"})
    @SafeVarargs
    public static <E> @NonNull VectorChampSet<E> of(E @NonNull ... elements) {
        if (elements.length == 0) {
            return (VectorChampSet<E>) VectorChampSet.EMPTY;
        } else {
            return ((VectorChampSet<E>) VectorChampSet.EMPTY).addAll(Arrays.asList(elements));
        }
    }

    @Override
    public @NonNull VectorChampSet<E> add(@Nullable E key) {
        return addLast(key, false);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public @NonNull VectorChampSet<E> addAll(@NonNull Iterable<? extends E> set) {
        if (set == this || isEmpty() && (set instanceof VectorChampSet<?>)) {
            return (VectorChampSet<E>) set;
        }
        if (isEmpty() && (set instanceof MutableVectorChampSet<?> t)) {
            return (VectorChampSet<E>) t.toImmutable();
        }
        var t = toMutable();
        return t.addAll(set) ? t.toImmutable() : this;
    }

    public @NonNull VectorChampSet<E> addFirst(@Nullable E key) {
        return addFirst(key, true);
    }

    public @NonNull VectorChampSet<E> addLast(@Nullable E key) {
        return addLast(key, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull VectorChampSet<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return find(new SequencedElement<>(key), Objects.hashCode(key), 0, Objects::equals) != Node.NO_DATA;
    }

    private @NonNull VectorChampSet<E> addFirst(@Nullable E e,
                                                boolean moveToFirst) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, offset - 1);
        var newRoot = update(null, newElem,
                Objects.hashCode(e), 0, details,
                moveToFirst ? SequencedElement::updateAndMoveToFirst : SequencedElement::update,
                Objects::equals, Objects::hashCode);
        VectorList<Object> newSeqRoot = vector;
        if (details.isModified()) {
            int newSize = size;
            int newOffset = offset + 1;
            IdentityObject mutator = new IdentityObject();
            if (details.isReplaced()) {
                var oldElem = details.getData();
                newSeqRoot = SequencedData.vecRemove(newSeqRoot, mutator, oldElem, details);
                int seq = details.getData().getSequenceNumber();
            } else {
                newSize++;
            }
            newSeqRoot = SequencedData.vecUpdate(newSeqRoot, mutator, newElem, details, SequencedElement::update);
            return renumber(newRoot, newSeqRoot, newSize, newOffset);
        }
        return this;
    }

    private @NonNull VectorChampSet<E> addLast(@Nullable E e,
                                               boolean moveToLast) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<E>(e, vector.size() - offset);
        var newRoot = update(
                null, newElem, Objects.hashCode(e), 0,
                details,
                moveToLast ? SequencedElement::updateAndMoveToLast : SequencedElement::update,
                Objects::equals, Objects::hashCode);
        if (details.isModified()) {
            var newSeqRoot = vector;
            int newOffset = offset;
            int newSize = size;
            var mutator = new IdentityObject();
            if (details.isReplaced()) {
                var oldElem = details.getData();
                newSeqRoot = SequencedData.vecRemove(newSeqRoot, mutator, oldElem, details);
                int seq = details.getData().getSequenceNumber();
                newOffset = seq == newOffset - 1 ? newOffset - 1 : newOffset;
            } else {
                newSize++;
            }
            newSeqRoot = SequencedData.vecUpdate(newSeqRoot, mutator, newElem, details, SequencedElement::update);
            return renumber(newRoot, newSeqRoot, newSize, newOffset);
        }
        return this;
    }

    private @NonNull VectorChampSet<E> remove(@Nullable E key, int newOffset) {
        int keyHash = Objects.hashCode(key);
        var details = new ChangeEvent<SequencedElement<E>>();
        BitmapIndexedNode<SequencedElement<E>> newRoot = remove(null,
                new SequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        var newSeqRoot = vector;
        if (details.isModified()) {
            var oldElem = details.getData();
            newSeqRoot = SequencedData.vecRemove(newSeqRoot, null, oldElem, details);
            int seq = oldElem.getSequenceNumber();
            return renumber(newRoot, newSeqRoot, size - 1,
                    (seq == newOffset) ? newOffset + 1 : newOffset);
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
        if (other instanceof VectorChampSet) {
            VectorChampSet<?> that = (VectorChampSet<?>) other;
            return size == that.size && equivalent(that);
        } else {
            return ReadOnlySet.setEquals(this, other);
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
        return ReadOnlySet.iteratorToHashCode(iterator());
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new IteratorFacade<>(spliterator(), null);
    }

    @SuppressWarnings("unchecked")
    private @NonNull EnumeratorSpliterator<E> reversedSpliterator() {
        return new ReverseVectorSpliterator<>(vector,
                e -> ((SequencedElement<E>) e).getElement(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public EnumeratorSpliterator<E> spliterator() {
        return new VectorSpliterator<>(vector,
                e -> ((SequencedElement<E>) e).getElement(),
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE, size());
    }

    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(
                this::reversedIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst
        );
    }

    @Override
    public @NonNull VectorChampSet<E> remove(@Nullable E key) {
        return remove(key, -offset);
    }

    @Override
    public @NonNull VectorChampSet<E> removeAll(@NonNull Iterable<?> set) {
        if (set == this) {
            return of();
        }
        if (isEmpty()
                || (set instanceof Collection<?> c) && c.isEmpty()
                || (set instanceof ReadOnlyCollection<?> rc) && rc.isEmpty()) {
            return this;
        }
        var t = toMutable();
        return t.removeAll(set) ? t.toImmutable() : this;
    }

    @Override
    public @NonNull MutableVectorChampSet<E> toMutable() {
        return new MutableVectorChampSet<>(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public VectorChampSet<E> removeFirst() {
        SequencedElement<E> k = (SequencedElement<E>) vector.getFirst();
        return remove(k.getElement(), k.getSequenceNumber() + 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public VectorChampSet<E> removeLast() {
        SequencedElement<E> k = (SequencedElement<E>) vector.getLast();
        return remove(k.getElement(), -offset);
    }


    /**
     * Renumbers the sequenced elements in the trie if necessary.
     *
     * @param root    the root of the trie
     * @param seqRoot the root of the vector
     * @param size    the size of the trie
     * @param offset  the offset that must be added to a sequence number to get the index into the vector
     * @return a new {@link VectorChampSet} instance
     */
    @NonNull
    private VectorChampSet<E> renumber(
            BitmapIndexedNode<SequencedElement<E>> root,
            VectorList<Object> seqRoot,
            int size, int offset) {
        /*
        if (vecMustRenumber(size, offset, vector.size())) {
            var mutator = new IdentityObject();
            var renumberedRoot = SequencedData.renumber(
                    size, root, seqRoot, mutator, Objects::hashCode, Objects::equals,
                    (e, seq) -> new SequencedElement<>(e.getElement(), seq));
            var renumberedSeqRoot = SequencedData.buildSequencedTrie(renumberedRoot, mutator);
            return new VectorChampSet<>(
                    renumberedRoot, renumberedSeqRoot,
                    size, , -1);
        }
        return new VectorChampSet<>(root, seqRoot, size, , first);

         */
        return this;
    }

    @Override
    public @NonNull VectorChampSet<E> retainAll(@NonNull Collection<?> set) {
        if (isEmpty()) {
            return this;
        }
        if (set.isEmpty()) {
            return of();
        }
        var t = toMutable();
        return t.retainAll(set) ? t.toImmutable() : this;
    }

    public @NonNull Iterator<E> reversedIterator() {
        return new IteratorFacade<>(reversedSpliterator(), null);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public @NonNull MutableVectorChampSet<E> asSet() {
        return new MutableVectorChampSet<>(this);
    }

    @Override
    public @NonNull String toString() {
        return ReadOnlyCollection.iterableToString(this);
    }

    @Serial
    private @NonNull Object writeReplace() {
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
        protected @NonNull Object readResolve() {
            return VectorChampSet.copyOf(deserialized);
        }
    }
}