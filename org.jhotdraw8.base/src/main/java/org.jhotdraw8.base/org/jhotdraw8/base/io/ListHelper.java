/*
 * @(#)ListHelper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.base.io;

import org.jhotdraw8.annotation.NonNull;

import java.util.Arrays;

import static java.lang.Integer.max;

/**
 * Provides helper methods for lists.
 *
 * @author Werner Randelshofer
 */
class ListHelper {
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
    public static @NonNull Object @NonNull [] grow(final int targetCapacity, final int itemSize, @NonNull final Object @NonNull [] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength, items.getClass());
    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static double @NonNull [] grow(final int targetCapacity, final int itemSize, final double @NonNull [] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static short @NonNull [] grow(final int targetCapacity, final int itemSize, final short @NonNull [] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static int @NonNull [] grow(final int targetCapacity, final int itemSize, final int @NonNull [] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static long @NonNull [] grow(final int targetCapacity, final int itemSize, final long @NonNull [] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Grows an items array.
     *
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static char @NonNull [] grow(final int targetCapacity, final int itemSize, final char @NonNull [] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }


    /**
     * Resizes an array to fit the number of items.
     *
     * @param size     the size to fit
     * @param itemSize number of array elements that an item occupies
     * @param items    the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static @NonNull Object @NonNull [] trimToSize(final int size, final int itemSize, @NonNull final Object @NonNull [] items) {
        int newLength = size * itemSize;
        if (items.length == newLength) {
            return items;
        }
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Resizes an array to fit the number of items.
     *
     * @param size     the size to fit
     * @param itemSize number of array elements that an item occupies
     * @param items    the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static int @NonNull [] trimToSize(final int size, final int itemSize, final int @NonNull [] items) {
        int newLength = size * itemSize;
        if (items.length == newLength) {
            return items;
        }
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Resizes an array to fit the number of items.
     *
     * @param size     the size to fit
     * @param itemSize number of array elements that an item occupies
     * @param items    the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static long @NonNull [] trimToSize(final int size, final int itemSize, final long @NonNull [] items) {
        int newLength = size * itemSize;
        if (items.length == newLength) {
            return items;
        }
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Resizes an array to fit the number of items.
     *
     * @param size     the size to fit
     * @param itemSize number of array elements that an item occupies
     * @param items    the items array
     * @return a new item array of smaller size or the same if no resizing is necessary
     */
    public static double @NonNull [] trimToSize(final int size, final int itemSize, final double @NonNull [] items) {
        int newLength = size * itemSize;
        if (items.length == newLength) {
            return items;
        }
        return Arrays.copyOf(items, newLength);
    }
}
