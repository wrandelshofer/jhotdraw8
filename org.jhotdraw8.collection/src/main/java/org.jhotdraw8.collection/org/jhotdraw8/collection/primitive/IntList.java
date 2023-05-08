/*
 * @(#)IntList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import java.util.List;

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
        return size() == 0;
    }

    @Override
    default Integer removeFirst() {
        return IntSequencedCollection.super.removeFirst();
    }

    @Override
    default Integer removeLast() {
        return IntSequencedCollection.super.removeLast();
    }

    @Override
    default Integer getFirst() {
        return IntSequencedCollection.super.getFirst();
    }

    @Override
    default Integer getLast() {
        return IntSequencedCollection.super.getLast();
    }

    @Override
    default void addFirst(Integer e) {
        IntSequencedCollection.super.addFirst(e);
    }

    @Override
    default void addLast(Integer e) {
        IntSequencedCollection.super.addLast(e);
    }
}