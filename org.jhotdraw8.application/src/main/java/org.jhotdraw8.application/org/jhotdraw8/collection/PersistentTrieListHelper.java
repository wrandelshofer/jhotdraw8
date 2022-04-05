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
class PersistentTrieListHelper {
    final static LeafNode<?> EMPTY_LEAF = new LeafNode<>(new Object[0]);
    static final int BIT_PARTITION_SIZE = 5;
    static final int M = 32;

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

        int[] mergedSizes = new int[leftLen + centreLen + rightLen];
        @SuppressWarnings("unchecked")
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

    static <E> @NonNull InternalNode<E> newAbove1(@Nullable UniqueIdentity mutator, @NonNull Node<E> child) {
        int[] sizes = new int[]{child.getLength()};
        @SuppressWarnings("unchecked") Node<E>[] children = new Node[]{child};
        return newInternalNode(mutator, sizes, children);
    }

    static <E> @NonNull InternalNode<E> newInternalNode(@Nullable UniqueIdentity mutator,
                                                        @NonNull int[] sizes, @NonNull Node<E>[] children) {
        return mutator == null ? new InternalNode<E>(sizes, children) : new MutableInternalNode<E>(mutator, sizes, children);
    }

    static <E> @NonNull LeafNode<E> newLeafNode(@Nullable UniqueIdentity mutator, @NonNull E[] data) {
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
     *     <li>All nodes except the root have between {@code m - epsilon - 1}
     *     and {@code m} children.</li>
     *     <li>The root node has between {@code 1} and {@code m} children.</li>
     *     <li>The tail has between {@code 0} and {@code m} data elements</li>
     * </ul>
     *
     * @param <E>
     */
    static class RrbTree<E> {
        private int size;
        private int shift;
        private LeafNode<E> head;
        private LeafNode<E> tail;
        private InternalNode<E> root;
        private UniqueIdentity mutator = new UniqueIdentity();

        @SuppressWarnings("unchecked")
        public RrbTree() {
            head = tail = (LeafNode<E>) EMPTY_LEAF;
            root = newInternalNode(mutator, new int[]{0}, new LeafNode[]{EMPTY_LEAF});
            size = 0;
            shift = 0;
        }

        public E get(int index) {
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

        @Nullable UniqueIdentity getMutator() {
            return null;
        }

        boolean isAllowedToEdit(@Nullable UniqueIdentity y) {
            UniqueIdentity x = getMutator();
            return x != null && x == y;
        }

        @SuppressWarnings("unchecked")
        protected abstract @NonNull Node<E>[] insert(@Nullable UniqueIdentity mutator,
                                                     @Nullable E[] elements,
                                                     int index,
                                                     int shift);

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

        @Override
        protected int getLength() {
            return data.length;
        }

        @SuppressWarnings("unchecked")
        protected @NonNull Node<E>[] insert(@Nullable UniqueIdentity mutator,
                                            @Nullable E[] elements,
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

        public InternalNode(@NonNull int[] sizes, @NonNull Node<E>[] children) {
            this.sizes = sizes;
            this.children = children;
        }

        @SuppressWarnings("unchecked")
        protected @NonNull Node<E>[] insert(@Nullable UniqueIdentity mutator,
                                            @Nullable E[] elements,
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

        MutableInternalNode(@NonNull UniqueIdentity mutator, @NonNull int[] sizes, @NonNull Node<E>[] children) {
            super(sizes, children);
            this.mutator = mutator;
        }

        @NonNull UniqueIdentity getMutator() {
            return mutator;
        }
    }


}
