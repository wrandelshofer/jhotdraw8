/*
 * @(#)GrowableIntSet8Bit.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.primitive;

/**
 * Extension of {@link DenseIntSet8Bit} that can be used, when
 * the size of the set is not known.
 */
public class GrowableIntSet8Bit extends DenseIntSet8Bit {
    public GrowableIntSet8Bit() {
        super(0);
    }

    /**
     * Adds an element to the set.
     * <p>
     * Automatically increases the capacity of the set if needed.
     *
     * @param element the element
     * @return true if the element was added, false if it was already in the set.
     */
    @Override
    public boolean addAsInt(int element) {
        ensureCapacity(element);
        return super.addAsInt(element);
    }

    /**
     * Removes the specified element from the set.
     * <p>
     * Automatically increases the capacity of the set if needed.
     *
     * @param element an element
     * @return true if the element was in the set, false otherwise
     */
    @Override
    public boolean removeAsInt(int element) {
        ensureCapacity(element);
        return super.removeAsInt(element);
    }

    /**
     * Checks if the set contains the specified element.
     * <p>
     * Automatically increases the capacity of the set if needed.
     *
     * @param element an element
     * @return true if the element is in the set.
     * @throws ArrayIndexOutOfBoundsException if element is outside of the
     *                                        capacity range.
     */
    @Override
    public boolean containsAsInt(int element) {
        ensureCapacity(element);
        return super.containsAsInt(element);
    }

    private void ensureCapacity(int index) {
        if (capacity() < index) {
            setCapacity(Integer.highestOneBit(index + index - 1));
        }
    }


}
