/*
 * @(#)SequencedSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Interface for a set with a well-defined linear ordering of its elements.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public interface SequencedSet<E> extends Set<E>, SequencedCollection<E> {

    /**
     * Removes the element at the front of the set.
     *
     * @return the front element
     * @throws NoSuchElementException if this set is empty
     */
    default E removeFirst() {
        Iterator<E> iterator = iterator();
        E e = iterator.next();
        iterator.remove();
        return e;
    }

    /**
     * Removes the element at the tail of the set.
     *
     * @return the tail element
     * @throws NoSuchElementException if this set is empty
     */
    E removeLast();

    @Override
    default Object @NonNull [] toArray() {
        return SequencedCollection.super.toArray();
    }

    @Override
    default <T> T @NonNull [] toArray(T @NonNull [] a) {
        return SequencedCollection.super.toArray(a);
    }

    @Override
    default boolean isEmpty() {
        return SequencedCollection.super.isEmpty();
    }
}
