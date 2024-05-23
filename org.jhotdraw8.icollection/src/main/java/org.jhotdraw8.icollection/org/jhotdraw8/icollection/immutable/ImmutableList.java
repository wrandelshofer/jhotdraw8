/*
 * @(#)ImmutableList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An interface to an immutable list; the implementation guarantees that the state of the collection does not change.
 * <p>
 * An interface to an immutable list provides methods for creating a new immutable list with
 * added or removed elements, without changing the original immutable list.
 *
 * @param <E> the element type
 */
public interface ImmutableList<E> extends ReadOnlyList<E>, ImmutableSequencedCollection<E> {
    /**
     * Returns a copy of this list that is empty.
     *
     * @return this list instance if it is already empty, or a different list
     * instance that is empty.
     */
    @SuppressWarnings("unchecked")
    @Override
    <T> ImmutableList<T> empty();

    @Override
    ImmutableList<E> addFirst(@Nullable final E element);

    @Override
    ImmutableList<E> addLast(@Nullable final E element);

    @Override
    default ImmutableList<E> removeFirst() {
        return (ImmutableList<E>) ImmutableSequencedCollection.super.removeFirst();
    }

    @Override
    default ImmutableList<E> removeLast() {
        return (ImmutableList<E>) ImmutableSequencedCollection.super.removeLast();
    }

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element appended to the
     * end of the list.
     *
     * @param element an element
     * @return a different list instance with the element added
     */
    @Override
    ImmutableList<E> add(E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element appended to the
     * end of the list.
     *
     * @param index   the insertion index
     * @param element an element
     * @return a different list instance with the element added
     */
    ImmutableList<E> add(int index, E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and all elements of the specified
     * collection appended.
     *
     * @param c a collection to be added to this list
     * @return a different list instance with the elements added
     */
    @Override
    ImmutableList<E> addAll(Iterable<? extends E> c);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and all elements of the specified
     * collection appended.
     *
     * @param index the insertion index
     * @param c     a collection to be added to this list
     * @return a different list instance with the elements added
     */
    ImmutableList<E> addAll(int index, Iterable<? extends E> c);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the specified element.
     *
     * @param element an element
     * @return this list instance if it already does not contain the element, or
     * a different list instance with the element removed
     */
    @Override
    ImmutableList<E> remove(E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the element at the specified index
     *
     * @param index an index
     * @return a different list instance with the element removed
     */
    ImmutableList<E> removeAt(int index);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the elements in the specified range.
     *
     * @param fromIndex from index (inclusive) of the sub-list
     * @param toIndex   to index (exclusive) of the sub-list
     * @return a different list instance with the element removed
     */
    ImmutableList<E> removeRange(int fromIndex, int toIndex);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the elements of the specified
     * collection.
     *
     * @param c a collection with elements to be removed from this set
     * @return this list instance if it already does not contain the elements, or
     * a different list instance with the elements removed
     */
    @Override
    ImmutableList<E> removeAll(Iterable<?> c);

    /**
     * Returns a copy of this list that contains only elements
     * that are in this list and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this list instance if it has not changed, or
     * a different list instance with elements removed
     */
    @Override
    ImmutableList<E> retainAll(Iterable<?> c);

    /**
     * Returns a reversed copy of this list.
     * <p>
     * This operation may be implemented in O(N).
     * <p>
     * Use {@link #readOnlyReversed()} if you only
     * need to iterate in the reversed sequence over this list.
     *
     * @return a reversed copy of this list.
     */
    ImmutableList<E> reverse();

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element replaced.
     *
     * @param element an element
     * @return this list instance if it has not changed, or
     * a different list instance with the element changed
     */
    ImmutableList<E> set(int index, E element);

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
    ImmutableList<E> readOnlySubList(int fromIndex, int toIndex);

    /**
     * Returns a mutable copy of this list.
     *
     * @return a mutable copy.
     */
    List<E> toMutable();
}
