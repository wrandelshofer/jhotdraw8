/*
 * @(#)PersistentSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * An interface to an persistent collection with a well-defined iteration order;
 * the implementation guarantees that the state of the collection does not
 * change.
 * <p>
 * An interface to an persistent sequenced collection provides methods for creating a new
 * persistent sequenced collection with added or removed elements, without
 * changing the original persistent sequenced collection.
 *
 * @param <E> the element type
 */
public interface PersistentSequencedCollection<E> extends PersistentCollection<E>, ReadableSequencedCollection<E> {
    @Override
    PersistentSequencedCollection<E> add(E element);

    @Override
    PersistentSequencedCollection<E> addAll(Iterable<? extends E> c);

    /**
     * Returns a copy of this collection that contains all elements
     * of this collection and also the specified element as the first
     * element in the iteration order.
     * <p>
     * A collection may prevent that the same element can be
     * added more than once.
     * <p>
     * If the iteration order is based on an ordering relation of
     * the elements, then the element is only the first in a sequence of elements
     * with the same ordering relation; which is not necessarily the first in
     * the total iteration order.
     *
     * @param element an element
     * @return this collection instance if it already contains the element
     * as the first in the iteration order, or
     * a different collection instance with the element added as the first
     * in the iteration order
     */
    PersistentSequencedCollection<E> addFirst(final @Nullable E element);

    /**
     * Returns a copy of this collection that contains all elements
     * of this collection and also the specified element as the last
     * element in the iteration order.
     * <p>
     * A collection may prevent that the same element can be
     * added more than once.
     * <p>
     * If the iteration order is based on an ordering relation of
     * the elements, then the element is only the last in a sequence of elements
     * with the same ordering relation; which is not necessarily the last in
     * the total iteration order.
     *
     * @param element an element
     * @return this collection instance if it already contains the element
     * as the last in the iteration order, or
     * a different collection instance with the element added as the last
     * in the iteration order
     */
    PersistentSequencedCollection<E> addLast(final @Nullable E element);

    @Override
    <T> PersistentSequencedCollection<T> empty();

    @Override
    PersistentSequencedCollection<E> remove(E element);

    @Override
    PersistentSequencedCollection<E> removeAll(Iterable<?> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    default PersistentSequencedCollection<E> removeFirst() {
        return remove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return a new set instance with the last element removed
     * @throws NoSuchElementException if this set is empty
     */
    default PersistentSequencedCollection<E> removeLast() {
        return remove(getLast());
    }

    @Override
    PersistentSequencedCollection<E> retainAll(Iterable<?> c);

}
