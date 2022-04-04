package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Arrays;

/**
 * Package private class with code for {@code PersistentTreeList}
 * and {@code TreeList}.
 * <p>
 * This is an extremely naïve implementation of a Relaxed Radix Balanced Tree (RRB Tree).
 * <p>
 * References:
 * <dl>
 *     <dt>Phil Bagwell, Tiark Rompf. (2012). RRB-Trees: Efficient Immutable Vectors</dt>
 *     <dd><a href="https://infoscience.epfl.ch/record/169879/files/RMTrees.pdf">epfl.ch</a></dd>
 *
 *     <dt>Jean Niklas L'orange. (2014). Improving RRB-Tree Performance through
 *     Transience</dt>
 *     <dd><a href="https://hypirion.com/thesis.pdf">hypirion.com</a></dd>
 *
 *     <dt>"c-rrb" library</dt>
 *     <dd>Copyright (c) Jean Niklas L'orange . MIT License.
 *         <a href="https://github.com/hypirion/c-rrb">github.com</a></dd>
 * </dl>
 */
class PersistentTreeListHelper {
    final static LeafNode<?> EMPTY_LEAF = new LeafNode<>(new Object[0]);

    static <E> @NonNull LeafNode<E> merge(@Nullable UniqueIdentity mutator, @NonNull LeafNode<E> left, @NonNull LeafNode<E> right) {
        E[] leftData = left.data;
        E[] rightData = right.data;
        E[] mergedData = Arrays.copyOf(leftData, leftData.length + rightData.length);
        System.arraycopy(rightData, 0, mergedData, leftData.length, rightData.length);
        return newLeafNode(mutator, mergedData);
    }

    static <E> @NonNull InternalNode<E> newAbove(@Nullable UniqueIdentity mutator, @Nullable InternalNode<E> left, @Nullable InternalNode<E> centre, @Nullable InternalNode<E> right) {
        int leftLen = left == null ? 0 : left.children.length - 1;
        int centreLen = centre == null ? 0 : centre.children.length;
        int rightLen = right == null ? 0 : right.children.length - 1;

        @SuppressWarnings("unchecked")
        Node<E>[] mergedChildren = (Node<E>[]) new Node[leftLen + centreLen + rightLen];
        if (left != null) {
            System.arraycopy(left.children, 0, mergedChildren, 0, leftLen);
        }
        if (centre != null) {
            System.arraycopy(centre.children, 0, mergedChildren, leftLen, centreLen);
        }
        if (right != null) {
            System.arraycopy(right.children, 0, mergedChildren, leftLen + centreLen, rightLen);
        }
        return newInternalNode(mutator, mergedChildren);
    }

    static <E> @NonNull InternalNode<E> newAbove1(@Nullable UniqueIdentity mutator, @NonNull Node<E> child) {
        @SuppressWarnings("unchecked") Node<E>[] children = new Node[]{child};
        return newInternalNode(mutator, children);
    }

    static <E> @NonNull InternalNode<E> newInternalNode(@Nullable UniqueIdentity mutator, @NonNull Node<E>[] children) {
        return mutator == null ? new InternalNode<E>(children) : new MutableInternalNode<E>(mutator, children);
    }

    static <E> @NonNull LeafNode<E> newLeafNode(@Nullable UniqueIdentity mutator, @NonNull E[] data) {
        return mutator == null ? new LeafNode<E>(data) : new MutableLeafNode<E>(mutator, data);
    }

    /**
     * Relaxed Radix Balanced Tree.
     * <p>
     * Invariants:
     * <ul>
     *     <li>All leaf nodes are at the same depth.</li>
     *     <li>All leaf nodes have between
     *     {@code m/2} and {@code m} data elements.</li>
     *     <li>All inner nodes except the root have {@code m} children.</li>
     *     <li>The root node has between {@code 1} and {@code m} children.</li>
     *     <li>The rightmost node (the tail) is stored in the a separate
     *     data structure (named {@code tail}). The tail has {@code 0}
     *     to {@code m} data elements.</li>
     * </ul>
     *
     * @param <E>
     */
    static class RrbTree<E> {
        private int cnt;
        private int shift;
        private int tailLen;
        private LeafNode<E> tail;
        private Node<E> root;
        private UniqueIdentity mutator;

        @SuppressWarnings("unchecked")
        public RrbTree() {
            tail = (LeafNode<E>) EMPTY_LEAF;
        }
    }

    static class Node<E> {
        @Nullable UniqueIdentity getMutator() {
            return null;
        }

        boolean isAllowedToEdit(@Nullable UniqueIdentity y) {
            UniqueIdentity x = getMutator();
            return x != null && x == y;
        }
    }

    static class LeafNode<E> extends Node<E> {
        private @NonNull E[] data;

        LeafNode(@NonNull E[] newData) {
            this.data = newData;
        }

        LeafNode<E> dec(@Nullable UniqueIdentity mutator) {
            E[] newData = Arrays.copyOf(data, data.length - 1);
            if (isAllowedToEdit(mutator)) {
                data = newData;
                return this;
            } else {
                return new LeafNode<>(newData);
            }
        }

        LeafNode<E> inc(@Nullable UniqueIdentity mutator) {
            E[] newData = Arrays.copyOf(data, data.length + 1);
            if (isAllowedToEdit(mutator)) {
                data = newData;
                return this;
            } else {
                return new LeafNode<>(newData);
            }
        }

    }

    static class InternalNode<E> extends Node<E> {
        private int[] sizes;
        private @NonNull Node<E>[] children;

        public InternalNode(@NonNull Node<E>[] children) {
            this.children = children;
        }

    }

    static class MutableLeafNode<E> extends LeafNode<E> {
        private final @NonNull UniqueIdentity mutator;

        MutableLeafNode(@NonNull UniqueIdentity mutator, @NonNull E[] data) {
            super(data);
            this.mutator = mutator;
        }

        @NonNull UniqueIdentity getMutator() {
            return mutator;
        }
    }

    static class MutableInternalNode<E> extends InternalNode<E> {
        private final @NonNull UniqueIdentity mutator;

        MutableInternalNode(@NonNull UniqueIdentity mutator, @NonNull Node<E>[] children) {
            super(children);
            this.mutator = mutator;
        }

        @NonNull UniqueIdentity getMutator() {
            return mutator;
        }
    }
}
