/*
 * @(#)ImmutableArrayList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.Spliterator;

/**
 * An immutable list.
 *
 * @param <E> element type
 * @author Werner Randelshofer
 */
public class ImmutableArrayList<E> extends AbstractReadOnlyList<E> implements ImmutableList<E>, RandomAccess {

    static final ImmutableArrayList<Object> EMPTY = new ImmutableArrayList<>(new Object[0]);

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private final Object[] array;

    public ImmutableArrayList(@Nullable Collection<? extends E> copyItems) {
        this.array = copyItems == null || copyItems.isEmpty() ? EMPTY_ARRAY : copyItems.toArray();
    }

    public ImmutableArrayList(@Nullable ReadOnlyCollection<? extends E> copyItems) {
        this.array = copyItems == null || copyItems.isEmpty() ? EMPTY_ARRAY : copyItems.toArray();
    }

    ImmutableArrayList(@NonNull Object[] a, int offset, int length) {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset = " + offset);
        }
        if (length > a.length) {
            throw new IndexOutOfBoundsException("length = " + length);
        }
        this.array = length == 0 ? EMPTY_ARRAY : new Object[length];
        System.arraycopy(a, offset, array, 0, length);
    }

    ImmutableArrayList(Object[] array) {
        this.array = array;
    }


    public void copyInto(@NonNull Object[] out, int offset) {
        System.arraycopy(array, 0, out, offset, array.length);
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0, n = array.length; i < n; i++) {
            if (array[i].equals(o)) {
                return true;
            }
        }
        return false;
    }

    public @NonNull E get(int index) {
        @SuppressWarnings("unchecked")
        E value = (E) array[index];
        return value;
    }

    public int size() {
        return array.length;
    }

    public @NonNull <T> T[] toArray(@NonNull T[] a) {
        int size = size();
        if (a.length < size) {
            @SuppressWarnings("unchecked")
            T[] t = (T[]) Arrays.copyOf(array, size, a.getClass());
            return t;
        }
        System.arraycopy(array, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new ArrayIterator<>(array);
    }

    public @NonNull Spliterator<E> spliterator() {
        return new ArrayIterator<>(array);
    }

    public @NonNull Enumerator<E> enumerator() {
        return new ArrayIterator<>(array);
    }

    @Override
    public @NonNull ImmutableList<E> readOnlySubList(int fromIndex, int toIndex) {
        return new ImmutableArrayList<E>(this.array, fromIndex, toIndex - fromIndex);
    }

    @Override
    public Object[] toArray() {
        return array.clone();
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableList<E> emptyList() {
        return (ImmutableList<E>) EMPTY;
    }


    @SafeVarargs
    @SuppressWarnings("varargs")
    public static @NonNull <T> ImmutableList<T> of(@NonNull T... items) {
        return items.length == 0 ? emptyList() : new ImmutableArrayList<>(items.clone());
    }

    public static @NonNull <T> ImmutableList<T> copyOf(Iterable<? extends T> iterable) {
        if (iterable instanceof ImmutableList) {
            @SuppressWarnings("unchecked")
            ImmutableList<T> unchecked = (ImmutableList<T>) iterable;
            return unchecked;
        } else if (iterable instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<T> unchecked = (Collection<T>) iterable;
            return new ImmutableArrayList<>(unchecked);
        } else if (iterable instanceof ReadOnlyCollection) {
            @SuppressWarnings("unchecked")
            ReadOnlyCollection<T> unchecked = (ReadOnlyCollection<T>) iterable;
            return copyOf(unchecked);
        }
        ArrayList<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }
        return list.isEmpty() ? emptyList() : new ImmutableArrayList<>(list);
    }

    public static @NonNull <T> ImmutableList<T> copyOf(ReadOnlyCollection<? extends T> collection) {
        if (collection instanceof ImmutableList) {
            @SuppressWarnings("unchecked")
            ImmutableList<T> unchecked = (ImmutableList<T>) collection;
            return unchecked;
        }
        return collection.isEmpty() ? emptyList() : new ImmutableArrayList<>(collection.asCollection());
    }


}
