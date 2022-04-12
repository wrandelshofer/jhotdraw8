/*
 * @(#)IntList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.util.List;

/**
 * Interface for a {@link List} with a primitive integer data elements.
 */
public interface IntList extends List<Integer> {
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
     * @See List#lastIndexOf(Object)
     */
    int lastIndexOfAsInt(int o);

    /**
     * @See List#indexOf(Object)
     */
    int indexOfAsInt(int o);

    /**
     * @see List#add(Object)
     */
    void addAsInt(int e);

    /**
     * @see ReadOnlySequencedCollection#getFirst()
     */
    int getFirstAsInt();

    /**
     * @see ReadOnlySequencedCollection#getLast()
     */
    int getLastAsInt();
}