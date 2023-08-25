/*
 * @(#)ReadOnlyCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.readonly;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.facade.CollectionFacade;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Read-only interface for a collection. The state
 * of the collection may change.
 * <p>
 * This interface does not guarantee 'read-only', it actually guarantees
 * 'readable'. We use the prefix 'ReadOnly' because this is the naming
 * convention in JavaFX for interfaces that provide read methods but no write methods.
 *
 * @param <E> the element type
 */
public interface ReadOnlyCollection<E> extends Iterable<E> {

    /**
     * Returns the maximal size of this collection.
     * <p>
     * Attempting to add more elements to this collection will result
     * in a {@link SizeLimitExceededException}.
     * <p>
     * The default implementation provided by this interface
     * returns {@link Integer#MAX_VALUE} {@code - 16}.
     * This is the largest supported array size on a JVM when
     * pointers are not compressed.
     *
     * @return the maximal size
     */
    default long maxSize() {
        return Integer.MAX_VALUE - 16;
    }


    /**
     * Returns the size of the collection.
     * <p>
     * If this collection contains more than {@link Integer#MAX_VALUE}
     * elements, returns {@link Integer#MAX_VALUE}.
     *
     * @return the size
     */
    int size();

    /**
     * Returns the size of the collection as a long value.
     * <p>
     * The default implementation provided by this interface
     * returns {@link #size()}.
     *
     * @return the size
     */
    default long sizeAsLong() {
        return size();
    }

    /**
     * Returns true if the collection is empty.
     *
     * @return true if empty
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    default Object @NonNull [] toArray() {
        return toArray(new Object[size()]);
    }

    /**
     * Converts the collection to an array.
     *
     * @param <T> the element type
     * @param a   a template array
     * @return an array
     */
    default @NonNull <T> T @NonNull [] toArray(@NonNull T @NonNull [] a) {
        // Estimate size of array; be prepared to see more or fewer elements
        int size = size();
        @SuppressWarnings("unchecked")
        T[] r = a.length >= size ? a : (T[]) Arrays.copyOf(a, size, a.getClass());
        Iterator<E> it = iterator();

        for (int i = 0; i < r.length; i++) {
            if (!it.hasNext()) { // fewer elements than expected
                throw new ConcurrentModificationException("fewer elements than expected. expected=" + size);
            }
            @SuppressWarnings("unchecked")
            T t = (T) it.next();
            r[i] = t;
        }
        if (it.hasNext()) {
            throw new ConcurrentModificationException("more elements than expected. expected=" + size);
        }
        return r;
    }

    /**
     * Returns a stream.
     *
     * @return a stream
     */
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns {@code true} if this collection contains the specified object.
     *
     * @param o an object
     * @return {@code true} if this collection contains the specified
     * object
     */
    boolean contains(Object o);

    /**
     * Returns an iterator over the elements in this collection.
     *
     * @return an iterator
     */
    @Override
    @NonNull
    Iterator<E> iterator();

    /**
     * Returns true if this collection contains all elements of that collection.
     *
     * @param c another collection
     * @return true if this collection contains all of c
     */
    default boolean containsAll(@NonNull Iterable<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Wraps this collection in the Collection interface - without copying.
     *
     * @return the wrapped collection
     */
    default @NonNull Collection<E> asCollection() {
        return new CollectionFacade<>(this);
    }

    /**
     * Returns a string representation of the provided iterable.  The string
     * representation consists of a list of the iterable's elements in the
     * order they are returned, enclosed in square brackets
     * ({@code "[]"}).  Adjacent elements are separated by the characters
     * {@code ", "} (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @param c   an iterable
     * @param <E> the element type
     * @return a string representation of the iterable
     */
    static <E> @NonNull String iterableToString(final @NonNull Iterable<E> c) {
        Iterator<E> it = c.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            E e = it.next();
            sb.append(e == c ? "(this Collection)" : e);
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }
}
