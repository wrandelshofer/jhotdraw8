package org.jhotdraw8.collection;

/**
 * Elem has a configurable bit mask for its hash code.
 * Allowing to run the same test with many or few hash collisions.
 */
class HashCollider {
    private final int value;
    private final int hash;
    private final int hashBitMask;

    public HashCollider(int value, int hashBitMask) {
        this.value = value;
        this.hashBitMask = hashBitMask;
        this.hash = value & hashBitMask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HashCollider collider = (HashCollider) o;
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
}
