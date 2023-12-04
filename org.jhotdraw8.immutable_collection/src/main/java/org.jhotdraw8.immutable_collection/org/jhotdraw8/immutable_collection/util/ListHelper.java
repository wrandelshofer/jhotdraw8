/*
 * @(#)ListHelper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.immutable_collection.util;

import org.jhotdraw8.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Provides static methods for lists that are based on arrays.
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
     * Copies 'src' and inserts 'numComponents' at position 'index'.
     * <p>
     * The new components will have a null value.
     *
     * @param src           an array
     * @param index         an index
     * @param numComponents the number of array components to be added
     * @param <T>           the array type
     * @return a new array
     */
    public static <T> @NonNull T @NonNull [] copyComponentAdd(@NonNull T @NonNull [] src, int index, int numComponents) {
        if (index == src.length) {
            return Arrays.copyOf(src, src.length + numComponents);
        }
        @SuppressWarnings("unchecked") final T[] dst = (T[]) Array.newInstance(src.getClass().getComponentType(), src.length + numComponents);
        System.arraycopy(src, 0, dst, 0, index);
        System.arraycopy(src, index, dst, index + numComponents, src.length - index);
        return dst;
    }

    /**
     * Copies 'src' and removes 'numComponents' at position 'index'.
     *
     * @param src           an array
     * @param index         an index
     * @param numComponents the number of array components to be removed
     * @param <T>           the array type
     * @return a new array
     */
    public static <T> @NonNull T @NonNull [] copyComponentRemove(@NonNull T @NonNull [] src, int index, int numComponents) {
        if (index == src.length - numComponents) {
            return Arrays.copyOf(src, src.length - numComponents);
        }
        @SuppressWarnings("unchecked") final T[] dst = (T[]) Array.newInstance(src.getClass().getComponentType(), src.length - numComponents);
        System.arraycopy(src, 0, dst, 0, index);
        System.arraycopy(src, index + numComponents, dst, index, src.length - index - numComponents);
        return dst;
    }

    /**
     * Copies 'src' and sets 'value' at position 'index'.
     *
     * @param src   an array
     * @param index an index
     * @param value a value
     * @param <T>   the array type
     * @return a new array
     */
    public static <T> @NonNull T @NonNull [] copySet(@NonNull T @NonNull [] src, int index, T value) {
        final T[] dst = Arrays.copyOf(src, src.length);
        dst[index] = value;
        return dst;
    }

}
