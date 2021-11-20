/*
 * <Project / Product>
 * Copyright (c) 2021 Siemens Mobility AG.
 * All rights reserved
 * confidential
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import static java.lang.Integer.max;

/**
 * Provides helper methods for lists.
 */
public class ListHelper {
    /**
     * Don't let anyone instantiate this class.
     */
    private ListHelper() {

    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static Object[] grow(final int size, final int targetCapacity, final int itemSize, @Nullable final Object[] items) {
        if (items == null) {
            return new Object[targetCapacity * itemSize];
        }
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newCapacity = max(targetCapacity, items.length + items.length / 2); // grow by 50%
        Object[] newItems = new Object[newCapacity * itemSize];
        System.arraycopy(items, 0, newItems, 0, size * itemSize);
        return newItems;
    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static double[] grow(final int size, final int targetCapacity, final int itemSize, @Nullable final double[] items) {
        if (items == null) {
            return new double[targetCapacity * itemSize];
        }
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newCapacity = max(targetCapacity, items.length + items.length / 2); // grow by 50%
        double[] newItems = new double[newCapacity * itemSize];
        System.arraycopy(items, 0, newItems, 0, size * itemSize);
        return newItems;
    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static int[] grow(final int size, final int targetCapacity, final int itemSize, @Nullable final int[] items) {
        if (items == null) {
            return new int[targetCapacity * itemSize];
        }
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newCapacity = max(targetCapacity, items.length + items.length / 2); // grow by 50%
        int[] newItems = new int[newCapacity * itemSize];
        System.arraycopy(items, 0, newItems, 0, size * itemSize);
        return newItems;
    }

    /**
     * Shrink an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static Object[] shrink(final int size, final int targetCapacity, final int itemSize, @NonNull final Object[] items) {
        if (targetCapacity == 0) {
            return null;
        }
        if (targetCapacity * itemSize < items.length / 8) {
            return items;
        }
        // shrink to load factor 50%
        int newCapacity = max(targetCapacity, targetCapacity * 2);
        Object[] newItems = new Object[newCapacity * itemSize];
        System.arraycopy(items, 0, newItems, 0, size * itemSize);
        return newItems;
    }

    /**
     * Resizes an array to fit the number of items.
     *
     * @param itemSize number of array elements that an item occupies
     * @param items    the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static Object[] sizeToFit(final int size, final int itemSize, @NonNull final Object[] items) {
        if (size == 0) {
            return null;
        }
        if (items.length == size * itemSize) {
            return items;
        }
        Object[] newItems = new Object[size * itemSize];
        System.arraycopy(items, 0, newItems, 0, size * itemSize);
        return newItems;
    }

    /**
     * Shrink an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static double[] shrink(final int targetCapacity, final int itemSize, @NonNull final double[] items) {
        if (targetCapacity == 0) {
            return null;
        }
        if (targetCapacity * itemSize < items.length / 8) {
            return items;
        }
        // shrink to load factor 50%
        int newCapacity = max(targetCapacity, items.length / 4);
        double[] newItems = new double[newCapacity * itemSize];
        System.arraycopy(items, 0, newItems, 0, items.length);
        return newItems;
    }

    /**
     * Shrink an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static int[] shrink(final int targetCapacity, final int itemSize, @NonNull final int[] items) {
        if (targetCapacity == 0) {
            return null;
        }
        if (targetCapacity * itemSize < items.length / 8) {
            return items;
        }
        int newCapacity = max(targetCapacity, items.length / 2); // shrink by 50%
        int[] newItems = new int[newCapacity * itemSize];
        System.arraycopy(items, 0, newItems, 0, items.length);
        return newItems;
    }


}
