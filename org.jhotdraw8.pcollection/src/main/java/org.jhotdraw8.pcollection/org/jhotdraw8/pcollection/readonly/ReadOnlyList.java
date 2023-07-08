/*
 * @(#)ReadOnlyList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.pcollection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.pcollection.facade.ListFacade;
import org.jhotdraw8.pcollection.facade.ReadOnlyListFacade;
import org.jhotdraw8.pcollection.impl.iteration.ReadOnlyListSpliterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;

/**
 * Read-only interface for a list. The state of the
 * list may change.
 * <p>
 * Note: To compare a ReadOnlyList to a {@link List}, you must either
 * wrap the ReadOnlyList into a List using {@link ListFacade},
 * or wrap the List into a ReadOnlyList using {@link ReadOnlyListFacade}.
 * <p>
 * This interface does not guarantee 'read-only', it actually guarantees
 * 'readable'. We use the prefix 'ReadOnly' because this is the naming
 * convention in JavaFX for interfaces that provide read methods but no write methods.
 *
 * @param <E> the element type
 */
public interface ReadOnlyList<E> extends ReadOnlySequencedCollection<E> {

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index the index of the element
     * @return the element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    E get(int index);

    /**
     * Returns the element at the specified position in this list, counted
     * from the last element of the list.
     *
     * @param index the index of the element, counted from the last
     *              element.
     * @return the element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    default E getLast(int index) {
        return get(size() - index - 1);
    }

    /**
     * Gets the first element of the list.
     *
     * @return the first element
     * @throws java.util.NoSuchElementException if the list is empty
     */
    @Override
    default E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(0);
    }

    /**
     * Gets the last element of the list.
     *
     * @return the last element
     * @throws java.util.NoSuchElementException if the list is empty
     */
    @Override
    default E getLast() {
        int index = size() - 1;
        if (index < 0) {
            throw new NoSuchElementException();
        }
        return get(index);
    }

    /**
     * Peeks the first element of the list.
     *
     * @return the first element or null if the list is empty
     */
    default @Nullable E peekFirst() {
        return isEmpty() ? null : get(0);
    }

    /**
     * Peeks the last element of the list.
     *
     * @return the last element or null if the list is empty
     */
    default @Nullable E peekLast() {
        int index = size() - 1;
        return index < 0 ? null : get(index);
    }

    /**
     * Returns an iterator over elements of type {@code E}.
     *
     * @return an iterator.
     */
    @Override
    default @NonNull Iterator<E> iterator() {
        return new ReadOnlyListSpliterator<>(this);
    }

    /**
     * Returns a spliterator over elements of type {@code E}.
     *
     * @return an iterator.
     */
    @Override
    default @NonNull Spliterator<E> spliterator() {
        return new ReadOnlyListSpliterator<>(this);
    }

    /**
     * Returns a list iterator over elements of type {@code E}.
     *
     * @return a list iterator.
     */
    default @NonNull ListIterator<E> listIterator() {
        return new ReadOnlyListSpliterator<>(this);
    }

    /**
     * Returns a list iterator over elements of type {@code E} starting
     * at the specified index.
     *
     * @param index the start index
     * @return a list iterator.
     */
    default @NonNull ListIterator<E> listIterator(int index) {
        return new ReadOnlyListSpliterator<>(this, index, size());
    }

    /**
     * Copies this list into an ArrayList.
     *
     * @return a new ArrayList.
     */
    default @NonNull ArrayList<E> toArrayList() {
        return new ArrayList<>(this.asList());
    }

    /**
     * Wraps this list in the List interface - without copying.
     *
     * @return the wrapped list
     */
    default @NonNull List<E> asList() {
        return new ListFacade<>(this);
    }

    /**
     * Returns a view of the portion of this list between the specified
     * * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     *
     * @param fromIndex the from index
     * @param toIndex   the to index (exclusive)
     * @return the sub list
     */
    @NonNull
    ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex);

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     *
     * @param o an element
     * @return the index of the element or -1
     */
    default int indexOf(Object o) {
        for (int i = 0, n = size(); i < n; i++) {
            if (Objects.equals(get(i), o)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     *
     * @param o an element
     * @return the index of the element or -1
     */
    default int lastIndexOf(Object o) {
        for (int i = size() - 1; i >= 0; i--) {
            if (Objects.equals(get(i), o)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Compares the given list with the given object for equality.
     * <p>
     * Returns {@code true} if the given object is also a read-only list and the
     * two lists contain the same elements in the same sequence.
     *
     * @param list a list
     * @param o    an object
     * @param <E>  the element type of the list
     * @return {@code true} if the object is equal to this list
     */
    static <E> boolean listEquals(@NonNull ReadOnlyList<E> list, @Nullable Object o) {
        if (o == list) {
            return true;
        }
        if (!(o instanceof ReadOnlyList)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        ReadOnlyCollection<E> that = (ReadOnlyCollection<E>) o;
        if (that.size() != list.size()) {
            return false;
        }
        for (Iterator<E> i = that.iterator(), j = list.iterator(); j.hasNext(); ) {
            if (!Objects.equals(i.next(), j.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the hash code of the provided iterable, assuming that
     * the iterator is from a list.
     *
     * @param iterator an iterator over a list
     * @return the ordered sum of the hash codes of the elements in the list
     * @see List#hashCode()
     */
    static <E> int iteratorToHashCode(@NonNull Iterator<E> iterator) {
        int h = 1;
        while (iterator.hasNext()) {
            E e = iterator.next();
            h = 31 * h + (e == null ? 0 : e.hashCode());
        }
        return h;
    }

    /**
     * Compares the specified object with this list for equality.
     * <p>
     * Returns {@code true} if the given object is also a read-only list and the
     * two lists contain the same elements in the same sequence.
     * <p>
     * Implementations of this method should use {@link ReadOnlyList#listEquals}.
     *
     * @param o an object
     * @return {@code true} if the object is equal to this list
     */
    boolean equals(@Nullable Object o);

    /**
     * Returns the hash code value for this list. The hash code
     * is the result of the calculation described in {@link List#hashCode()}.
     * <p>
     * Implementations of this method should use {@link ReadOnlyList#iteratorToHashCode}.
     *
     * @return the hash code value for this set
     */
    int hashCode();

}
