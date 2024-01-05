/*
 * @(#)RedBlackTree.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;


import org.jhotdraw8.icollection.ImmutableLists;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;


/**
 * Purely functional Red/Black Tree, inspired by <a href="https://github.com/kazu-yamamoto/llrbtree/blob/master/Data/Set/RBTree.hs">Kazu Yamamoto's Haskell implementation</a>.
 * <p>
 * Based on
 * <dl>
 * <dt><a href="http://www.eecs.usma.edu/webs/people/okasaki/pubs.html#jfp99">Chris Okasaki, "Red-Black Trees in a Functional Setting", Journal of Functional Programming, 9(4), pp 471-477, July 1999</a></dt>
 * <dt>Stefan Kahrs, "Red-black trees with types", Journal of functional programming, 11(04), pp 425-432, July 2001</dt>
 * </dl>
 * <p>
 * This class has been derived from 'vavr' RedBlackTree.java.
 * <dl>
 *     <dt>RedBlackTree.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/RedBlackTree.java">github.com</a></dd>
 * </dl>
 *
 * @param <T> Component type
 */
interface RedBlackTree<T> extends Iterable<T> {

    static <T> RedBlackTree<T> empty(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return new Empty<>(comparator);
    }

    static <T> RedBlackTree<T> of(Comparator<? super T> comparator, T value) {
        Objects.requireNonNull(comparator, "comparator is null");
        final Empty<T> empty = new Empty<>(comparator);
        return new Node<>(Color.BLACK, 1, empty, value, empty, empty);
    }

    @SafeVarargs
    static <T> RedBlackTree<T> of(Comparator<? super T> comparator, T... values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        RedBlackTree<T> tree = empty(comparator);
        for (T value : values) {
            tree = tree.insert(value);
        }
        return tree;
    }

    @SuppressWarnings("unchecked")
    static <T> RedBlackTree<T> ofAll(Comparator<? super T> comparator, Iterable<? extends T> values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        // function equality is not computable => same object check
        if (values instanceof RedBlackTree && ((RedBlackTree<T>) values).comparator() == comparator) {
            return (RedBlackTree<T>) values;
        } else {
            RedBlackTree<T> tree = empty(comparator);
            for (T value : values) {
                tree = tree.insert(value);
            }
            return tree;
        }
    }

    /**
     * Inserts a new value into this tree.
     *
     * @param value A value.
     * @return A new tree if this tree does not contain the given value, otherwise the same tree instance.
     */
    default RedBlackTree<T> insert(T value) {
        return Node.insert(this, value).color(Color.BLACK);
    }

    /**
     * Return the {@link Color} of this Red/Black Tree node.
     * <p>
     * An empty node is {@code BLACK} by definition.
     *
     * @return Either {@code RED} or {@code BLACK}.
     */
    Color color();

    /**
     * Returns the underlying {@link Comparator} of this RedBlackTree.
     *
     * @return The comparator.
     */
    Comparator<T> comparator();

    /**
     * Checks, if this {@code RedBlackTree} contains the given {@code value}.
     *
     * @param value A value.
     * @return true, if this tree contains the value, false otherwise.
     */
    boolean contains(T value);

    /**
     * Deletes a value from this RedBlackTree.
     *
     * @param value A value
     * @return A new RedBlackTree if the value is present, otherwise this.
     */
    default RedBlackTree<T> delete(T value) {
        final RedBlackTree<T> tree = Node.delete(this, value)._1;
        return Node.color(tree, Color.BLACK);
    }

    default RedBlackTree<T> difference(RedBlackTree<T> tree) {
        Objects.requireNonNull(tree, "tree is null");
        if (isEmpty() || tree.isEmpty()) {
            return this;
        } else {
            final Node<T> that = (Node<T>) tree;
            final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(this, that.value);
            return Node.merge(split._1.difference(that.left), split._2.difference(that.right));
        }
    }

    /**
     * Returns the empty instance of this RedBlackTree.
     *
     * @return An empty ReadBlackTree
     */
    RedBlackTree<T> emptyInstance();

    /**
     * Finds the value stored in this tree, if exists, by applying the underlying comparator to the tree elements and
     * the given element.
     * <p>
     * Especially the value returned may differ from the given value, even if the underlying comparator states that
     * both are equal.
     *
     * @param value A value
     * @return Some value, if this tree contains a value equal to the given value according to the underlying comparator. Otherwise None.
     */
    Option<T> find(T value);

