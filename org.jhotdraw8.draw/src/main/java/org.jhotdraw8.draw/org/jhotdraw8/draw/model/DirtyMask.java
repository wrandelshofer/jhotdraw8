/*
 * @(#)DirtyMask.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.model;


/**
 * Represents a bitmask of {@code DirtyBits}.
 *
 */
public class DirtyMask {

    /**
     * The bit set is coalesced into a bitmask.
     */
    private final int bitmask;

    public static final DirtyMask EMPTY = new DirtyMask(0);
    public static final DirtyMask ALL = new DirtyMask(~0);

    /**
     * Prevent instantiation.
     */
    private DirtyMask(int bitmask) {
        this.bitmask = bitmask;
    }

    public static DirtyMask of(DirtyBits... bits) {
        int mask = 0;
        for (DirtyBits bit : bits) {
            mask |= bit.getMask();
        }
        return new DirtyMask(mask);
    }

    /**
     * Interface for DirtyBits.
     */
    final int getMask() {
        return bitmask;
    }

    public boolean containsOneOf(DirtyBits... bits) {
        for (DirtyBits bit : bits) {
            if ((bitmask & bit.getMask()) == bit.getMask()) {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(DirtyBits... bits) {
        return intersects(of(bits));
    }

    public boolean intersects(DirtyMask that) {
        return (this.bitmask & that.bitmask) != 0;
    }

    public boolean isEmpty() {
        return bitmask == 0;
    }

    /**
     * Adds all bits of the specified dirty mask to this mask.
     *
     * @param that that mask
     * @return a new mask
     */
    public DirtyMask add(DirtyMask that) {
        return new DirtyMask(this.bitmask | that.bitmask);
    }

    public DirtyMask add(DirtyBits bits) {
        return new DirtyMask(this.bitmask | bits.getMask());
    }

    @Override
    public String toString() {
        return "DirtyMask{" + "bitmask=" + Integer.toBinaryString(bitmask) + '}';
    }

}
