/*
 * @(#)ImmutableSequencedSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;

import java.util.NoSuchElementException;

/**
 * Interface for an immutable collection with a well-defined iteration order;
 * the implementation guarantees that the state of the collection does not
 * change.
 * <p>
 * An immutable sequenced collection provides methods for creating a new
 * immutable sequenced collection with added or removed elements, without
 * changing the original immutable sequenced collection.
 *
 * @param <E> the element type
 */
public interface ImmutableSequencedCollection<E> extends ImmutableCollection<E>, ReadOnlySequencedCollection<E> {
    @Override
    @NonNull ImmutableSequencedCollection<E> add(E element);

    @Override
    @NonNull ImmutableSequencedCollection<E> addAll(@NonNull Iterable<? extends E> c);

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
    @NonNull ImmutableSequencedCollection<E> addFirst(final @Nullable E element);

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
    @NonNull ImmutableSequencedCollection<E> addLast(final @Nullable E element);

    @Override
    @NonNull ImmutableSequencedCollection<E> clear();

    @Override
    @NonNull ImmutableSequencedCollection<E> remove(E element);

    @Override
    @NonNull ImmutableSequencedCollection<E> removeAll(@NonNull Iterable<?> c);

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the first.
     *
     * @return a new set instance with the first element removed
     * @throws NoSuchElementException if this set is empty
     */
    default ImmutableSequencedCollection<E> removeFirst() {
        return remove(getFirst());
    }

    /**
     * Returns a copy of this set that contains all elements
     * of this set except the last.
     *
     * @return a new set instance with the last element removed
     * @throws NoSuchElementException if this set is empty
     */
    default ImmutableSequencedCollection<E> removeLast() {
        return remove(getLast());
    }

    @Override
    @NonNull ImmutableSequencedCollection<E> retainAll(@NonNull Iterable<?> c);

}
