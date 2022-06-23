/*
 * @(#)IntArrayDeque.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.primitive;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.SequencedCollection;
import org.jhotdraw8.util.Preconditions;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@code int}-valued deque backed by a primitive array.
 *
 * @author Werner Randelshofer
 */
public class IntArrayDeque extends AbstractCollection<Integer> implements IntDeque {
    /**
     * The length of this array is always a power of 2.
     */
    private int[] elements;

    /**
     * Index of the element at the head of the deque.
     */
    private int head;

    /**
     * Index at which the next element would be added to the tail of the deque.
     */
    private int tail;

    /**
     * Creates a new instance with an initial capacity for 8 elements.
     */
    public IntArrayDeque() {
        this(8);
    }

    /**
     * Creates a new instance with the specified initial capacity rounded up
     * to the next strictly positive power of two.
     *
     * @param capacity initial capacity
     */
    public IntArrayDeque(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity=" + capacity);
        }
        elements = new int[Math.max(Integer.highestOneBit(capacity + capacity - 1), 0)];
    }


    @Override
    public void addFirstAsInt(int e) {
        //Note: elements.length is a power of two.
        head = (head - 1) & (elements.length - 1);
        elements[head] = e;
        if (head == tail) {
            doubleCapacity();
        }
    }

    @Override
    public void addLastAllAsInt(int @NonNull [] array) {
        addLastAllAsInt(array, 0, array.length);
    }

    @Override
    public void addLastAllAsInt(int @NonNull [] array, int offset, int length) {
        grow(length + size());


        int firstPart = elements.length - tail;
        if (tail >= head && firstPart >= length
                || head - tail > length) {
            System.arraycopy(array, offset, elements, tail, length);
            tail = (tail + length) & (elements.length - 1);
            return;
        }

        System.arraycopy(array, offset, elements, tail, firstPart);
        int secondPart = length - firstPart;
        System.arraycopy(array, offset + firstPart, elements, 0, secondPart);
        tail = secondPart;
    }

    /**
     * Increases the capacity of this deque.
     */
    private void grow(int capacity) {
        if (elements.length > capacity) return;
        int newLength = Integer.highestOneBit(capacity + capacity - 1);
        final int[] a = new int[newLength];
        int size = size();
        if (head < tail) {
            System.arraycopy(elements, head, a, 0, size);
        } else {
            final int r = elements.length - head; // number of elements to the right of head
            System.arraycopy(elements, head, a, 0, r);
            System.arraycopy(elements, 0, a, r, head);
        }
        elements = a;
        head = 0;
        tail = size;
    }

    @Override
    public void addLastAsInt(int e) {
        elements[tail] = e;
        tail = (tail + 1) & (elements.length - 1);
        if (tail == head) {
            doubleCapacity();
        }
    }

    /**
     * Clears the deque in O(1).
     */
    @Override
    public void clear() {
        // Performance: Do not fill the array with zeros.
        this.head = this.tail = 0;
    }

    @Override
    public @NonNull Iterator<Integer> descendingIterator() {
        throw new UnsupportedOperationException();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntArrayDeque)) {
            return false;
        }
        IntArrayDeque that = (IntArrayDeque) o;
        if (this.size() != that.size()) {
            return false;
        }
        int thisMask = elements.length - 1;
        int thatMask = that.elements.length - 1;
        for (int i = this.head, j = that.head; i != this.tail; i = (i + 1) & thisMask, j = (j + 1) & thatMask) {
            if (this.elements[i] != that.elements[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the first index of the specified element
     * or -1 if this deque does not contain the element.
     *
     * @param o the element
     * @return the first index of the element
     */
    public int firstIndexOfAsInt(int o) {
        if (tail < head) {
            for (int i = head; i < elements.length; i++) {
                if (o == (elements[i])) {
                    return i - head;
                }
            }
            for (int i = 0; i < tail; i++) {
                if (o == (elements[i])) {
                    return i + elements.length - head;
                }
            }
        } else {
            for (int i = head; i < tail; i++) {
                if (o == (elements[i])) {
                    return i - head;
                }
            }
        }
        return -1;
    }


    @Override
    public int getFirstAsInt() {
        if (head == tail) {
            throw new NoSuchElementException();
        }
        return elements[head];
    }

    public int getAsInt(int index) {
        Preconditions.checkIndex(index, size());
        return (head + index < elements.length) ? elements[head + index] : elements[head + index - elements.length];
    }


    @Override
    public int getLastAsInt() {
        if (head == tail) {
            throw new NoSuchElementException();
        }
        return elements[tail == 0 ? elements.length - 1 : tail - 1];
    }

    /**
     * Increases the capacity of this deque.
     */
    private void doubleCapacity() {
        assert head == tail;
        final int p = head;
        final int n = elements.length;
        final int r = n - p; // number of elements to the right of p
        final int[] a = new int[n << 1];
        System.arraycopy(elements, p, a, 0, r);
        System.arraycopy(elements, 0, a, r, p);
        elements = a;
        head = 0;
        tail = n;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        int mask = elements.length - 1;
        for (int i = head; i != tail; i = (i + 1) & mask) {
            hash = hash * 31 + elements[i];
        }
        return hash;
    }

    /**
     * Returns true if this deque is empty.
     *
     * @return {@code true} if this deque contains no elements
     */
    @Override
    public boolean isEmpty() {
        return head == tail;
    }

    @Override
    public SequencedCollection<Integer> reversed() {
        return null;
    }

    @Override
    public @NonNull Iterator<Integer> iterator() {
        return new DeqIterator();
    }

    /**
     * Returns the last index of the specified element
     * or -1 if this deque does not contain the element.
     *
     * @param o the element
     * @return the last index of the element
     */
    public int lastIndexOfAsInt(int o) {
        if (tail < head) {
            for (int i = elements.length - 1; i >= head; i--) {
                if (o == (elements[i])) {
                    return i - head;
                }
            }
            for (int i = tail - 1; i >= 0; i--) {
                if (o == (elements[i])) {
                    return i + elements.length - head;
                }
            }
        } else {
            for (int i = tail - 1; i >= head; i--) {
                if (o == (elements[i])) {
                    return i - head;
                }
            }
        }
        return -1;
    }

    /**
     * Removes an element at the given array index.
     *
     * @param i an array index
     */
    public void removeAt(int i) {
        int size = size();
        Preconditions.checkIndex(i, size);
        if (tail < head) {
            if (head + i < elements.length) {
                if (i > 0) {
                    System.arraycopy(elements, head, elements, head + 1, i - 1);
                }
                elements[head] = 0;
                head = head == elements.length ? 0 : head + 1;
            } else {
                if (i < size - 1) {
                    System.arraycopy(elements, i - elements.length + head + 1, elements, i - elements.length + head, size - i);
                }
                elements[tail] = 0;
                tail = tail == 0 ? elements.length : tail - 1;
            }
        } else {
            if (i < size - 1) {
                System.arraycopy(elements, head + i + 1, elements, head + i, size - i);
            }
            elements[head + i] = 0;
            tail--;
        }
    }

    @Override
    public int removeFirstAsInt() {
        if (head == tail) {
            throw new NoSuchElementException();
        }
        int result = elements[head];
        elements[head] = 0;
        head = (head == elements.length - 1) ? 0 : head + 1;
        return result;
    }

    @Override
    public boolean removeFirstOccurrenceAsInt(int o) {
        int index = firstIndexOfAsInt(o);
        if (index != -1) {
            removeAt(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrenceAsInt(int o) {
        int index = lastIndexOfAsInt(o);
        if (index != -1) {
            removeAt(index);
            return true;
        }
        return false;
    }

    @Override
    public int removeLastAsInt() {
        if (head == tail) {
            throw new NoSuchElementException();
        }
        tail = (tail == 0) ? elements.length - 1 : tail - 1;
        int result = elements[tail];
        elements[tail] = 0;
        return result;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            return firstIndexOfAsInt((int) o) != -1;
        }
        return false;
    }

    @Override
    public int size() {
        return (tail - head) & (elements.length - 1);
    }

    public @NonNull String toString() {
        Iterator<Integer> it = iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            Integer e = it.next();
            sb.append(e);
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

    private class DeqIterator implements Iterator<Integer> {
        /**
         * Tail recorded at construction, to stop
         * iterator and also to check for co-modification.
         */
        private final int fence = tail;
        /**
         * Index of element to be returned by subsequent call to next.
         */
        private int cursor = head;

        @Override
        public boolean hasNext() {
            return cursor != fence;
        }

        @Override
        public Integer next() {
            if (cursor == fence) {
                throw new NoSuchElementException();
            }
            int result = elements[cursor];
            // This check doesn't catch all possible co-modifications,
            // but does catch the ones that corrupt traversal
            if (tail != fence) {
                throw new ConcurrentModificationException();
            }
            cursor = (cursor + 1) & (elements.length - 1);
            return result;
        }
    }
}
