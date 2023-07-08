package org.jhotdraw8.pcollection.jmh;

import org.jhotdraw8.annotation.NonNull;

/**
 * A key with an integer value and a masked hash code.
 * The mask allows to provoke collisions in hash maps.
 */
public class Key implements Comparable<Key> {
    public final int value;
    public final int hashCode;

    public Key(int value, int mask) {
        this.value = value;
        this.hashCode = value & mask;
    }

    @Override
    public int compareTo(@NonNull Key o) {
        return Integer.compare(o.value, this.value);
    }

    @Override
    public String toString() {
        return "Key{" +
                "" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Key that = (Key) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
