/*
 * @(#)HashCollider.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;


import java.io.Serializable;

/**
 * Elem has a configurable bit mask for its hash code.
 * Allowing to run the same test with many or few hash collisions.
 */
public class Value implements Serializable, Cloneable, Comparable<Value> {
    private static final long serialVersionUID = 0L;
    private final int value;
    private final int hash;
    private final int hashBitMask;

    public Value(int value) {
        this(value, -1);
    }

    public Value(int value, int hashBitMask) {
        this.value = value;
        this.hashBitMask = hashBitMask;
        this.hash = value & hashBitMask;
    }

    @Override
    public int compareTo(Value o) {
        return Integer.compare(o.value, this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Value collider = (Value) o;
        return value == collider.value;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public int getValue() {
        return value;
    }

    public int getHashBitMask() {
        return hashBitMask;
    }

    @Override
    public Value clone() {
        try {
            return (Value) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
