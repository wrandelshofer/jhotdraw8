/*
 * @(#)SimpleImmutableSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.facade.ReadOnlySequencedSetFacade;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jhotdraw8.icollection.impl.champ.BitmapIndexedNode;
import org.jhotdraw8.icollection.impl.champ.ChangeEvent;
import org.jhotdraw8.icollection.impl.champ.Node;
import org.jhotdraw8.icollection.impl.champ.ReverseTombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.impl.champ.SequencedData;
import org.jhotdraw8.icollection.impl.champ.SequencedElement;
import org.jhotdraw8.icollection.impl.champ.TombSkippingVectorSpliterator;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.jhotdraw8.icollection.serialization.SetSerializationProxy;

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;


/**
 * Implements the {@link ImmutableSequencedSet} interface using a Compressed
 * Hash-Array Mapped Prefix-tree (CHAMP) and a bit-mapped trie (Vector).
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
 *     <li>add: O(log₃₂ N) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>remove: O(log₃₂ N) in an amortized sense, because we sometimes have to
 *     renumber the elements.</li>
 *     <li>contains: O(log₃₂ N)</li>
 *     <li>toMutable: O(1) + O(log₃₂ N) distributed across subsequent updates in
 *     the mutable copy</li>
 *     <li>clone: O(1)</li>
 *     <li>iterator creation: O(log₃₂ N)</li>
 *     <li>iterator.next: O(1)</li>
 *     <li>getFirst(), getLast(): O(log₃₂ N)</li>
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
 * using method {@link #toMutable()}. The mutable copy shares its nodes
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
 * For a similar design, see 'SimpleImmutableSequencedMap.scala'. Note, that this code is not a derivative
 * of that code.
 * <dl>
 *     <dt>The Scala library. SimpleImmutableSequencedMap.scala. Copyright EPFL and Lightbend, Inc. Apache License 2.0.</dt>
 *     <dd><a href="https://github.com/scala/scala/blob/28eef15f3cc46f6d3dd1884e94329d7601dc20ee/src/library/scala/collection/immutable/VectorMap.scala">github.com</a>
 *     </dd>
 * </dl>
 *
 * @param <E> the element type
 */
