package org.jhotdraw8.icollection.impl.rrbnaive;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jspecify.annotations.Nullable;

import static org.jhotdraw8.icollection.impl.rrbnaive.RrbTree.MASK;
import static org.jhotdraw8.icollection.impl.rrbnaive.RrbTree.WIDTH;

public class RrbNode<E> {
    public static final RrbNode<?> EMPTY = new RrbNode<Object>(new Object[0], null, null);

    public static <E> RrbNode<E> empty() {
        return (RrbNode<E>) EMPTY;
    }

    public boolean isEmpty() {
        return children.length == 0;
    }

    @Nullable Object[] children;
    short @Nullable [] sizes;
    @Nullable MutabilityOwnership owner;

    public RrbNode(@Nullable Object[] children, short @Nullable [] sizes, @Nullable MutabilityOwnership owner) {
        this.children = children;
        this.sizes = sizes;
        this.owner = owner;
    }

    public int getWidth() {
        return children.length;
    }

    public boolean isOwnedBy(MutabilityOwnership owner) {
        return owner != null && owner == this.owner;
    }

    public RrbNode<E> copy() {
        return new RrbNode<>(children.clone(), sizes, null);
    }


    public RrbNode<E> makeOwned(MutabilityOwnership owner) {
        if (isOwnedBy(owner)) {
            return this;
        }
        return new RrbNode<E>(children.clone(), sizes, owner);
    }

    public RrbNode<E> settingArray(@Nullable Object[] array, MutabilityOwnership owner) {
        if (isOwnedBy(owner)) {
            this.children = array;
            return this;
        }
        return new RrbNode<>(array, sizes, owner);
    }

    public RrbNode<E> get(int childIndex) {
        return (RrbNode<E>) children[childIndex];
    }

    public <E> E getValue(int childIndex) {
        return (E) children[childIndex];
    }

    public void setChild(int childIndex, RrbNode<E> node) {
        children[childIndex] = node;
    }

    public void setValue(int childIndex, E value) {
        children[childIndex] = value;
    }

    public int getIndex(int offsetKey, int shift) {
        int estimate = (offsetKey >> shift) & MASK;
        if (sizes == null) {
            return estimate;
        } else {
            return sizes[estimate] > offsetKey ? estimate : estimate + 1;
        }
    }

    public int getOffsetAtChildIndex(int childIndex, int shift) {
        if (sizes == null) {
            return (WIDTH * childIndex) << shift;
        } else {
            return sizes[childIndex] << shift;
        }
    }

}
