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

   protected ImmutableArrayList(@Nullable Collection<? extends E> copyItems) {
       this.array = copyItems == null || copyItems.isEmpty() ? EMPTY_ARRAY : copyItems.toArray();
   }

    protected ImmutableArrayList(@Nullable ReadOnlyCollection<? extends E> copyItems) {
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
        this.array = array.clone();
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
    public static <E> ImmutableArrayList<E> of() {
        return (ImmutableArrayList<E>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableArrayList<E> of(E... elements) {
        if (elements.length == 0) {
            return (ImmutableArrayList<E>) EMPTY;
        } else {
            return new ImmutableArrayList<E>(elements);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableArrayList<E> copyOf(Iterable<? extends E> list) {
        if (list instanceof ImmutableArrayList<?>) {
            return (ImmutableArrayList<E>) list;
        }
        if (list instanceof ReadOnlyCollection<?>) {
            ReadOnlyCollection<E> c = (ReadOnlyCollection<E>) list;
            return c.isEmpty() ? (ImmutableArrayList<E>) EMPTY : new ImmutableArrayList<E>(c.asCollection());
        }
        if (list instanceof Collection<?>) {
            Collection<E> c = (Collection<E>) list;
            return c.isEmpty() ? (ImmutableArrayList<E>) EMPTY : new ImmutableArrayList<E>(c);
        }
        ArrayList<E> a = new ArrayList<>();
        list.forEach(a::add);
        return copyOf(a);
    }

}
