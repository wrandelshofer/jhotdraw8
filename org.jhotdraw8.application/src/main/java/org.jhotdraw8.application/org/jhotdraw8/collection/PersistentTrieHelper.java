package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Helper class for persistent tries.
 */
class PersistentTrieHelper {
    /**
     * Don't let anyone instantiate this class.
     */
    private PersistentTrieHelper() {
    }

    /**
     * Copies 'src' and inserts 'value' at position 'index'.
     *
     * @param src   an array
     * @param index an index
     * @param value a value
     * @param <T>   the array type
     * @return a new array
     */
    public static <T> @NonNull T[] copyAdd(@NonNull T[] src, int index, T value) {
        final T[] dst = copyComponentAdd(src, index, 1);
        dst[index] = value;
        return dst;
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
    public static <T> @NonNull T[] copyComponentAdd(@NonNull T[] src, int index, int numComponents) {
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
    public static <T> @NonNull T[] copyComponentRemove(@NonNull T[] src, int index, int numComponents) {
        if (index == src.length - numComponents) {
            return Arrays.copyOf(src, src.length - numComponents);
        }
        @SuppressWarnings("unchecked") final T[] dst = (T[]) Array.newInstance(src.getClass().getComponentType(), src.length - numComponents);
        System.arraycopy(src, 0, dst, 0, index);
        System.arraycopy(src, index + numComponents, dst, index, src.length - index - numComponents);
        return dst;
    }

    /**
     * Copies 'src' and removes one component at position 'index'.
     *
     * @param src   an array
     * @param index an index
     * @param <T>   the array type
     * @return a new array
     */
    public static <T> @NonNull T[] copyRemove(@NonNull T[] src, int index) {
        return copyComponentRemove(src, index, 1);
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
    public static <T> @NonNull T[] copySet(@NonNull T[] src, int index, T value) {
        final T[] dst = Arrays.copyOf(src, src.length);
        dst[index] = value;
        return dst;
    }

    enum SizeClass {
        SIZE_EMPTY,
        SIZE_ONE,
        SIZE_MORE_THAN_ONE
    }

    /**
     * A unique key. Each instance is unique in this JVM.
     */
    static class UniqueKey implements Serializable {
        private final static long serialVersionUID = 0L;
    }
}
