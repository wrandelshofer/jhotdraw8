/*
 * @(#)SequencedCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.immutable_collection.sequenced;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Interface for a collection with a well-defined iteration order.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public interface SequencedCollection<E> extends Collection<E> {
    /**
     * Gets the first element.
     *
     * @return an element
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    default E getFirst() {
        return iterator().next();
    }

    /**
     * Gets the last element.
     *
     * @return an element
     * @throws java.util.NoSuchElementException if the collection is empty
     */
    default E getLast() {
        return _reversed().iterator().next();
    }

    /**
     * Adds the specified element at the front of this collection.
     *
     * @param e an element
     */
    void addFirst(E e);

    /**
     * Adds the specified element at the end of this collection.
     *
     * @param e an element
     */
    void addLast(E e);

    /**
     * Returns a reversed-order view of this collection.
     * <p>
     * Modifications write through to the underlying collection.
     * Changes to the underlying collection are visible in the reversed view.
     *
     * @return a reversed-order view of this collection
     */
    @NonNull SequencedCollection<E> _reversed();

    /**
     * Removes the element at the front of this collection.
     *
     * @return the first element
     * @throws NoSuchElementException if this set is empty
     */
    default E removeFirst() {
        Iterator<E> i = iterator();
        E e = i.next();
        i.remove();
        return e;
    }

    /**
     * Removes the element at the end of this collection.
     *
     * @return the last element
     * @throws NoSuchElementException if this set is empty
     */
    default E removeLast() {
        Iterator<E> i = _reversed().iterator();
        E e = i.next();
        i.remove();
        return e;
    }
}
