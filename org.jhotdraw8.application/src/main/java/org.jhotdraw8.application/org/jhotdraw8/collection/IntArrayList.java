/*
 * @(#)IntArrayList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.util.Preconditions;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * A lightweight int array list implementation for performance critical code.
 *
 * @author Werner Randelshofer
 */
public class IntArrayList extends AbstractList<Integer> implements IntList {
    private final static int[] EMPTY = new int[0];
    private int[] items;

    /**
     * Holds the size of the list. Invariant: size >= 0.
     */
    private int size;

    /**
     * Creates a new empty instance with 0 initial capacity.
     */
    public IntArrayList() {
        items = EMPTY;
    }

    /**
     * Creates a new empty instance with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public IntArrayList(int initialCapacity) {
        items = new int[initialCapacity];
    }

    /**
     * Creates a new instance from the specified collection
     *
     * @param collection a collection of integers
     */
    public IntArrayList(@NonNull Collection<Integer> collection) {
        this.size = collection.size();
        this.items = new int[size];

        int count = 0;
        //noinspection ForLoopReplaceableByForEach
        for (Iterator<Integer> iter = collection.iterator(); iter.hasNext(); ) {
            Integer value = iter.next();
            items[count++] = value;
        }
    }

    private IntArrayList(@NonNull int[] items) {
        this.items = items;
        this.size = items.length;
    }

    /**
     * Creates a new instance with the specified items.
     *
     * @param items the items (the newly created instance references the
     *              provided array)
     * @return the new instance
     */
    public static @NonNull IntArrayList of(@NonNull int... items) {
        return new IntArrayList(items);
    }

    /**
     * Adds a new item to the end of the list.
     *
     * @param newItem the new item
     */
    public void addAsInt(int newItem) {
        grow(size + 1);
        items[size++] = newItem;
    }

    /**
     * Inserts a new item at the specified index into this list.
     *
     * @param index   the index
     * @param newItem the new item
     */
    public void addAsInt(int index, int newItem) {
        Preconditions.checkIndex(index, size + 1);
        grow(size + 1);
        items[index] = newItem;
        ++size;
    }

    /**
     * Adds all items of the specified list to this list.
     *
     * @param that another list
     */
    public void addAllAsInt(@NonNull IntArrayList that) {
        if (that.isEmpty()) {
            return;
        }
        grow(size + that.size);
        System.arraycopy(that.items, 0, this.items, this.size, that.size);
        this.size += that.size;
    }

    /**
     * Adds all items of this collection to the specified collection.
     *
     * @param <T> the type of the collection
     * @param out the output collection
     * @return out
     */
    public @NonNull <T extends Collection<Integer>> T addAllInto(@NonNull T out) {
        for (int i = 0, n = size; i < n; i++) {
            out.add(items[i]);
        }
        return out;
    }

    /**
     * Clears the list in O(1).
     */
    public void clear() {
        // Performance: do not fill array with 0 values
        size = 0;
    }

