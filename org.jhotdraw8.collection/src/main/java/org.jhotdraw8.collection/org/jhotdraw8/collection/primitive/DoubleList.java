/*
 * @(#)IntList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

import java.util.List;

/**
 * Interface for a {@link List} with a primitive integer data elements.
 */
public interface DoubleList extends List<Double>, DoubleSequencedCollection {
    @Override
    default boolean add(Double e) {
        addAsDouble(e);
        return true;
    }

    @Override
    default int indexOf(Object o) {
        return indexOfAsDouble((Double) o);
    }

    @Override
    default int lastIndexOf(Object o) {
        return lastIndexOfAsDouble((Double) o);
    }

    /**
     * @see List#lastIndexOf(Object)
     */
    int lastIndexOfAsDouble(double o);

    /**
     * @see List#indexOf(Object)
     */
    int indexOfAsDouble(double o);

    /**
     * @see List#add(Object)
     */
    void addAsDouble(double e);

    /**
     * @see List#add(int, Object)
     */
    void addAsDouble(int index, double e);

    /**
     * @see List#get(int)
     */
    double getAsDouble(int index);

    @Override
    default void addFirstAsDouble(double e) {
        addAsDouble(0, e);
    }

    @Override
    default void addLastAsDouble(double e) {
        addAsDouble(size(), e);
    }

    /**
     * Removes the item at the specified index from this list.
     *
     * @param index an index
     * @return the removed item
     */
    double removeAtAsDouble(int index);

    @Override
    default double removeFirstAsDouble() {
        final double e = getAsDouble(0);
        removeAtAsDouble(0);
        return e;
    }


    @Override
    default boolean isEmpty() {
        return size() == 0;
    }
}