package org.jhotdraw8.icollection.impl.rrbnaive;

import org.jhotdraw8.icollection.impl.MutabilityOwnership;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class RrbTree<E> {
    public static final int WIDTH = 4;
    public static final int EPSILON = 1;
    public static final int AVERAGE_MIN_WIDTH = WIDTH - EPSILON;
    public static final int MIN_PARENT_WIDTH = WIDTH * (WIDTH - EPSILON) + 1;
    public static final int WIDTH2 = WIDTH * WIDTH;
    public static final int MASK = WIDTH - 1;
    public static final int LOG_WIDTH = Integer.numberOfTrailingZeros(WIDTH);
    private static final int MAX_SHIFT = 30;
    private static final int MAX_DEPTH = (32 + LOG_WIDTH - 1) / LOG_WIDTH;
    private int height;
    private int size;
    RrbNode<E> root;
    private int shift;

    public int size() {
        return size;
    }

    public static final RrbTree<?> EMPTY = new RrbTree<Object>(0, 0, RrbNode.empty(), 0);

    public static <E> RrbTree<E> empty() {
        return (RrbTree<E>) EMPTY;
    }

    public RrbTree(int height, int size, RrbNode<E> root, int shift) {
        this.height = height;
        this.size = size;
        this.root = root;
        this.shift = shift;
    }

    /// Creates a sparse tree that contains size elements initialized with the
    /// specified value.
    public static <E> RrbTree<E> initSparse(int size, E value, MutabilityOwnership owner) {
        if (size == 0) {
            return empty();
        }
        // Create a sparse tree that is potentially too large
        int height = minDepthForSize(size);
        if (height > MAX_DEPTH) {
            throw new IllegalArgumentException("size is too large");
        }
        RrbNode<E>[] stack = (RrbNode<E>[]) new RrbNode[height + 1];
        Object[] values = new Object[WIDTH];
        int generatedSize = WIDTH;
        Arrays.fill(values, value);
        // this node can not be owned because it is shared within the tree
        stack[0] = new RrbNode<>(values, null, null);
        int remaining = size;

        for (int currentDepth = 1; currentDepth <= height; currentDepth++) {
            Object[] nodes = new Object[WIDTH];
            Arrays.fill(nodes, stack[currentDepth - 1]);
            remaining = remaining >> LOG_WIDTH;
            // this node can not be owned because it is shared within the tree
            stack[currentDepth] = new RrbNode(nodes, null, null);
            generatedSize = generatedSize * WIDTH;
        }

        // Slice the tree down
        int treeShift = shiftForHeight(height);
        RrbTree<E> tree1 = new RrbTree<>(height, maxCapacity(height), stack[height], treeShift);
        return tree1.takingFirst(size, owner);
    }

    /// Computes the maximal capacity for a fully populated tree at the specified height.
    private static int maxCapacity(int height) {
        return WIDTH << ((height) * LOG_WIDTH);
    }

    /// Computes the minimal capacity for a tree at the specified height.
    private static int minCapacity(int height) {
        return height == 0 ? WIDTH : Math.powExact(AVERAGE_MIN_WIDTH, height);
    }

    private static int shiftForHeight(int height) {
        return LOG_WIDTH * (height);
    }

    /// Computes the minimally required height for the specified size.
    private static int minDepthForSize(int size) {
        return logBaseN(size, WIDTH);
    }

    /// Returns a tree that only contains the first n elements
    public RrbTree<E> takingFirst(int n, MutabilityOwnership owner) {
        if (size <= n) return this;
        if (n == 0) return empty();
        var newRoot = takingFirstRecursive(root, n, shift, owner);
        int newHeight = height;
        while (newHeight > 0 && newRoot.getWidth() == 1) {
            newRoot = newRoot.get(0);
            newHeight = height - 1;
        }
        return new RrbTree<E>(newHeight, n, newRoot, shiftForHeight(newHeight));
    }

    private RrbNode<E> takingFirstRecursive(RrbNode<E> node, int key, int shift, MutabilityOwnership owner) {
        var newNode = node.makeOwned(owner);
        if (shift == 0) {
            int index = key & MASK;
            newNode.children = Arrays.copyOf(node.children, index);
        } else {
            int index = node.getIndex(key, shift);
            var childNode = takingFirstRecursive(node.get(index), key, shift - LOG_WIDTH, owner);
            if (childNode.isEmpty()) {
                newNode.children = Arrays.copyOf(newNode.children, index);
            } else {
                newNode.children = Arrays.copyOf(newNode.children, index + 1);
                newNode.setChild(index, childNode);
            }
        }
        return newNode;
    }

    /// Gets a value at the specified index
    public @Nullable E getAt(int key) {
        RrbNode<E> node = root;
        for (int s = shift; s > 0; s -= LOG_WIDTH) {
            int index = node.getIndex(key, s);
            node = node.get(index);
        }
        int index = node.getIndex(key, 0);
        return node.getValue(index);
    }

    public RrbTree<E> settingAt(int key, E value, MutabilityOwnership owner) {
        Objects.checkIndex(key, size);
        var newRoot = root.makeOwned(owner);
        RrbNode<E> node = newRoot;
        for (int s = shift; s > 0; s -= LOG_WIDTH) {
            int index = node.getIndex(key, s);
            var newChild = node.get(index).makeOwned(owner);
            node.setChild(index, newChild);
            node = newChild;
        }
        int index = node.getIndex(key, 0);
        node.setValue(index, value);
        return new RrbTree<E>(height, size, newRoot, shift);
    }

    /// Adds a new value to the end of the tree
    public RrbTree<E> addingLast(E value, MutabilityOwnership owner) {
        RrbNode<E> newRoot;
        int newHeight, newShift;
        if (root.getWidth() >= WIDTH - 1 && minCapacity(height) <= size) {
            newRoot = new RrbNode<>(new Object[]{root.makeOwned(owner)}, null, owner);
            newHeight = height + 1;
            newShift = shift + LOG_WIDTH;
        } else {
            newRoot = root.makeOwned(owner);
            newHeight = height;
            newShift = shift;
        }
        RrbNode<E> node = newRoot;
        int key = size;
        for (int s = newShift; s > 0; s -= LOG_WIDTH) {
            int index = node.getIndex(key, s);
            RrbNode<E> newChild;
            if (index < node.getWidth()) {
                RrbNode<E> child = node.get(index);
                if (child == null) {
                    newChild = new RrbNode<>(new Object[1], null, owner);
                } else {
                    newChild = child.makeOwned(owner);
                }
            } else {
                node.children = Arrays.copyOf(node.children, node.children.length + 1);
                newChild = new RrbNode<>(new Object[1], null, owner);
            }
            node.setChild(index, newChild);
            node = newChild;
        }
        int index = node.getIndex(key, 0);
        if (index >= node.getWidth()) {
            node.children = Arrays.copyOf(node.children, node.children.length + 1);
        }
        node.setValue(node.children.length - 1, value);
        if (newHeight > 0 && newRoot.getWidth() == 1) {
            newRoot = newRoot.get(0);
            newHeight--;
            newShift -= LOG_WIDTH;
        }
        return new RrbTree<E>(newHeight, size + 1, newRoot, newShift);
    }

    /// Removes the value at the end of the tree
    public RrbTree<E> removingLast(MutabilityOwnership owner) {
        if (size == 1) return empty();
        var newRoot = removingLastRecursive(root, size - 1, shift, owner);
        int newHeight;
        if (height > 0 && newRoot.getWidth() == 1) {
            newRoot = newRoot.get(0);
            newHeight = height - 1;
        } else {
            newHeight = height;
        }
        return new RrbTree<E>(newHeight, size - 1, newRoot, shiftForHeight(newHeight));
    }

    private RrbNode<E> removingLastRecursive(RrbNode<E> node, int key, int shift, MutabilityOwnership owner) {
        var newNode = node.makeOwned(owner);
        if (shift == 0) {
            node.children = Arrays.copyOf(node.children, node.children.length - 1);
        } else {
            int index = node.getIndex(key, shift);
            var childNode = removingLastRecursive(node.get(index), key, shift - LOG_WIDTH, owner);
            if (childNode.isEmpty()) {
                newNode.children = Arrays.copyOf(node.children, node.children.length - 1);
            } else {
                newNode.setChild(index, childNode);
            }
        }
        return newNode;
    }


    private static int logBaseN(int x, int n) {
        if (x <= 0) {
            return 0;
        }
        if (n < 2 || (n & (n - 1)) != 0) {
            throw new IllegalArgumentException("Base n must be a power of 2 and >= 2");
        }
        int log2X = 31 - Integer.numberOfLeadingZeros(x);
        int log2N = 31 - Integer.numberOfLeadingZeros(n);
        return log2X / log2N;
    }
}
