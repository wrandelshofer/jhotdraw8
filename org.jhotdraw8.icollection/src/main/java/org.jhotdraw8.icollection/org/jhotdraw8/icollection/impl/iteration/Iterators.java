/*
 * @(#)Iterators.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.iteration;

import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides static utility methods for iterators.
 *
 * @author Werner Randelshofer
 */
public class Iterators {

    /**
     * Don't let anyone instantiate this class.
     */
    private Iterators() {
    }

    /**
     * Creates a list from an {@code Iterable}.
     * If the {@code Iterable} is a list, it is returned.
     *
     * @param <T>      the value type
     * @param iterable the iterable
     * @return the list
     */
    public static @NonNull <T> List<T> toList(@NonNull Iterable<T> iterable) {
        if (iterable instanceof List<?>) {
            return (List<T>) iterable;
        }
        ArrayList<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    public static <E> @NonNull Iterator<E> unmodifiableIterator(@NonNull Iterator<E> iterator) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next();
            }
        };
    }
}
