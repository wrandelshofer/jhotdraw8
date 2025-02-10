/*
 * @(#)ListHelper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.base.io;


import java.util.Arrays;

import static java.lang.Integer.max;

/**
 * Provides helper methods for lists.
 *
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
    public static Object[] grow(final int targetCapacity, final int itemSize, final Object[] items) {
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
    public static double[] grow(final int targetCapacity, final int itemSize, final double[] items) {
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
    public static short[] grow(final int targetCapacity, final int itemSize, final short[] items) {
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
    public static int[] grow(final int targetCapacity, final int itemSize, final int[] items) {
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
    public static long[] grow(final int targetCapacity, final int itemSize, final long[] items) {
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
    public static char[] grow(final int targetCapacity, final int itemSize, final char[] items) {
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
    public static Object[] trimToSize(final int size, final int itemSize, final Object[] items) {
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
    public static int[] trimToSize(final int size, final int itemSize, final int[] items) {
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
    public static long[] trimToSize(final int size, final int itemSize, final long[] items) {
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
    public static double[] trimToSize(final int size, final int itemSize, final double[] items) {
        int newLength = size * itemSize;
        if (items.length == newLength) {
            return items;
        }
        return Arrays.copyOf(items, newLength);
    }
}
