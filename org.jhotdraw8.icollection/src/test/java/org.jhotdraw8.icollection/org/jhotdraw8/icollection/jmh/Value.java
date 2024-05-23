package org.jhotdraw8.icollection.jmh;


/**
 * A key with an integer value and a masked hash code.
 * The mask allows to provoke collisions in hash maps.
 */
public class Value implements Comparable<Value> {
    public final int value;
    public final int hashCode;

    public Value(int value, int mask) {
        this.value = value;
        this.hashCode = value & mask;
    }

    @Override
    public int compareTo(Value o) {
        return Integer.compare(o.value, this.value);
    }

    @Override
    public String toString() {
        return "Value{" +
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
        Value that = (Value) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