@SuppressWarnings("exports")
public class SimpleImmutableSequencedSet<E>

        implements Serializable, ImmutableSequencedSet<E> {
    private static final @NonNull SimpleImmutableSequencedSet<?> EMPTY = new SimpleImmutableSequencedSet<>(
            BitmapIndexedNode.emptyNode(), SimpleImmutableList.of(), 0, 0);
    @Serial
    private static final long serialVersionUID = 0L;
    final @NonNull BitmapIndexedNode<SequencedElement<E>> root;
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
    final @NonNull SimpleImmutableList<Object> vector;

    private record OpaqueRecord<E>(@NonNull BitmapIndexedNode<SequencedElement<E>> root,
                                   @NonNull SimpleImmutableList<Object> vector,
                                   int size, int offset) {
    }

    ;

    /**
     * Creates a new instance with the provided opaque data object.
     * <p>
     * This constructor is intended to be called from a constructor
     * of the subclass, that is called from method {@link #newInstance(Opaque)}.
     *
     * @param opaque an opaque data object
     */
    @SuppressWarnings("unchecked")
    protected SimpleImmutableSequencedSet(@NonNull Opaque opaque) {
        this(((SimpleImmutableSequencedSet.OpaqueRecord<E>) opaque.get()).root,
                ((SimpleImmutableSequencedSet.OpaqueRecord<E>) opaque.get()).vector,
                ((SimpleImmutableSequencedSet.OpaqueRecord<E>) opaque.get()).size,
                ((SimpleImmutableSequencedSet.OpaqueRecord<E>) opaque.get()).offset);
    }

    /**
     * Creates a new instance with the provided opaque object as its internal data structure.
     * <p>
     * Subclasses must override this method, and return a new instance of their subclass!
     *
     * @param opaque the internal data structure needed by this class for creating the instance.
     * @return a new instance of the subclass
     */
    protected @NonNull SimpleImmutableSequencedSet<E> newInstance(@NonNull Opaque opaque) {
        return new SimpleImmutableSequencedSet<>(opaque);
    }

    private @NonNull SimpleImmutableSequencedSet<E> newInstance(@NonNull BitmapIndexedNode<SequencedElement<E>> root,
                                                                @NonNull SimpleImmutableList<Object> vector,
                                                                int size, int offset) {
        return new SimpleImmutableSequencedSet<>(new Opaque(new OpaqueRecord<>(root, vector, size, offset)));
    }

    SimpleImmutableSequencedSet(
            @NonNull BitmapIndexedNode<SequencedElement<E>> root,
            @NonNull SimpleImmutableList<Object> vector,
            int size, int offset) {
        this.root = root;
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
    public static <E> @NonNull SimpleImmutableSequencedSet<E> copyOf(@NonNull Iterable<? extends E> c) {
        return SimpleImmutableSequencedSet.<E>of().addAll(c);
    }


    /**
     * Returns an empty immutable set.
     *
     * @param <E> the element type
     * @return an empty immutable set
     */

    @SuppressWarnings("unchecked")
    public static <E> @NonNull SimpleImmutableSequencedSet<E> of() {
        return ((SimpleImmutableSequencedSet<E>) SimpleImmutableSequencedSet.EMPTY);
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
    public static <E> @NonNull SimpleImmutableSequencedSet<E> of(E @Nullable ... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return SimpleImmutableSequencedSet.<E>of().addAll(Arrays.asList(elements));
    }

    @Override
    public @NonNull SimpleImmutableSequencedSet<E> add(@Nullable E key) {
        return addLast(key, false);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public @NonNull SimpleImmutableSequencedSet<E> addAll(@NonNull Iterable<? extends E> c) {
        var m = toMutable();
        return m.addAll(c) ? m.toImmutable() : this;
    }

    public @NonNull SimpleImmutableSequencedSet<E> addFirst(@Nullable E element) {
        return addFirst(element, true);
    }

    private @NonNull SimpleImmutableSequencedSet<E> addFirst(@Nullable E e, boolean moveToFirst) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<>(e, -offset - 1);
        var newRoot = root.put(null, newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToFirst ? SequencedElement::putAndMoveToFirst : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        if (details.isModified()) {
            var newVector = vector;
            int newSize = size;

            if (details.isReplaced()) {
                if (moveToFirst) {
                    var result = SequencedData.vecRemove(newVector, details.getOldDataNonNull(), offset);
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

    public @NonNull SimpleImmutableSequencedSet<E> addLast(@Nullable E element) {
        return addLast(element, true);
    }

    private @NonNull SimpleImmutableSequencedSet<E> addLast(@Nullable E e,
                                                            boolean moveToLast) {
        var details = new ChangeEvent<SequencedElement<E>>();
        var newElem = new SequencedElement<E>(e, vector.size() - offset);
        var newRoot = root.put(null, newElem,
                SequencedElement.keyHash(e), 0, details,
                moveToLast ? SequencedElement::putAndMoveToLast : SequencedElement::put,
                Objects::equals, SequencedElement::elementKeyHash);
        if (details.isModified()) {
            var newVector = vector;
            int newOffset = offset;
            int newSize = size;
            if (details.isReplaced()) {
                if (moveToLast) {
                    var oldElem = details.getOldData();
                    var result = SequencedData.vecRemove(newVector, oldElem, newOffset);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public @NonNull SimpleImmutableSequencedSet<E> clear() {
        return isEmpty() ? this : of();
    }

    @Override
    public boolean contains(@Nullable final Object o) {
        @SuppressWarnings("unchecked") final E key = (E) o;
        return root.find(new SequencedElement<>(key), SequencedElement.keyHash(key), 0, Objects::equals) != Node.NO_DATA;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof SimpleImmutableSequencedSet) {
            SimpleImmutableSequencedSet<?> that = (SimpleImmutableSequencedSet<?>) other;
            return size == that.size && root.equivalent(that.root);
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
        return Spliterators.iterator(spliterator());
    }

    @Override
    public int maxSize() {
        return 1 << 30;
    }

    @Override
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(
                this::reverseIterator,
                this::iterator,
                this::size,
                this::contains,
                this::getLast,
                this::getFirst,
                Spliterator.IMMUTABLE);
    }

    @Override
    public @NonNull SimpleImmutableSequencedSet<E> remove(@Nullable E key) {
        int keyHash = SequencedElement.keyHash(key);
        var details = new ChangeEvent<SequencedElement<E>>();
        BitmapIndexedNode<SequencedElement<E>> newRoot = root.remove(null,
                new SequencedElement<>(key),
                keyHash, 0, details, Objects::equals);
        if (details.isModified()) {
            var removedElem = details.getOldDataNonNull();
            var result = SequencedData.vecRemove(vector, removedElem, offset);
            return size == 1 ? SimpleImmutableSequencedSet.of() : renumber(newRoot, result.first(), size - 1,
                    result.second());
        }
        return this;
    }


    @Override
    public @NonNull SimpleImmutableSequencedSet<E> removeAll(@NonNull Iterable<?> c) {
        var m = toMutable();
        return m.removeAll(c) ? m.toImmutable() : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SimpleImmutableSequencedSet<E> removeFirst() {
        return remove(getFirst());
    }

    @SuppressWarnings("unchecked")
    @Override
    public SimpleImmutableSequencedSet<E> removeLast() {
        return remove(getLast());
    }

    /**
     * Renumbers the sequenced elements in the trie if necessary.
     *
     * @param root   the root of the trie
     * @param vector the root of the vector
     * @param size   the size of the trie
     * @param offset the offset that must be added to a sequence number to get the index into the vector
     * @return a new {@link SimpleImmutableSequencedSet} instance
     */
    @NonNull
    private SimpleImmutableSequencedSet<E> renumber(
            BitmapIndexedNode<SequencedElement<E>> root,
            SimpleImmutableList<Object> vector,
            int size, int offset) {

        if (SequencedData.vecMustRenumber(size, offset, this.vector.size())) {
            var owner = new IdentityObject();
            var result = SequencedData.vecRenumber(
                    new IdentityObject(), size, vector.size(), root, vector.trie, SequencedElement::elementKeyHash, Objects::equals,
                    (e, seq) -> new SequencedElement<>(e.getElement(), seq));
            return newInstance(
                    result.first(), result.second(),
                    size, 0);
        }
        return newInstance(root, vector, size, offset);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull SimpleImmutableSequencedSet<E> retainAll(@NonNull Iterable<?> c) {
        var m = toMutable();
        return m.retainAll(c) ? m.toImmutable() : this;
    }

    @NonNull Iterator<E> reverseIterator() {
        return Spliterators.iterator(reverseSpliterator());
    }

    @SuppressWarnings("unchecked")
    @NonNull Spliterator<E> reverseSpliterator() {
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
    public @NonNull Spliterator<E> spliterator() {
        return new TombSkippingVectorSpliterator<>(vector.trie,
                e -> ((SequencedElement<E>) e).getElement(),
                0, size(), vector.size(), Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    @Override
    public @NonNull SimpleMutableSequencedSet<E> toMutable() {
        return new SimpleMutableSequencedSet<>(this);
    }

    /**
     * Returns a string representation of this set.
     * <p>
     * The string representation is consistent with the one produced
     * by {@link AbstractSet#toString()}.
     *
     * @return a string representation
     */
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
            return SimpleImmutableSequencedSet.copyOf(deserializedElements);
        }
    }

}