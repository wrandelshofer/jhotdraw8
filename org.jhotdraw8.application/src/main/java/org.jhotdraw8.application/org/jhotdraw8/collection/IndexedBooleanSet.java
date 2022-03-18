/*
 * @(#)BooleanSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A dense indexed boolean set.
 * <p>
 * This set is optimised for frequent setting and clearing.
 * <p>
 * Setting a boolean at an index is O(1).
 * <p>
 * Clearing the set is O(1) amortized. Every 255 times it takes O(n) time to
 * clear the set.
 * <p>
 * Storage space is one byte per boolean.
 */
public class IndexedBooleanSet {
    private byte[] a;
    private byte mark = 1;

    /**
     * Creates an empty set.
     */
    public IndexedBooleanSet() {
        this(0);
    }

    /**
     * Creates a set of the specified size.
     */
    public IndexedBooleanSet(int size) {
        a = new byte[size];
    }

    /**
     * Sets the boolean at the specified index.
     *
     * @param index index
     * @return true if the boolean was not set yet.
     */
    public boolean add(int index) {
        if (a[index] != mark) {
            a[index] = mark;
            return true;
        }
        return false;
    }

    /**
     * Clears the boolean at the specified index.
     *
     * @param index index
     * @return true if the boolean was already set.
     */
    public boolean remove(int index) {
        if (a[index] == mark) {
            a[index] = 0;
            return true;
        }
        return false;
    }

    /**
     * Gets the boolean at the specified index.
     *
     * @param index index
     * @return the boolean
     */
    public boolean get(int index) {
        return a[index] == mark;
    }

    /**
     * Clears the boolean set.
     */
    public void clear() {
        if (++mark == -1) {
            Arrays.fill(a, (byte) 0);
            mark = 1;
        }
    }

    /**
     * Sets the size of the set.
     *
     * @param size the new size.
     */
    public void setSize(int size) {
        a = Arrays.copyOf(a, size);
    }

    /**
     * Gets the size of the set.
     *
     * @return the size
     */
    public int size() {
        return a.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexedBooleanSet that = (IndexedBooleanSet) o;
        return this.size() == that.size()
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
        for (int i = words.length; --i >= 0; )
            h ^= words[i] * (i + 1);
        return (int) ((h >> 32) ^ h);
    }

    /**
     * Returns a new long array containing all the bits in this bit set.
     *
     * @return a new long array.
     */
    public long[] toLongArray() {
        int length = (a.length - 1 >>> 6) + 1;
        long[] words = new long[length];
        int lastSetBit = -1;
        for (int i = 0, n = size(); i < n; i++) {
            if (get(i)) {
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
        int i = 0, n = size();
        for (; i < n; i++) {
            if (get(i)) {
                buf.append(i++);
                break;
            }
        }
        for (; i < n; i++) {
            if (get(i)) {
                buf.append(", ");
                buf.append(i);
            }
        }
        buf.append('}');
        return buf.toString();
    }
}
