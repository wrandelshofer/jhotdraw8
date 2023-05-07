/*
 * @(#)SequencedChampSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.collection.enumerator.IteratorFacade;
import org.jhotdraw8.collection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.collection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.collection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.collection.impl.champ.ChampSequencedData;
import org.jhotdraw8.collection.impl.champ.ChampVectorSpliterator;
import org.jhotdraw8.collection.impl.champ.ChangeEvent;
import org.jhotdraw8.collection.impl.champ.Node;
import org.jhotdraw8.collection.impl.champ.ReverseChampVectorSpliterator;
import org.jhotdraw8.collection.impl.champ.SequencedElement;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.collection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
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
 *     <li>add: O(log N) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>remove: O(log N) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>contains: O(1)</li>
 *     <li>toMutable: O(1) + O(log N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(1)</li>
 *     <li>iterator.next: O(log N)</li>
 *     <li>getFirst(), getLast(): O(log N)</li>
 * </ul>
 * <p>
 * Implementation details:
 * <p>
 * This set performs read and write operations of single elements in O(log N) time,
 * and in O(log N) space, where N is the number of elements in the set.
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
 *      <dt>VectorMap.scala
 *      <br>The Scala library. Copyright EPFL and Lightbend, Inc. Apache License 2.0.</dt>
 *      <dd><a href="https://github.com/scala/scala/blob/28eef15f3cc46f6d3dd1884e94329d7601dc20ee/src/library/scala/collection/immutable/VectorMap.scala">github.com</a>
 *      </dd>
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class VectorSet<E>
        extends BitmapIndexedNode<SequencedElement<E>>
        implements Serializable, ImmutableSequencedSet<E> {
    private static final @NonNull VectorSet<?> EMPTY = new VectorSet<>(
            BitmapIndexedNode.emptyNode(), VectorList.of(), 0, 0);
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * Offset of sequence numbers to vector indices.
     *
     * <pre>vector index = sequence number + offset</pre>
     */
    final int offset;
    /**
     * The size of the set.
     */
    final int size;
    /**
     * In this vector we store the elements in the order in which they were inserted.
     */
    final @NonNull VectorList<Object> vector;

    VectorSet(
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
     * @param c   an iterable
     * @param <E> the element type
     * @return an immutable set of the provided elements
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull VectorSet<E> copyOf(@NonNull Iterable<? extends E> c) {
        return VectorSet.<E>of().addAll(c);
    }


    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull VectorSet<E> of() {
        return ((VectorSet<E>) VectorSet.EMPTY);
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
    public static <E> @NonNull VectorSet<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return VectorSet.<E>of().addAll(Arrays.asList(elements));
    }

    @Override
    public @NonNull VectorSet<E> add(@Nullable E key) {
        return addLast(key, false);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public @NonNull VectorSet<E> addAll(@NonNull Iterable<? extends E> c) {
        var m = toMutable();
        m.addAll(c);
        return m.toImmutable();
    }

    public @NonNull VectorSet<E> addFirst(@Nullable E key) {
        return addFirst(key, true);
    }

    private @NonNull VectorSet<E> addFirst(@Nullable E e, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, -offset - 1);
        var newRoot = put(null, newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToFirst ? SequencedElement::putAndMoveToFirst : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        if (details.isModified()) {
            var newVector = vector;
            int newSize = size;

            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = ChampSequencedData.vecRemove(newVector, details.getOldDataNonNull(), details, offset);
                    newVector = result.first();
                }
            } else {
                newSize++;
            }
            int newOffset = offset + 1;
            newVector = newVector.addFirst(newElem);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    public @NonNull VectorSet<E> addLast(@Nullable E key) {
        return addLast(key, true);
    }

    private @NonNull VectorSet<E> addLast(@Nullable E e,
                                          boolean moveToLast) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<E>(e, vector.size() - offset);
        var newRoot = put(null, newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToLast ? SequencedElement::putAndMoveToLast : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        if (details.isModified()) {
            var newVector = vector;
            int newOffset = offset;
            int newSize = size;
            var owner = new IdentityObject();
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldElem = details.getOldData();
                    var result = ChampSequencedData.vecRemove(newVector, oldElem, details, newOffset);
                    newVector = result.first();
                    newOffset = result.second();
                }
            } else {
                newSize++;
            }
            newVector = newVector.addLast(newElem);
            return renumber(newRoot, newVector, newSize, newOffset);
        }
        return this;
    }

    @Override
    public @NonNull MutableVectorSet<E> asSet() {
        return new MutableVectorSet<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull VectorSet<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return find(new SequencedElement<>(key), SequencedElement.keyHash(key), 0, Objects::equals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof VectorSet) {
            VectorSet<?> that = (VectorSet<?>) other;
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

    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst
        );
    }

    @Override
    public @NonNull VectorSet<E> remove(@Nullable E key) {
        int keyHash = SequencedElement.keyHash(key);
        var details = new ChangeEvent<SequencedElement<E>>();
        BitmapIndexedNode<SequencedElement<E>> newRoot = remove(null,
                new SequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            var removedElem = details.getOldDataNonNull();
            var result = ChampSequencedData.vecRemove(vector, removedElem, details, offset);
            return size == 1 ? VectorSet.of() : renumber(newRoot, result.first(), size - 1,
                    result.second());
        }
        return this;
    }


    @Override
    public @NonNull VectorSet<E> removeAll(@NonNull Iterable<?> c) {
        var m = toMutable();
        m.removeAll(c);
        return m.toImmutable();
    }

    @SuppressWarnings("unchecked")
    @Override
    public VectorSet<E> removeFirst() {
        return remove(getFirst());
    }

    @SuppressWarnings("unchecked")
    @Override
    public VectorSet<E> removeLast() {
        return remove(getLast());
    }

    /**
     * Renumbers the sequenced elements in the trie if necessary.
     *
     * @param root   the root of the trie
     * @param vector the root of the vector
     * @param size   the size of the trie
     * @param offset the offset that must be added to a sequence number to get the index into the vector
     * @return a new {@link VectorSet} instance
     */
    @NonNull
    private VectorSet<E> renumber(
            BitmapIndexedNode<SequencedElement<E>> root,
            VectorList<Object> vector,
            int size, int offset) {

        if (ChampSequencedData.vecMustRenumber(size, offset, this.vector.size())) {
            var owner = new IdentityObject();
            var result = ChampSequencedData.vecRenumber(
                    new IdentityObject(), size, root, vector, SequencedElement::elementKeyHash, Objects::equals,
                    (e, seq) -> new SequencedElement<>(e.getElement(), seq));
            return new VectorSet<>(
                    result.first(), result.second(),
                    size, 0);
        }
        return new VectorSet<>(root, vector, size, offset);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull VectorSet<E> retainAll(@NonNull Iterable<?> c) {
        var m = toMutable();
        m.retainAll(c);
        return m.toImmutable();
    }

    public @NonNull Iterator<E> reverseIterator() {
        return new IteratorFacade<>(reverseSpliterator(), null);
    }

    @SuppressWarnings("unchecked")
    private @NonNull EnumeratorSpliterator<E> reverseSpliterator() {
        return new ReverseChampVectorSpliterator<>(vector,
                e -> ((SequencedElement<E>) e).getElement(),
                size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull EnumeratorSpliterator<E> spliterator() {
        return new ChampVectorSpliterator<>(vector,
                e -> ((SequencedElement<E>) e).getElement(),
                size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    @Override
    public @NonNull MutableVectorSet<E> toMutable() {
        return new MutableVectorSet<>(this);
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
            return VectorSet.copyOf(deserialized);
        }
    }

}