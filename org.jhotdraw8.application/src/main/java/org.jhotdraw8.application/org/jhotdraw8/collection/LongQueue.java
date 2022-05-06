/*
 * @(#)LongQueue.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.LongConsumer;

/**
 * Interface for a {@link Queue} with primitive long data elements.
 */
public interface LongQueue extends Queue<Long> {
    /**
     * @see Queue#add
     */
    boolean addAsLong(long e);

    @Override
    default boolean add(Long e) {
        return addAsLong(e);
    }

    @Override
    default boolean addAll(@NonNull Collection<? extends Long> c) {
        boolean added = false;
        for (Long e : c) {
            added |= add(e);
        }
        return added;
    }

    @Override
    default boolean removeAll(@NonNull Collection<?> c) {
        boolean removed = false;
        for (Object e : c) {
            removed |= remove(e);
        }
        return removed;
    }

    @Override
    default boolean retainAll(@NonNull Collection<?> c) {
        final LongArrayList tmp = new LongArrayList(size());
        ((Spliterator.OfLong) spliterator())
                .forEachRemaining((LongConsumer) tmp::addAsLong);
        clear();
        boolean removed = false;
        for (final LongEnumerator i = tmp.enumerator(); i.moveNext(); ) {
            if (c.contains(i.current())) {
                addAsLong(i.currentAsLong());
            } else {
                removed = true;
            }
        }
        return removed;
    }

    /**
     * @see Queue#offer
     */
    boolean offerAsLong(long e);

    @Override
    default boolean offer(Long e) {
        return offerAsLong(e);
    }

    /**
     * @see Queue#remove
     */
    long removeAsLong();

    @Override
    default Long remove() {
        return removeAsLong();
    }

    boolean removeAsLong(long e);

    @Override
    default boolean remove(Object e) {
        return e instanceof Long && removeAsLong((Long) e);
    }

    @Override
    default Long poll() {
        return isEmpty() ? null : removeAsLong();
    }

    @Override
    default Long element() {
        return elementAsLong();
    }

    /**
     * @see Queue#element
     */
    long elementAsLong();

    @Override
    default Long peek() {
        return isEmpty() ? null : elementAsLong();
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    boolean containsAsLong(long e);

    @Override
    default boolean contains(Object e) {
        return (e instanceof Long) && containsAsLong((Long) e);
    }

    @Override
    default boolean containsAll(@NonNull Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }
}
