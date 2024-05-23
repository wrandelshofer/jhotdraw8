/*
 * @(#)LongQueue.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import org.jspecify.annotations.Nullable;

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
    default boolean addAll(Collection<? extends Long> c) {
        boolean added = false;
        for (Long e : c) {
            added |= add(e);
        }
        return added;
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        boolean removed = false;
        for (Object e : c) {
            removed |= remove(e);
        }
        return removed;
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        final LongArrayList tmp = new LongArrayList(size());
        ((Spliterator.OfLong) spliterator())
                .forEachRemaining((LongConsumer) tmp::addAsLong);
        clear();
        boolean removed = false;
        for (final var i = tmp.iterator(); i.hasNext(); ) {
            long current = i.nextLong();
            if (c.contains(current)) {
                addAsLong(current);
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

    /**
     * @see Queue#remove(Object)
     */
    boolean removeAsLong(long e);

    @Override
    default boolean remove(Object e) {
        return e instanceof Long && removeAsLong((Long) e);
    }

    @Override
    default @Nullable Long poll() {
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
    default @Nullable Long peek() {
        return isEmpty() ? null : elementAsLong();
    }


    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    boolean containsAsLong(long e);

    @Override
    default boolean contains(Object e) {
        return (e instanceof Long) && containsAsLong((Long) e);
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }
}
