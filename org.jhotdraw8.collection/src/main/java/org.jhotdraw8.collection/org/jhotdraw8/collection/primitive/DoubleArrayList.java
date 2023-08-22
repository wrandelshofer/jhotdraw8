/*
 * @(#)DoubleArrayList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.ListFacade;
import org.jhotdraw8.collection.impl.ListHelper;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

import java.util.*;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

/**
 * A {@code double}-valued list backed by a primitive array.
 *
 * @author Werner Randelshofer
 */
public class DoubleArrayList extends AbstractList<Double> implements DoubleList {
    private static final double[] EMPTY = new double[0];
    private double[] items;

    /**
     * Holds the size of the list. Invariant: size >= 0.
     */
    private int size;

    /**
     * Creates a new empty instance with 0 initial capacity.
     */
    public DoubleArrayList() {
        items = EMPTY;
    }

    /**
     * Creates a new empty instance with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public DoubleArrayList(int initialCapacity) {
        items = new double[initialCapacity];
    }

    /**
     * Creates a new instance from the specified collection
     *
     * @param collection a collection of integers
     */
    public DoubleArrayList(@NonNull Collection<Double> collection) {
        this.size = collection.size();
        this.items = new double[size];

        int count = 0;
        //noinspection ForLoopReplaceableByForEach
        for (Iterator<Double> iter = collection.iterator(); iter.hasNext(); ) {
            Double value = iter.next();
            items[count++] = value;
        }
    }

