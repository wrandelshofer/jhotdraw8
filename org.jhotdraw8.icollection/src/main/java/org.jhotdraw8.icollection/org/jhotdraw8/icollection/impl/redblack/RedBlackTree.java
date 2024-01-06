/*
 * @(#)RedBlackTree.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.ImmutableLists;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;


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
public interface RedBlackTree<T> {

    static <T> RedBlackTree<T> empty() {
        return Empty.empty();
    }

    static <T> RedBlackTree<T> of(@NonNull Comparator<? super T> comparator, @NonNull BiFunction<T, T, T> updateFunction, T value) {
        Objects.requireNonNull(comparator, "comparator is null");
        final Empty<T> empty = Empty.empty();
        return new Node<>(Color.BLACK, 1, empty, value, empty);
    }

    @SafeVarargs
    static <T> RedBlackTree<T> of(@NonNull Comparator<? super T> comparator, @NonNull BiFunction<T, T, T> updateFunction, T... values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        RedBlackTree<T> tree = empty();
        for (T value : values) {
            tree = tree.insert(value, comparator, updateFunction);
        }
        return tree;
    }

    @SuppressWarnings("unchecked")
    static <T> RedBlackTree<T> ofAll(@NonNull Comparator<? super T> comparator, @NonNull BiFunction<T, T, T> updateFunction, Iterable<? extends T> values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        // function equality is not computable => same object check
        //if (values instanceof RedBlackTree && ((RedBlackTree<T>) values).comparator() == comparator) {
        //     return (RedBlackTree<T>) values;
        // } else {
        RedBlackTree<T> tree = empty();
        for (T value : values) {
            tree = tree.insert(value, comparator, updateFunction);
        }
        return tree;
        //}
    }

    /**
     * Inserts a new value into this tree.
     *
     * @param value          A value.
     * @param comparator
     * @param updateFunction
     * @return A new tree if this tree does not contain the given value, otherwise the same tree instance.
     */
    default RedBlackTree<T> insert(T value, Comparator<? super T> comparator, BiFunction<T, T, T> updateFunction) {
        return Node.insert(this, value, comparator, updateFunction).color(Color.BLACK);
    }

    /**
     * Return the {@link Color} of this Red/Black Tree node.
     * <p>
     * An empty node is {@code BLACK} by definition.
     *
     * @return Either {@code RED} or {@code BLACK}.
     */
    boolean color();


    /**
     * Checks, if this {@code RedBlackTree} contains the given {@code value}.
     *
     * @param value      A value.
     * @param comparator
     * @return true, if this tree contains the value, false otherwise.
     */
    boolean contains(T value, Comparator<? super T> comparator);

    /**
     * Deletes a value from this RedBlackTree.
     *
     * @param value      A value
     * @param comparator
     * @return A new RedBlackTree if the value is present, otherwise this.
     */
    default RedBlackTree<T> delete(T value, Comparator<? super T> comparator) {
        final RedBlackTree<T> tree = Node.delete(this, value, comparator)._1;
        return tree.size() == this.size() ? this : Node.color(tree, Color.BLACK);
    }

    default RedBlackTree<T> difference(@NonNull RedBlackTree<T> tree, @NonNull Comparator<? super T> comparator, @NonNull BiFunction<T, T, T> updateFunction) {
        Objects.requireNonNull(tree, "tree is null");
        if (isEmpty() || tree.isEmpty()) {
            return this;
        } else {
            final Node<T> that = (Node<T>) tree;
            final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(this, that.value, comparator, updateFunction);
            return Node.merge(split._1.difference(that.left, comparator, updateFunction), split._2.difference(that.right, comparator, updateFunction));
        }
    }


    /**
     * Finds the value stored in this tree, if exists, by applying the underlying comparator to the tree elements and
     * the given element.
     * <p>
     * Especially the value returned may differ from the given value, even if the underlying comparator states that
     * both are equal.
     *
     * @param value      A value
     * @param comparator
     * @return Some value, if this tree contains a value equal to the given value according to the underlying comparator. Otherwise None.
     */
    RedBlackTree<T> find(T value, Comparator<? super T> comparator);

    default RedBlackTree<T> intersection(@NonNull RedBlackTree<T> tree, @NonNull Comparator<? super T> comparator, @NonNull BiFunction<T, T, T> updateFunction) {
        Objects.requireNonNull(tree, "tree is null");
        if (isEmpty()) {
            return this;
        } else if (tree.isEmpty()) {
            return tree;
        } else {
            final Node<T> that = (Node<T>) tree;
            final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(this, that.value, comparator, updateFunction);
            if (contains(that.value, comparator)) {
                return Node.join(split._1.intersection(that.left, comparator, updateFunction), that.value, split._2.intersection(that.right, comparator, updateFunction), comparator, updateFunction);
            } else {
                return Node.merge(split._1.intersection(that.left, comparator, updateFunction), split._2.intersection(that.right, comparator, updateFunction));
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
     * @param tree           The RedBlackTree to form the union with.
     * @param comparator
     * @param updateFunction
     * @return the same RedBlackTree, if it already contains all elements of the given {@code tree},
     * otherwise a new RedBlackTree
     */
    default RedBlackTree<T> addAll(@NonNull RedBlackTree<T> tree, @NonNull Comparator<? super T> comparator, @NonNull BiFunction<T, T, T> updateFunction) {
        RedBlackTree<T> newTree = union(tree, comparator, updateFunction);
        return newTree.size() == this.size() ? this : newTree;
    }

    /**
     * Adds all the elements of the given {@code tree} to this tree, if not already present.
     *
     * @param tree           The RedBlackTree to form the union with.
     * @param comparator
     * @param updateFunction
     * @return A new RedBlackTree that contains all distinct elements of this and the given {@code tree}.
     */
    default RedBlackTree<T> union(@NonNull RedBlackTree<T> tree, @NonNull Comparator<? super T> comparator, @NonNull BiFunction<T, T, T> updateFunction) {
        Objects.requireNonNull(tree, "tree is null");
        if (tree.isEmpty()) {
            return this;
        } else {
            final Node<T> that = (Node<T>) tree;
            if (isEmpty()) {
                return that.color(Color.BLACK);
            } else {
                final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(this, that.value, comparator, updateFunction);
                return Node.join(
                        split._1.union(that.left, comparator, updateFunction),
                        that.value,
                        split._2.union(that.right, comparator, updateFunction),
                        comparator, updateFunction);
            }
        }
    }

    /**
     * Returns true if the given tree has the same size and the same elements in the same sequence as this tree.
     *
     * @param tree the given tree
     * @return true if equal
     */
    default boolean equals(@NonNull RedBlackTree<T> tree) {
        if (this == tree) return true;
        if (!(this.size() == tree.size())) return false;
        Iterator<T> a = this.iterator();
        Iterator<T> b = tree.iterator();
        for (int i = 0, n = this.size(); i < n; i++) {
            T va = a.next();
            T vb = b.next();
            if (!Objects.equals(va, vb)) {
                return false;
            }
        }
        return true;
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

                private Deque<Node<T>> pushLeftChildren(Deque<Node<T>> stack, Node<T> that) {
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
     * Iteration order: 7, 6, 5, 4, 3, 2, 1
     * <p>
     * See also <a href="http://n00tc0d3r.blogspot.de/2013/08/implement-iterator-for-binarytree-i-in.html">Implement Iterator for BinaryTree I (In-order)</a>.
     */

    default Iterator<T> reverseIterator() {
        if (isEmpty()) {
            return ImmutableLists.<T>of().iterator();
        } else {
            final Node<T> that = (Node<T>) this;
            return new Iterator<T>() {

                Deque<Node<T>> stack = pushRightChildren(new ArrayDeque<>(), that);

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
                    if (!node.left.isEmpty()) pushRightChildren(stack, (Node<T>) node.left);
                    return node.value;
                }

                private Deque<Node<T>> pushRightChildren(Deque<Node<T>> stack, Node<T> that) {
                    RedBlackTree<T> tree = that;
                    while (!tree.isEmpty()) {
                        final Node<T> node = (Node<T>) tree;
                        stack.push(node);
                        tree = node.right;
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

    /**
     * Returns the least {@code Node} in this tree greater than or equal to
     * the given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<T> ceiling(T e, @NonNull Comparator<? super T> comparator);

    /**
     * Returns the greatest {@code Node} in this tree less than or equal to
     * the given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<T> floor(T e, @NonNull Comparator<? super T> comparator);

    /**
     * Returns the least {@code Node} in this tree strictly greater than the
     * given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<T> higher(T e, @NonNull Comparator<? super T> comparator);

    /**
     * Returns the greatest {@code Node} in this tree strictly less than the
     * given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<T> lower(T e, @NonNull Comparator<? super T> comparator);

    /**
     * Returns this {@code RedBlackTree} if it is nonempty, otherwise return the alternative.
     */
    @SuppressWarnings("unchecked")
    RedBlackTree<T> orElse(RedBlackTree<T> other);

    /**
     * Returns the value of this RedBlackTree or {@code null} if it is empty.
     */
    @Nullable T orNull();

}

