/*
 * @(#)ImmutableArraySubList.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.Spliterator;

/**
 * An immutable sub list.
 * <p>
 * This list is package private. It has more overhead than {@link ImmutableList}
 * and is only created if necessary.
 *
 * @param <E> element type
 * @author Werner Randelshofer
 */
final class ImmutableArraySubList<E> extends AbstractReadOnlyList<E> implements ImmutableList<E>, RandomAccess {

    private final @NonNull Object[] array;
    private final int size;
    private final int offset;

    ImmutableArraySubList(@NonNull Object[] array, int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > array.length) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
        }
        this.offset = fromIndex;
        this.size = toIndex - fromIndex;
        this.array = array;
    }


    public void copyInto(@NonNull Object[] out, int offset) {
        System.arraycopy(array, 0, out, offset, array.length);
    }

    @Override
    public boolean contains(Object o) {
        for (int i = offset, n = offset + size; i < n; i++) {
            if (array[i].equals(o)) {
                return true;
            }
        }
        return false;
    }

    public @NonNull E get(int index) {
        @SuppressWarnings("unchecked")
        E value = (E) array[offset + index];
        return value;
    }

    public int size() {
        return size;
    }

    public @NonNull <T> T[] toArray(@NonNull T[] a) {
        if (a.length < size) {
            @SuppressWarnings("unchecked")
            T[] t = (T[]) Arrays.copyOf(array, size, a.getClass());
            return t;
        }
        System.arraycopy(array, offset, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }


    public @NonNull Spliterator<E> spliterator() {
        return new ArrayIterator<>(array, offset, offset + size);
    }

    public @NonNull Iterator<E> iterator() {
        return new ArrayIterator<>(array, offset, offset + size);
    }

    @Override
    public @NonNull ImmutableList<E> readOnlySubList(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > size) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
        }

        return new ImmutableArraySubList<>(this.array, offset + fromIndex, offset + toIndex);
    }

    @Override
    public @NonNull Object[] toArray() {
        Object[] dest = new Object[size];
        System.arraycopy(array, offset, dest, 0, size);
        return dest;
    }
}