    private DoubleArrayList(double @NonNull [] items) {
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
    public static @NonNull DoubleArrayList of(double @NonNull ... items) {
        return new DoubleArrayList(items);
    }

    /**
     * Adds a new item to the end of the list.
     *
     * @param newItem the new item
     */
    @Override
    public void addAsDouble(double newItem) {
        grow(size + 1);
        items[size++] = newItem;
    }

    /**
     * Inserts a new item at the specified index into this list.
     *
     * @param index   the index
     * @param newItem the new item
     */
    @Override
    public void addAsDouble(int index, double newItem) {
        Objects.checkIndex(index, size + 1);
        grow(size + 1);
        items[index] = newItem;
        ++size;
    }

    /**
     * Adds all items of the specified list to this list.
     *
     * @param that another list
     */
    public void addAllAsDouble(@NonNull DoubleArrayList that) {
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
    public @NonNull <T extends Collection<Double>> T addAllInto(@NonNull T out) {
        for (int i = 0, n = size; i < n; i++) {
            out.add(items[i]);
        }
        return out;
    }

    /**
     * Clears the list in O(1).
     */
    @Override
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
    public void copyInto(double @NonNull [] a, int offset) {
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
        // FIXME this is not correct since we implement List<Double>
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DoubleArrayList other = (DoubleArrayList) obj;
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
    @Override
    public double getAsDouble(int index) {
        Objects.checkIndex(index, size);
        return items[index];
    }

    /*
     * Gets the item at the specified index.
     *
     * @param index an index
     * @return the item at the index
     */
    @Override
    public Double get(int index) {
        Objects.checkIndex(index, size);
        return items[index];
    }

    @Override
    public double getLastAsDouble() {
        return getAsDouble(size - 1);
    }

    @Override
    public double getFirstAsDouble() {
        return getAsDouble(0);
    }

    /**
     * Sets the size of this list. If the new size is greater than the current
     * size, new {@code 0} items are added to the end of the list. If the new
     * size is less than the current size, all items at indices greater or
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
            long bits = Double.doubleToLongBits(items[i]);
            result = 31 * result + (int) (bits ^ (bits >>> 32));
        }

        return result;
    }

    private void grow(int capacity) {
        if (items.length < capacity) {
            items = ListHelper.grow(Math.max(1, items.length * 2), 1, items);
        }
    }

    @Override
    public int indexOfAsDouble(double item) {
        return indexOfAsDouble(item, 0);
    }

    public int indexOfAsDouble(double item, int start) {
        for (int i = start; i < size; i++) {
            if (items[i] == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOfAsDouble(double item) {
        return lastIndexOfAsDouble(item, size - 1);
    }

    public int lastIndexOfAsDouble(double item, int start) {
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
    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    @Override
    public boolean contains(Object o) {
        if (o instanceof Double e) {
            return indexOfAsDouble(e) != -1;
        }
        return false;
    }

    /**
     * Removes the item at the specified index from this list.
     *
     * @param index an index
     * @return the removed item
     */
    @Override
    public double removeAtAsDouble(int index) {
        Objects.checkIndex(index, size);
        double removedItem = items[index];
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
    @Override
    public double removeLastAsDouble() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }
        return removeAtAsDouble(size - 1);
    }

    /**
     * Replaces the item at the specified index.
     *
     * @param index   an index
     * @param newItem the new item
     * @return the old item
     */
    public double setAsDouble(int index, double newItem) {
        Objects.checkIndex(index, size);
        double removedItem = items[index];
        items[index] = newItem;
        return removedItem;
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    @Override
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
    @Override
    public PrimitiveIterator.@NonNull OfDouble iterator() {
        return new PrimitiveIterator.OfDouble() {
            private int index = 0;
            private final int size = DoubleArrayList.this.size;
            private final double[] items = DoubleArrayList.this.items;

            @Override
            public double nextDouble() {
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
    @Override
    public Spliterator.@NonNull OfDouble spliterator() {
        return Spliterators.spliterator(items, 0, size, Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    /**
     * Returns a stream for processing the items of this list.
     *
     * @return a stream
     */
    public @NonNull DoubleStream doubleStream() {
        return (size == 0) ? DoubleStream.empty() : Arrays.stream(items, 0, size);
    }

    /**
     * Returns a new array containing all the elements in this collection.
     *
     * @return array
     */
    public double @NonNull [] toDoubleArray() {
        double[] result = new double[size];
        System.arraycopy(items, 0, result, 0, size);
        return result;
    }

    @Override
    public boolean add(Double e) {
        addAsDouble(e);
        return true;
    }


    @Override
    public boolean remove(Object o) {
        if (o instanceof Double e) {
            int index = indexOfAsDouble(e);
            if (index != -1) {
                removeAtAsDouble(index);
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
     * Removes all the elements of this collection that satisfy the given
     * predicate.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     *               removed
     * @return {@code true} if any elements were removed
     */
    public boolean removeIfAsDouble(@NonNull DoublePredicate filter) {
        boolean hasRemoved = false;
        Objects.requireNonNull(filter, "filter");
        for (int i = size - 1; i >= 0; i--) {
            if (filter.test(getAsDouble(i))) {
                removeAtAsDouble(i);
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
    @Override
    public void sort(@Nullable Comparator<? super Double> c) {
        if (size > 1) {
            if (c == null) {
                Arrays.sort(items, 0, size);
            } else {
                // XXX this is inefficient, we need a sort method for an int-array that takes a comparator.
                final Double[] objects = new Double[size];
                for (int i = 0; i < size; i++) {
                    objects[i] = items[i];
                }
                Arrays.sort(objects, 0, size, c);
                for (int i = 0; i < size; i++) {
                    items[i] = objects[i];
                }
            }
        }
    }

    @Override
    public @NonNull SequencedCollection<Double> _reversed() {
        return new ListFacade<>(
                this::size,
                i -> get(size() - i)
        );
    }

    public double[] getArray() {
        return items;
    }

    @Override
    public void addFirst(Double e) {
        DoubleList.super.addFirst(e);
    }

    @Override
    public void addLast(Double e) {
        DoubleList.super.addLast(e);
    }

    @Override
    public Double getFirst() {
        return DoubleList.super.getFirst();
    }

    @Override
    public Double getLast() {
        return DoubleList.super.getLast();
    }

    @Override
    public Double removeFirst() {
        return DoubleList.super.removeFirst();
    }

    @Override
    public Double removeLast() {
        return DoubleList.super.removeLast();
    }
}
