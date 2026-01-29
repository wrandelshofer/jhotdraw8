/*
 * @(#)PersistentList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableList;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An interface to an persistent list; the implementation guarantees that the state of the collection does not change.
 * <p>
 * An interface to an persistent list provides methods for creating a new persistent list with
 * added or removed elements, without changing the original persistent list.
 *
 * @param <E> the element type
 */
public interface PersistentList<E> extends ReadableList<E>, PersistentSequencedCollection<E> {
    /**
     * Returns a copy of this list that is empty.
     *
     * @return this list instance if it is already empty, or a different list
     * instance that is empty.
     */
    @SuppressWarnings("unchecked")
    @Override
    <T> PersistentList<T> empty();

    @Override
    PersistentList<E> addFirst(@Nullable final E element);

    @Override
    PersistentList<E> addLast(@Nullable final E element);

    @Override
    default PersistentList<E> removeFirst() {
        return (PersistentList<E>) PersistentSequencedCollection.super.removeFirst();
    }

    @Override
    default PersistentList<E> removeLast() {
        return (PersistentList<E>) PersistentSequencedCollection.super.removeLast();
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
    PersistentList<E> add(E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element appended to the
     * end of the list.
     *
     * @param index   the insertion index
     * @param element an element
     * @return a different list instance with the element added
     */
    PersistentList<E> add(int index, E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and all elements of the specified
     * collection appended.
     *
     * @param c a collection to be added to this list
     * @return a different list instance with the elements added
     */
    @Override
    PersistentList<E> addAll(Iterable<? extends E> c);

    /**
     * Returns a copy of this list that contains all elements
     * of this list and all elements of the specified
     * collection appended.
     *
     * @param index the insertion index
     * @param c     a collection to be added to this list
     * @return a different list instance with the elements added
     */
    PersistentList<E> addAll(int index, Iterable<? extends E> c);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the specified element.
     *
     * @param element an element
     * @return this list instance if it already does not contain the element, or
     * a different list instance with the element removed
     */
    @Override
    PersistentList<E> remove(E element);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the element at the specified index
     *
     * @param index an index
     * @return a different list instance with the element removed
     */
    PersistentList<E> removeAt(int index);

    /**
     * Returns a copy of this list that contains all elements
     * of this list except the elements in the specified range.
     *
     * @param fromIndex from index (inclusive) of the sub-list
     * @param toIndex   to index (exclusive) of the sub-list
     * @return a different list instance with the element removed
     */
    PersistentList<E> removeRange(int fromIndex, int toIndex);

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
    PersistentList<E> removeAll(Iterable<?> c);

    /**
     * Returns a copy of this list that contains only elements
     * that are in this list and in the specified collection.
     *
     * @param c a collection with elements to be retained in this set
     * @return this list instance if it has not changed, or
     * a different list instance with elements removed
     */
    @Override
    PersistentList<E> retainAll(Iterable<?> c);

    /**
     * Returns a reversed copy of this list.
     * <p>
     * This operation may be implemented in O(N).
     * <p>
     * Use {@link #readableReversed()} if you only
     * need to iterate in the reversed sequence over this list.
     *
     * @return a reversed copy of this list.
     */
    PersistentList<E> reverse();

    /**
     * Returns a copy of this list that contains all elements
     * of this list and the specified element replaced.
     *
     * @param element an element
     * @return this list instance if it has not changed, or
     * a different list instance with the element changed
     */
    PersistentList<E> set(int index, E element);

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
    PersistentList<E> readableSubList(int fromIndex, int toIndex);

    /**
     * Returns a mutable copy of this list.
     *
     * @return a mutable copy.
     */
    List<E> toMutable();
}
