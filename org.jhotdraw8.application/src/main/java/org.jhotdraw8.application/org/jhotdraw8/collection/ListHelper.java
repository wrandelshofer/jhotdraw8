/*
 * @(#)ListHelper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Arrays;

import static java.lang.Integer.max;

/**
 * Provides helper methods for lists.
 *
 * @author Werner Randelshofer
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
     * @param size           the current size of the list {@literal >= 0}
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static @NonNull Object[] grow(final int size, final int targetCapacity, final int itemSize, @NonNull final Object[] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength, items.getClass());
    }

    /**
     * Grows an items array.
     *
     * @param size           the current size of the list {@literal >= 0}
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static @NonNull double[] grow(final int size, final int targetCapacity, final int itemSize, @NonNull final double[] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Grows an items array.
     *
     * @param size           the current size of the list {@literal >= 0}
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static @NonNull short[] grow(final int size, final int targetCapacity, final int itemSize, @NonNull final short[] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Grows an items array.
     *
     * @param size           the current size of the list {@literal >= 0}
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static @NonNull int[] grow(final int size, final int targetCapacity, final int itemSize, @NonNull final int[] items) {
        if (targetCapacity * itemSize <= items.length) {
            return items;
        }
        int newLength = max(targetCapacity * itemSize, items.length * 2);
        return Arrays.copyOf(items, newLength);
    }

    /**
     * Grows an items array.
     *
     * @param size           the current size of the list {@literal >= 0}
     * @param targetCapacity {@literal >= 0}
     * @param itemSize       number of array elements that an item occupies
     * @param items          the items array
     * @return a new item array of larger size or the same if no resizing is necessary
     */
    public static @NonNull long[] grow(final int size, final int targetCapacity, final int itemSize, @NonNull final long[] items) {
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
    public static @NonNull char[] grow(final int size, final int targetCapacity, final int itemSize, @NonNull final char[] items) {
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
    public static @NonNull Object[] trimToSize(final int size, final int itemSize, @NonNull final Object[] items) {
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
    public static @NonNull int[] trimToSize(final int size, final int itemSize, @NonNull final int[] items) {
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
    public static @NonNull long[] trimToSize(final int size, final int itemSize, @NonNull final long[] items) {
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
    public static @NonNull double[] trimToSize(final int size, final int itemSize, @NonNull final double[] items) {
        int newLength = size * itemSize;
        if (items.length == newLength) {
            return items;
        }
        return Arrays.copyOf(items, newLength);
    }
}