    default RedBlackTree<T> intersection(RedBlackTree<T> tree) {
        Objects.requireNonNull(tree, "tree is null");
        if (isEmpty()) {
            return this;
        } else if (tree.isEmpty()) {
            return tree;
        } else {
            final Node<T> that = (Node<T>) tree;
            final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(this, that.value);
            if (contains(that.value)) {
                return Node.join(split._1.intersection(that.left), that.value, split._2.intersection(that.right));
            } else {
                return Node.merge(split._1.intersection(that.left), split._2.intersection(that.right));
            }
        }
    }

    /**
     * Checks if this {@code RedBlackTree} is empty, i.e. an instance of {@code Leaf}.
     *
     * @return true, if it is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the left child if this is a non-empty node, otherwise throws.
     *
     * @return The left child.
     * @throws UnsupportedOperationException if this RedBlackTree is empty
     */
    RedBlackTree<T> left();

    /**
     * Returns the maximum element of this tree according to the underlying comparator.
     *
     * @return Some element, if this is not empty, otherwise None
     */
    default Option<T> max() {
        return isEmpty() ? Option.none() : Option.some(Node.maximum((Node<T>) this));
    }

    /**
     * Returns the minimum element of this tree according to the underlying comparator.
     *
     * @return Some element, if this is not empty, otherwise None
     */
    default Option<T> min() {
        return isEmpty() ? Option.none() : Option.some(Node.minimum((Node<T>) this));
    }

    /**
     * Returns the right child if this is a non-empty node, otherwise throws.
     *
     * @return The right child.
     * @throws UnsupportedOperationException if this RedBlackTree is empty
     */
    RedBlackTree<T> right();

    /**
     * Returns the size of this tree.
     *
     * @return the number of nodes of this tree and 0 if this is the empty tree
     */
    int size();

    /**
     * Adds all the elements of the given {@code tree} to this tree, if not already present.
     *
     * @param tree The RedBlackTree to form the union with.
     * @return A new RedBlackTree that contains all distinct elements of this and the given {@code tree}.
     */
    default RedBlackTree<T> union(RedBlackTree<T> tree) {
        Objects.requireNonNull(tree, "tree is null");
        if (tree.isEmpty()) {
            return this;
        } else {
            final Node<T> that = (Node<T>) tree;
            if (isEmpty()) {
                return that.color(Color.BLACK);
            } else {
                final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(this, that.value);
                return Node.join(split._1.union(that.left), that.value, split._2.union(that.right));
            }
        }
    }

    /**
     * Returns the value of the current tree node or throws if this is empty.
     *
     * @return The value.
     * @throws NoSuchElementException if this is the empty node.
     */
    T value();

    /**
     * Returns an Iterator that iterates elements in the order induced by the underlying Comparator.
     * <p>
     * Internally an in-order traversal of the RedBlackTree is performed.
     * <p>
     * Example:
     *
     * <pre><code>
     *       4
     *      / \
     *     2   6
     *    / \ / \
     *   1  3 5  7
     * </code></pre>
     * <p>
     * Iteration order: 1, 2, 3, 4, 5, 6, 7
     * <p>
     * See also <a href="http://n00tc0d3r.blogspot.de/2013/08/implement-iterator-for-binarytree-i-in.html">Implement Iterator for BinaryTree I (In-order)</a>.
     */
    @Override
    default Iterator<T> iterator() {
        if (isEmpty()) {
            return ImmutableLists.<T>of().iterator();
        } else {
            final Node<T> that = (Node<T>) this;
            return new Iterator<T>() {

                Deque<Node<T>> stack = pushLeftChildren(new ArrayDeque<>(), that);

                @Override
                public boolean hasNext() {
                    return !stack.isEmpty();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Node<T> node = stack.pop();
                    if (!node.right.isEmpty()) pushLeftChildren(stack, (Node<T>) node.right);
                    return node.value;
                }

                private Deque<Node<T>> pushLeftChildren(Deque<Node<T>> initialStack, Node<T> that) {
                    Deque<Node<T>> stack = initialStack;
                    RedBlackTree<T> tree = that;
                    while (!tree.isEmpty()) {
                        final Node<T> node = (Node<T>) tree;
                        stack.push(node);
                        tree = node.left;
                    }
                    return stack;
                }
            };
        }
    }

    /**
     * Returns a Lisp like representation of this tree.
     *
     * @return This Tree as Lisp like String.
     */
    @Override
    String toString();

}

