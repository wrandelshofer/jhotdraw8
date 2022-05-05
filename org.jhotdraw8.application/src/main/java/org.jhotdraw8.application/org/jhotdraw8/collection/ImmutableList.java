/*
 * @(#)PersistentList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;

/**
 * Provides an API for an immutable list.
 * <p>
 * A persistent list provides methods for creating a new persistent list with
 * added or removed elements, without changing the original persistent list.
 * <p>
 * Implementations are expected to only require time and space that is
 * proportional to the differences between the newly created persistent list to
 * the original persistent list.
 */
public interface ImmutableList<E> extends ReadOnlyList<E>, ImmutableCollection<E> {
    /**
     * Returns a copy of this list that is empty.
     *
     * @param element an element
     * @return this list instance if it is already empty, or a different list
     * instance that is empty.
     */
    @NonNull ImmutableList<E> copyClear();

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element appended to the
     * end of the list.
     *
     * @param element an element
     * @return a different list instance with the element added
     */
    @NonNull ImmutableList<E> copyAdd(@NonNull E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element appended to the
     * end of the list.
     *
     * @param index   the insertion index
     * @param element an element
     * @return a different list instance with the element added
     */
    @NonNull ImmutableList<E> copyAdd(int index, @NonNull E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and all elements of the specified
     * collection appended.
     *
     * @param c a collection to be added to this list
     * @return a different list instance with the elements added
     */
    @NonNull ImmutableList<E> copyAddAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and all elements of the specified
     * collection appended.
     *
     * @param index the insertion index
     * @param c     a collection to be added to this list
     * @return a different list instance with the elements added
     */
    @NonNull ImmutableList<E> copyAddAll(int index, @NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the specified element.
     *
     * @param element an element
     * @return this list instance if it already does not contain the element, or
     * a different list instance with the element removed
     */
    @NonNull ImmutableList<E> copyRemove(@NonNull E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the element at the specified index
     *
     * @param index an index
     * @return a different list instance with the element removed
     */
    @NonNull ImmutableList<E> copyRemoveAt(int index);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the elements in the specified range.
     *
     * @param fromIndex from index (inclusive) of the sub-list
     * @param toIndex   to index (exclusive) of the sub-list
     * @return a different list instance with the element removed
     */
    @NonNull ImmutableList<E> copyRemoveRange(int fromIndex, int toIndex);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the elements of the specified
     * collection.
     *
     * @param c a collection with elements to be removed from this set
     * @return this list instance if it already does not contain the elements, or
     * a different list instance with the elements removed
     */
    @NonNull ImmutableList<E> copyRemoveAll(@NonNull Iterable<? extends E> c);

    /**
     * Returns a copy of this list that contains only elements
     * that are in this list and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this list instance if it has not changed, or
     * a different list instance with elements removed
     */
    @NonNull ImmutableList<E> copyRetainAll(@NonNull Collection<? extends E> c);

    /**
     * Returns a copy of this list that contains only elements
     * that are in this list and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this list instance if it has not changed, or
     * a different list instance with elements removed
     */
    default @NonNull ImmutableList<E> copyRetainAll(final @NonNull ReadOnlyCollection<? extends E> c) {
        if (c == this) {
            return this;
        }
        return copyRetainAll(c.asCollection());
    }

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element replaced.
     *
     * @param element an element
     * @return this list instance if it has not changed, or
     * a different list instance with the element changed
     */
    @NonNull ImmutableList<E> copySet(int index, @NonNull E element);

    /**
     * Returns a copy of this list that contains only
     * the elements in the given index range.
     *
     * @param fromIndex from index (inclusive) of the sub-list
     * @param toIndex   to index (exclusive) of the sub-list
     * @return this list instance if it has not changed, or
     * a different list instance with the element changed
     */
    @Override
    @NonNull ImmutableList<E> readOnlySubList(int fromIndex, int toIndex);


}
