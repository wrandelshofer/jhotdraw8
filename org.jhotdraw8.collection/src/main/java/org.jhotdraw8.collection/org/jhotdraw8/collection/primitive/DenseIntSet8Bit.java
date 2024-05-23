/*
 * @(#)DenseIntSet8Bit.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.primitive;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A dense set of int-values which can be cleared in O(1);
 * needs 8-bits storage space for each int-value.
 * <p>
 * This set is optimised for frequent setting and clearing.
 * <p>
 * Setting a boolean at an index is O(1).
 * <p>
 * Clearing the set is O(1) amortized. Every 255 times it takes O(n) time to
 * clear the set.
 * <p>
 * Storage space is one byte per boolean.
 * <p>
 * This set has a fixed capacity. Attempting to access an element outside of the
 * capacity range results in an {@link ArrayIndexOutOfBoundsException}.
 */
public class DenseIntSet8Bit implements IntSet {
    private byte[] a;
    private byte mark = 1;

    /**
     * Creates a set with the specified capacity.
     */
    public DenseIntSet8Bit(int capacity) {
        a = new byte[capacity];
    }

    /**
     * Adds an element to the set.
     *
     * @param element the element
     * @return true if the element was added, false if it was already in the set.
     * @throws ArrayIndexOutOfBoundsException if element is outside of the
     *                                        capacity range.
     */
    @Override
    public boolean addAsInt(int element) {
        if (a[element] != mark) {
            a[element] = mark;
            return true;
        }
        return false;
    }

    /**
     * Removes the specified element from the set.
     *
     * @param element an element
     * @return true if the element was in the set, false otherwise
     * @throws ArrayIndexOutOfBoundsException if element is outside of the
     *                                        capacity range.
     */
    @Override
    public boolean removeAsInt(int element) {
        if (a[element] == mark) {
            a[element] = 0;
            return true;
        }
        return false;
    }

    /**
     * Checks if the set contains the specified element.
     *
     * @param e an element
     * @return true if the element is in the set.
     * @throws ArrayIndexOutOfBoundsException if element is outside of the
     *                                        capacity range.
     */
    @Override
    public boolean containsAsInt(int e) {
        return a[e] == mark;
    }

    @Override
    public void clear() {
        if (++mark == 0) {
            Arrays.fill(a, (byte) 0);
            mark = 1;
        }
    }

    /**
     * Sets the capacity of the set.
     *
     * @param capacity the new capacity
     */
    public void setCapacity(int capacity) {
        a = Arrays.copyOf(a, capacity);
    }

    /**
     * Gets the capacity of the set.
     *
     * @return the capacity
     */
    public int capacity() {
        return a.length;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DenseIntSet8Bit that = (DenseIntSet8Bit) o;
        return this.capacity() == that.capacity()
                && Arrays.equals(this.toLongArray(), that.toLongArray());
    }

    /**
     * The hash code is the same as {@link BitSet#hashCode()}.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        long h = 1234;
        long[] words = toLongArray();
        for (int i = words.length; --i >= 0; ) {
            h ^= words[i] * (i + 1);
        }
        return (int) ((h >> 32) ^ h);
    }

    /**
     * Returns a new long array containing all the bits in this int set.
     *
     * @return a new long array.
     */
    public long[] toLongArray() {
        int length = (a.length - 1 >>> 6) + 1;
        long[] words = new long[length];
        int lastSetBit = -1;
        for (int i = 0, n = capacity(); i < n; i++) {
            if (containsAsInt(i)) {
                lastSetBit = i;
                int wordIndex = i >>> 6;
                int bitIndex = i & 63;
                words[wordIndex] |= 1L << bitIndex;
            }
        }
        int usedLength = lastSetBit == -1 ? 0 : (lastSetBit >>> 6) + 1;
        return usedLength == length ? words : Arrays.copyOf(words, usedLength);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append('{');
        int i = 0, n = capacity();
        for (; i < n; i++) {
            if (containsAsInt(i)) {
                buf.append(i++);
                break;
            }
        }
        for (; i < n; i++) {
            if (containsAsInt(i)) {
                buf.append(", ");
                buf.append(i);
            }
        }
        buf.append('}');
        return buf.toString();
    }
}
