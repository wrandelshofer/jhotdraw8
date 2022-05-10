/*
 * @(#)TrieListHelper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Arrays;

class TrieListHelper {
    final static LeafNode<?> EMPTY_LEAF = new LeafNode<>(new Object[0]);
    static final int BIT_PARTITION_SIZE = 2;
    static final int BIT_MASK = (1 << BIT_PARTITION_SIZE) - 1;
    static final int M = 1 << BIT_PARTITION_SIZE;

    static <E> @NonNull LeafNode<E> merge(@Nullable UniqueId mutator, @NonNull LeafNode<E> left, @NonNull LeafNode<E> right) {
        E[] leftData = left.data;
        E[] rightData = right.data;
        E[] mergedData = Arrays.copyOf(leftData, leftData.length + rightData.length);
        System.arraycopy(rightData, 0, mergedData, leftData.length, rightData.length);
        return newLeafNode(mutator, mergedData);
    }

    static <E> @NonNull InternalNode<E> newAbove(@Nullable UniqueId mutator, @Nullable InternalNode<E> left, @Nullable InternalNode<E> centre, @Nullable InternalNode<E> right) {
        int leftLen = left == null ? 0 : left.children.length - 1;
        int centreLen = centre == null ? 0 : centre.children.length;
        int rightLen = right == null ? 0 : right.children.length - 1;

        int[] mergedSizes = new int[leftLen + centreLen + rightLen];
        @SuppressWarnings({"unchecked", "rawtypes"})
        Node<E>[] mergedChildren = (Node<E>[]) new Node[leftLen + centreLen + rightLen];
        if (left != null) {
            System.arraycopy(left.sizes, 0, mergedSizes, 0, leftLen);
            System.arraycopy(left.children, 0, mergedChildren, 0, leftLen);
        }
        if (centre != null) {
            System.arraycopy(centre.sizes, 0, mergedSizes, leftLen, centreLen);
            System.arraycopy(centre.children, 0, mergedChildren, leftLen, centreLen);
        }
        if (right != null) {
            System.arraycopy(right.sizes, 0, mergedSizes, leftLen + centreLen, rightLen);
            System.arraycopy(right.children, 0, mergedChildren, leftLen + centreLen, rightLen);
        }
        return newInternalNode(mutator, mergedSizes, mergedChildren);
    }

    static <E> @NonNull InternalNode<E> newAbove1(@Nullable UniqueId mutator, @NonNull Node<E> child) {
        int[] sizes = new int[]{child.getLength()};
        @SuppressWarnings({"unchecked", "rawtypes"}) Node<E>[] children = new Node[]{child};
        return newInternalNode(mutator, sizes, children);
    }

    static <E> @NonNull InternalNode<E> newInternalNode(@Nullable UniqueId mutator,
                                                        @NonNull int[] sizes, @NonNull Node<E>[] children) {
        return mutator == null ? new InternalNode<E>(sizes, children) : new MutableInternalNode<E>(mutator, sizes, children);
    }

    static <E> @NonNull LeafNode<E> newLeafNode(@Nullable UniqueId mutator, @NonNull E[] data) {
        return mutator == null ? new LeafNode<E>(data) : new MutableLeafNode<E>(mutator, data);
    }

    /**
     * Relaxed Radix Balanced Tree.
     * <p>
     * This is almost a trie, because the path from the root down to an element
     * is approximately encoded by its index.
     * <p>
     * Invariants:
     * <ul>
     *     <li>The data structure contains of a tree and of a tail.</li>
     *     <li>The tree consists of inner nodes and leaf nodes.</li>
     *     <li>The children of an inner node are inner nodes or leaf nodes.</li>
     *     <li>The children of a leaf node are data element.</li>
     *     <li>All leaf nodes are at the same depth in the tree.</li>
     *     <li>All nodes except the right-most have between {@code m - 2}
     *     and {@code m} children.</li>
     *     <li>The root node has between {@code 1} and {@code m} children.</li>
     *     <li>The tail has between {@code 0} and {@code m} data elements</li>
     *     <li>A tree of height {@code h} has between {@code (m-2)^h + |tail|}
     *     and {@code m^h + |tail|} elements.</li>
     * </ul>
     *
     * @param <E>
     */
    static class RrbTree<E> {
        private final int size;
        private final int shift;
        private final @NonNull LeafNode<E> head;
        private final @NonNull LeafNode<E> tail;
        private InternalNode<E> root;
        private final UniqueId mutator = new UniqueId();

        @SuppressWarnings({"unchecked", "rawtypes"})
        public RrbTree() {
            head = tail = (LeafNode<E>) EMPTY_LEAF;
            root = newInternalNode(mutator, new int[]{0}, new LeafNode[]{EMPTY_LEAF});
            size = 0;
            shift = 0;
        }

        public @NonNull E get(int index) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        public void add(E e) {
            @NonNull Node<E>[] newNodes = root.insert(mutator, (E[]) new Object[]{e}, size, shift);
            root = (InternalNode<E>) newNodes[0];
        }

        public int size() {
            return size;
        }
    }

    static abstract class Node<E> {
        protected abstract int getLength();

        @Nullable UniqueId getMutator() {
            return null;
        }

        boolean isAllowedToEdit(@Nullable UniqueId y) {
            UniqueId x = getMutator();
            return x != null && x == y;
        }

        protected abstract @NonNull Node<E> @NonNull [] insert(@Nullable UniqueId mutator,
                                                               @Nullable E[] elements,
                                                               int index,
                                                               int shift);

    }

    static class LeafNode<E> extends Node<E> {
        private @NonNull E[] data;

        LeafNode(@NonNull E[] newData) {
            this.data = newData;
        }

        @NonNull LeafNode<E> dec(@Nullable UniqueId mutator) {
            E[] newData = Arrays.copyOf(data, data.length - 1);
            if (isAllowedToEdit(mutator)) {
                data = newData;
                return this;
            } else {
                return new LeafNode<>(newData);
            }
        }

        @Override
        protected int getLength() {
            return data.length;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected @NonNull Node<E> @NonNull [] insert(@Nullable UniqueId mutator,
                                                      E @NonNull [] elements,
                                                      int index,
                                                      int shift) {
            E[] newData = ArrayHelper.copyAddAll(data, index, elements);
            if (isAllowedToEdit(mutator)) {
                this.data = newData;
                return new Node[]{this};
            } else {
                return new Node[]{newLeafNode(mutator, newData)};
            }
        }

        @NonNull LeafNode<E> inc(@Nullable UniqueId mutator) {
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

        public InternalNode(@NonNull int[] sizes, @NonNull Node<E>[] children) {
            this.sizes = sizes;
            this.children = children;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected @NonNull Node<E> @NonNull [] insert(@Nullable UniqueId mutator,
                                                      E @NonNull [] elements,
                                                      int index,
                                                      int shift) {
            int i = Arrays.binarySearch(sizes, index);
            if (i < 0) {
                i = ~i;
            }
            int length = getLength();
            Node<E>[] updated = children[i].insert(mutator, elements, index - sizes[i], shift - 1);
            if (updated.length == 1) {
                int insertLength = elements.length;
                int[] newSizes = Arrays.copyOf(sizes, sizes.length);
                for (int j = i; j < length; j++) {
                    newSizes[j] += insertLength;
                }
                @NonNull Node<E>[] newChildren = Arrays.copyOf(children, children.length);
                if (isAllowedToEdit(mutator)) {
                    this.sizes = newSizes;
                    this.children = newChildren;
                    return new Node[]{this};
                } else {
                    return new Node[]{newInternalNode(mutator, newSizes, newChildren)};
                }
            } else {
                throw new UnsupportedOperationException("implement me");
            }
        }

        protected int getSize(int i) {
            return i == 0 ? sizes[i] : sizes[i] - sizes[i - 1];
        }

        @Override
        protected int getLength() {
            return children.length;
        }

    }

    static class MutableLeafNode<E> extends LeafNode<E> {
        private final @NonNull UniqueId mutator;

        MutableLeafNode(@NonNull UniqueId mutator, @NonNull E[] data) {
            super(data);
            this.mutator = mutator;
        }

        @Override
        @NonNull UniqueId getMutator() {
            return mutator;
        }
    }

    static class MutableInternalNode<E> extends InternalNode<E> {
        private final @NonNull UniqueId mutator;

        MutableInternalNode(@NonNull UniqueId mutator, @NonNull int[] sizes, @NonNull Node<E>[] children) {
            super(sizes, children);
            this.mutator = mutator;
        }

        @Override
        @NonNull UniqueId getMutator() {
            return mutator;
        }
    }


    abstract static class TrieNode<E> {
        abstract E get(int index, int shift);

        abstract void set(int index, int shift, E e);
    }

    static class InnerTrieNode<E> extends TrieNode<E> {
        private final TrieNode<E>[] children;

        @SuppressWarnings({"unchecked", "rawtypes"})
        InnerTrieNode(int size, int shift) {
            int nodeCapacity = M << (shift);
            int childShift = shift - BIT_PARTITION_SIZE;
            int childCapacity = M << childShift;
            this.children = (TrieNode<E>[]) new TrieNode[(size + childCapacity - 1) / childCapacity];
            for (int i = 0; i < children.length; i++) {
                int childSize = Math.min(size - i * childCapacity, childCapacity);
                if (childShift == 0) {
                    children[i] = new LeafTrieNode<>(childSize);
                } else {
                    children[i] = new InnerTrieNode<>(childSize, childShift);
                }
            }
        }

        InnerTrieNode(TrieNode<E>[] children) {
            this.children = children;
        }


        @Override
        E get(int index, int shift) {
            return children[(index >>> shift) & BIT_MASK].get(index, shift - BIT_PARTITION_SIZE);
        }

        @Override
        void set(int index, int shift, E e) {
            TrieNode<E> child = children[(index >>> shift) & BIT_MASK];
            child.set(index, shift - BIT_PARTITION_SIZE, e);
        }
    }

    static class LeafTrieNode<E> extends TrieNode<E> {
        private final E @NonNull [] elements;

        @SuppressWarnings("unchecked")
        LeafTrieNode(int size) {
            this.elements = (E[]) new Object[size];
        }

        @Override
        E get(int index, int shift) {
            return elements[index & BIT_PARTITION_SIZE];
        }

        @Override
        void set(int index, int shift, E e) {
            elements[index & BIT_PARTITION_SIZE] = e;
        }
    }
}
