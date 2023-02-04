/*
 * @(#)IntList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import java.util.List;

/**
 * Interface for a {@link List} with a primitive integer data elements.
 */
public interface ByteList extends List<Byte>, ByteSequencedCollection {
    @Override
    default boolean add(Byte e) {
        addAsByte(e);
        return true;
    }

    @Override
    default int indexOf(Object o) {
        return indexOfAsByte((Byte) o);
    }

    @Override
    default int lastIndexOf(Object o) {
        return lastIndexOfAsByte((Byte) o);
    }

    /**
     * @see List#lastIndexOf(Object)
     */
    int lastIndexOfAsByte(byte o);

    /**
     * @see List#indexOf(Object)
     */
    int indexOfAsByte(byte o);

    /**
     * @see List#add(Object)
     */
    void addAsByte(byte e);

    /**
     * @see List#add(int, Object)
     */
    void addAsByte(int index, byte e);

    /**
     * @see List#get(int)
     */
    byte getAsByte(int index);

    @Override
    default void addFirstAsByte(byte e) {
        addAsByte(0, e);
    }

    @Override
    default void addLastAsByte(byte e) {
        addAsByte(size(), e);
    }

    /**
     * Removes the item at the specified index from this list.
     *
     * @param index an index
     * @return the removed item
     */
    byte removeAtAsByte(int index);

    @Override
    default byte removeFirstAsByte() {
        final byte e = getAsByte(0);
        removeAtAsByte(0);
        return e;
    }


    @Override
    default boolean isEmpty() {
        return size() == 0;
    }
}