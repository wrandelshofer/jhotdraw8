/*
 * @(#)IntList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for a {@link List} with a primitive integer data elements.
 */
public interface IntList extends List<Integer>, IntSequencedCollection {
    @Override
    default boolean add(Integer integer) {
        addAsInt(integer);
        return true;
    }

    @Override
    default int indexOf(Object o) {
        return indexOfAsInt((Integer) o);
    }

    @Override
    default int lastIndexOf(Object o) {
        return lastIndexOfAsInt((Integer) o);
    }

    /**
     * @see List#lastIndexOf(Object)
     */
    int lastIndexOfAsInt(int o);

    /**
     * @see List#indexOf(Object)
     */
    int indexOfAsInt(int o);

    /**
     * @see List#add(Object)
     */
    void addAsInt(int e);

    /**
     * @see List#add(int, Object)
     */
    void addAsInt(int index, int e);

    /**
     * @see List#get(int)
     */
    int getAsInt(int index);

    @Override
    default void addFirstAsInt(int e) {
        addAsInt(0, e);
    }

    @Override
    default void addLastAsInt(int e) {
        addAsInt(size(), e);
    }

    /**
     * Removes the item at the specified index from this list.
     *
     * @param index an index
     * @return the removed item
     */
    int removeAtAsInt(int index);

    @Override
    default int removeFirstAsInt() {
        final int e = getAsInt(0);
        removeAtAsInt(0);
        return e;
    }


    @Override
    default boolean isEmpty() {
        return IntSequencedCollection.super.isEmpty();
    }

    @Override
    default <T> T @NonNull [] toArray(T @NonNull [] a) {
        return IntSequencedCollection.super.toArray(a);
    }

    @Override
    default Object @NonNull [] toArray() {
        return IntSequencedCollection.super.toArray();
    }

    @Override
    default Stream<Integer> stream() {
        return IntSequencedCollection.super.stream();
    }
}