    /**
     * Copies the contents of this list into the provided array.
     *
     * @param a      an array
     * @param offset the offset into the array
     */
    public void copyInto(@NonNull int[] a, int offset) {
        System.arraycopy(items, 0, a, offset, size);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        // FIXME this is not correct since we implement List<Integer>
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntArrayList other = (IntArrayList) obj;
        if (other.size != this.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (other.items[i] != this.items[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the item at the specified index.
     *
     * @param index an index
     * @return the item at the index
     */
    public int getAsInt(int index) {
        Preconditions.checkIndex(index, size);
        return items[index];
    }

    /*
     * Gets the item at the specified index.
     *
     * @param index an index
     * @return the item at the index
     */
    @Override
    public Integer get(int index) {
        Preconditions.checkIndex(index, size);
        return items[index];
    }

    public int getLastAsInt() {
        return getAsInt(size - 1);
    }

    public int getFirstAsInt() {
        return getAsInt(0);
    }

    /**
     * Sets the size of this list. If the new size is greater than the current
     * size, new {@code 0} items are added to the end of the list. If the new
     * size is is less than the current size, all items at indices greater or
     * equal {@code newSize} are discarded.
     *
     * @param newSize the new size
     */
    public void setSize(int newSize) {
        grow(newSize);
        if (newSize > size) {
            Arrays.fill(items, size, newSize, 0);
        }
        size = newSize;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            result = 31 * result + items[i];
        }

        return result;
    }

    private void grow(int capacity) {
        if (items.length < capacity) {
            items = ListHelper.grow(size, Math.max(1, items.length * 2), 1, items);
        }
    }

    @Override
    public int indexOfAsInt(int item) {
        return indexOfAsInt(item, 0);
    }

    public int indexOfAsInt(int item, int start) {
        for (int i = start; i < size; i++) {
            if (items[i] == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOfAsInt(int item) {
        return lastIndexOfAsInt(item, size - 1);
    }

    public int lastIndexOfAsInt(int item, int start) {
        for (int i = start; i >= 0; i--) {
            if (items[i] == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if size==0.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            int e = (int) o;
            return indexOfAsInt(e) != -1;
        }
        return false;
    }

    /**
     * Removes the item at the specified index from this list.
     *
     * @param index an index
     * @return the removed item
     */
    public int removeAtAsInt(int index) {
        Preconditions.checkIndex(index, size);
        int removedItem = items[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(items, index + 1, items, index, numMoved);
        }
        --size;
        return removedItem;
    }

    /**
     * Removes the last item
     *
     * @return the removed item
     * @throws NoSuchElementException if the list is empty
     */
    public int removeLastAsInt() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }
        return removeAtAsInt(size - 1);
    }

    /**
     * Replaces the item at the specified index.
     *
     * @param index   an index
     * @param newItem the new item
     * @return the old item
     */
    public int setAsInt(int index, int newItem) {
        Preconditions.checkIndex(index, size);
        int removedItem = items[index];
        items[index] = newItem;
        return removedItem;
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return size;
    }

    /**
     * Trims the capacity of the list its current size.
     */
    public void trimToSize() {
        items = ListHelper.trimToSize(size, 1, items);
    }

    /**
     * Returns an iterator for this list.
     *
     * @return an iterator over the elements of this list
     */
    public @NonNull PrimitiveIterator.OfInt iterator() {
        return new PrimitiveIterator.OfInt() {
            private int index = 0;
            private final int size = IntArrayList.this.size;
            private final int[] items = IntArrayList.this.items;

            @Override
            public int nextInt() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return items[index++];
            }

            @Override
            public boolean hasNext() {
                return index < size;
            }
        };
    }

    /**
     * Returns a spliterator for this list.
     *
     * @return a spliterator over the elements of this list
     */
    public @NonNull Spliterator.OfInt spliterator() {
        return Spliterators.spliterator(items, 0, size, Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a stream for processing the items of this list.
     *
     * @return a stream
     */
    public @NonNull IntStream intStream() {
        return (size == 0) ? IntStream.empty() : Arrays.stream(items, 0, size);
    }

    /**
     * Returns a new array containing all of the elements in this collection.
     *
     * @return array
     */
    public @NonNull int[] toIntArray() {
        int[] result = new int[size];
        System.arraycopy(items, 0, result, 0, size);
        return result;
    }

    @Override
    public boolean add(Integer integer) {
        addAsInt(integer);
        return true;
    }


    @Override
    public boolean remove(Object o) {
        if (o instanceof Integer) {
            int index = indexOfAsInt((int) o);
            if (index != -1) {
                removeAtAsInt(index);
                return true;
            }
        }
        return false;
    }


    @Override
    public @NonNull String toString() {
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(items[i]);
        }
        return b.append(']').toString();
    }

    /**
     * Sorts the items in ascending order.
     */
    public void sort() {
        Arrays.sort(items, 0, size);
    }

    /**
     * Removes all of the elements of this collection that satisfy the given
     * predicate.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     *               removed
     * @return {@code true} if any elements were removed
     */
    public boolean removeIfAsInt(@NonNull IntPredicate filter) {
        boolean hasRemoved = false;
        Objects.requireNonNull(filter, "filter");
        for (int i = size - 1; i >= 0; i--) {
            if (filter.test(getAsInt(i))) {
                removeAtAsInt(i);
                hasRemoved = true;
            }
        }
        return hasRemoved;
    }

    /**
     * Sorts this list according to the order induced by the specified
     * {@link Comparator}. The sort is <i>stable</i>: it does not
     * reorder equal elements.
     *
     * @param c the {@code Comparator} used to compare list elements.
     *          A {@code null} value indicates that the elements'
     *          {@linkplain Comparable natural ordering} should be used.
     */
    public void sort(@Nullable Comparator<? super Integer> c) {
        if (size > 1) {
            if (c == null) {
                Arrays.sort(items, 0, size);
            } else {
                // XXX this is inefficient, we need a sort method for an int-array that takes a comparator.
                final Integer[] objects = new Integer[size];
                for (int i = 0; i < size; i++) objects[i] = items[i];
                Arrays.sort(objects, 0, size, c);
                for (int i = 0; i < size; i++) items[i] = objects[i];
            }
        }
    }
}
