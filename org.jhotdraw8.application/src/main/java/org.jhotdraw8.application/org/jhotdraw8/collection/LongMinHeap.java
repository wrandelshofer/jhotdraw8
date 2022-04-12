/*
 * @(#)LongMinHeap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;

public class LongMinHeap implements LongQueue {

    // Member variables of this class
    private long[] heap;
    private int size;
    private int maxsize;

    /**
     * Initializing front as static with unity.
     */
    private static final int FRONT = 1;

    // Constructor of this class
    public LongMinHeap(final int maxsize) {

        // This keyword refers to current object itself
        this.maxsize = maxsize;
        this.size = 0;

        heap = new long[this.maxsize + 1];
        heap[0] = Long.MIN_VALUE;
    }

    /**
     * Returns the position of
     * the parent for the node currently
     * at pos.
     */
    private int parent(final int pos) {
        return pos / 2;
    }

    /**
     * Returns the position of the
     * left child for the node currently at pos.
     */
    private int leftChild(final int pos) {
        return (2 * pos);
    }

    /**
     * Returns the position of
     * the right child for the node currently
     * at pos.
     */
    private int rightChild(final int pos) {
        return (2 * pos) + 1;
    }

    /**
     * Returns true if the passed
     * node is a leaf node.
     */
    private boolean isLeaf(final int pos) {

        return pos > (size / 2) && pos <= size;
    }

    /**
     * Swaps two nodes of the heap.
     */
    private void swap(final int fpos, final int spos) {

        final long tmp;
        tmp = heap[fpos];

        heap[fpos] = heap[spos];
        heap[spos] = tmp;
    }

    /**
     * Heapifies the node at pos.
     */
    private void minHeapify(final int pos) {

        // If the node is a non-leaf node and greater
        // than any of its child
        if (!isLeaf(pos)) {
            if (heap[pos] > heap[leftChild(pos)]
                    || heap[pos] > heap[rightChild(pos)]) {

                // Swap with the left child and heapify
                // the left child
                if (heap[leftChild(pos)]
                        < heap[rightChild(pos)]) {
                    swap(pos, leftChild(pos));
                    minHeapify(leftChild(pos));
                }

                // Swap with the right child and heapify
                // the right child
                else {
                    swap(pos, rightChild(pos));
                    minHeapify(rightChild(pos));
                }
            }
        }
    }

    /**
     * Inserts a node into the heap.
     *
     * @throws IllegalStateException if the element cannot be added
     *                               at this time due to capacity restrictions.
     */
    public boolean offerAsLong(final long element) {
        if (size < maxsize) {
            return addAsLong(element);
        }
        return false;
    }

    public boolean addAsLong(final long element) {
        if (size >= maxsize) {
            throw new IllegalStateException("maxsize reached: " + maxsize);
        }

        heap[++size] = element;
        int current = size;

        while (heap[current] < heap[parent(current)]) {
            swap(current, parent(current));
            current = parent(current);
        }

        return true;
    }

    /**
     * Prints the contents of the heap.
     */
    public void print() {
        for (int i = 1; i <= size / 2; i++) {

            // Printing the parent and both childrens
            System.out.print(
                    " PARENT : " + heap[i]
                            + " LEFT CHILD : " + heap[2 * i]
                            + " RIGHT CHILD :" + heap[2 * i + 1]);

            // By here new line is required
            System.out.println();
        }
    }

    /**
     * Removes and returns the minimum element from the heap.
     *
     * @throws java.util.NoSuchElementException if the heap is empty
     */
    public long removeAsLong() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        final long popped = heap[FRONT];
        heap[FRONT] = heap[size--];
        minHeapify(FRONT);

        return popped;
    }

    @Override
    public boolean removeAsLong(long e) {
        for (int i = FRONT; i <= size; i++) {
            if (heap[i] == e) {
                final LongArrayList tmp = new LongArrayList(size - 1);
                for (int j = FRONT; j <= size; j++) {
                    if (j != i) {
                        tmp.addAsLong(heap[j]);
                    }
                }
                clear();
                for (int j = 0, n = tmp.size(); j < n; j++) {
                    add(tmp.getAsLong(j));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public long elementAsLong() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return heap[FRONT];
    }

    @Override
    public boolean containsAsLong(long e) {
        for (int i = FRONT; i <= size; i++) {
            if (e == heap[i]) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return size;
    }

    public @NonNull Spliterator.OfLong spliterator() {
        return Spliterators.spliterator(heap, 1, size + 1, Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    public @NonNull Iterator<Long> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Object[] toArray() {
        final Object[] objects = new Object[size];
        for (int i = FRONT; i <= size; i++) {
            objects[i - 1] = heap[i];
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = this.size;
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        for (int i = FRONT; i <= size; i++) {
            a[i - 1] = (T) (Long) heap[i];
        }
        return a;
    }

    @Override
    public void clear() {
        size = 0;
    }
}

