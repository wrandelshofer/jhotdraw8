/*
 * @(#)RedBlackTree.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.SimpleImmutableList;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
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
 * @param <K> the key type
 * @param <V> the value type
 */
public interface RedBlackTree<K, V> extends Iterable<Node<K, V>> {
    String toLispString();

    static <K, V> RedBlackTree<K, V> empty() {
        return Empty.empty();
    }

    static <K, V> RedBlackTree<K, V> of(@NonNull Comparator<? super K> comparator, K key, V value) {
        Objects.requireNonNull(comparator, "comparator is null");
        final Empty<K, V> empty = Empty.empty();
        return new Node<>(Color.BLACK, empty, key, value, empty);
    }

    @SafeVarargs
    static <K, V> RedBlackTree<K, V> of(@NonNull Comparator<? super K> comparator, K... keys) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(keys, "values is null");
        RedBlackTree<K, V> tree = empty();
        for (K key : keys) {
            tree = tree.insert(key, null, comparator);
        }
        return tree;
    }

    @SuppressWarnings("unchecked")
    static <K, V> RedBlackTree<K, V> ofAll(@NonNull Comparator<? super K> comparator, Iterable<? extends K> keys) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(keys, "values is null");
        // function equality is not computable => same object check
        //if (values instanceof RedBlackTree && ((RedBlackTree<K>) values).comparator() == comparator) {
        //     return (RedBlackTree<K>) values;
        // } else {
        RedBlackTree<K, V> tree = empty();
        for (K key : keys) {
            tree = tree.insert(key, null, comparator);
        }
        return tree;
        //}
    }

    /**
     * Inserts a new value into this tree.
     *
     * @param key        A key.
     * @param value      A value.
     * @param comparator
     * @return A new tree if this tree does not contain the given value, otherwise the same tree instance.
     */
    default RedBlackTree<K, V> insert(K key, V value, Comparator<? super K> comparator) {
        return Node.insert(this, key, value, comparator).color(Color.BLACK);
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
     * Checks, if this {@code RedBlackTree} contains the given {@code key}.
     *
     * @param key      A key.
     * @param comparator
     * @return true, if this tree contains the value, false otherwise.
     */
    boolean contains(K key, Comparator<? super K> comparator);

    boolean isRed();

    /**
     * Deletes a value from this RedBlackTree.
     *
     * @param key      A value
     * @param comparator
     * @return A new RedBlackTree if the value is present, otherwise this.
     */
    default RedBlackTree<K, V> delete(K key, Comparator<? super K> comparator) {
        final RedBlackTree<K, V> tree = Node.delete(this, key, comparator)._1();
        return tree.size() == this.size() ? this : Node.color(tree, Color.BLACK);
    }



    /**
     * Finds the value stored in this tree, if exists, by applying the underlying comparator to the tree elements and
     * the given element.
     * <p>
     * Especially the value returned may differ from the given value, even if the underlying comparator states that
     * both are equal.
     *
     * @param key      A value
     * @param comparator
     * @return Some value, if this tree contains a value equal to the given value according to the underlying comparator. Otherwise None.
     */
    RedBlackTree<K, V> find(K key, Comparator<? super K> comparator);


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
    RedBlackTree<K, V> left();

    /**
     * Returns the maximum element of this tree according to the underlying comparator.
     *
     * @return Node, if this is not empty, otherwise Empty
     */
    default @NonNull RedBlackTree<K, V> max() {
        return isEmpty() ? RedBlackTree.empty() : Node.maximum((Node<K, V>) this);
    }

    /**
     * Returns the minimum element of this tree according to the underlying comparator.
     *
     * @return Node, if this is not empty, otherwise Empty
     */
    default @NonNull RedBlackTree<K, V> min() {
        return isEmpty() ? RedBlackTree.empty() : Node.minimum((Node<K, V>) this);
    }

    /**
     * Returns the right child if this is a non-empty node, otherwise throws.
     *
     * @return The right child.
     * @throws UnsupportedOperationException if this RedBlackTree is empty
     */
    @NonNull RedBlackTree<K, V> right();

    /**
     * Returns the size of this tree.
     *
     * @return the number of nodes of this tree and 0 if this is the empty tree
     */
    int size();


    /**
     * Adds all the elements of the given {@code tree} to this tree, if not already present.
     *
     * @param tree       The RedBlackTree to form the union with.
     * @param comparator
     * @return A new RedBlackTree that contains all distinct elements of this and the given {@code tree}.
     */


    /**
     * Returns true if the given tree has the same size and the same elements in the same sequence as this tree.
     *
     * @param tree the given tree
     * @return true if equal
     */
    default boolean equals(@NonNull RedBlackTree<K, V> tree) {
        if (this == tree) return true;
        if (!(this.size() == tree.size())) return false;
        Iterator<Node<K, V>> ia = this.iterator();
        Iterator<Node<K, V>> ib = tree.iterator();
        for (int i = 0, n = this.size(); i < n; i++) {
            var a = ia.next();
            var b = ib.next();
            if (!Objects.equals(a, b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the key of the current tree node or throws if this is empty.
     *
     * @return The value.
     * @throws NoSuchElementException if this is the empty node.
     */
    K getKey();

    /**
     * Returns the value of the current tree node or throws if this is empty.
     *
     * @return The value.
     * @throws NoSuchElementException if this is the empty node.
     */
    V getValue();

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

    default Iterator<Node<K, V>> iterator() {
        if (isEmpty()) {
            return SimpleImmutableList.<Node<K, V>>of().iterator();
        } else {
            final Node<K, V> that = (Node<K, V>) this;
            return new Iterator<Node<K, V>>() {

                final Deque<Node<K, V>> stack = pushLeftChildren(new ArrayDeque<>(), that);

                @Override
                public boolean hasNext() {
                    return !stack.isEmpty();
                }

                @Override
                public Node<K, V> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Node<K, V> node = stack.pop();
                    if (!node.right.isEmpty()) pushLeftChildren(stack, (Node<K, V>) node.right);
                    return node;
                }

                private Deque<Node<K, V>> pushLeftChildren(Deque<Node<K, V>> stack, Node<K, V> that) {
                    RedBlackTree<K, V> tree = that;
                    while (!tree.isEmpty()) {
                        final Node<K, V> node = (Node<K, V>) tree;
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
    default Iterator<Node<K, V>> reverseIterator() {
        if (isEmpty()) {
            return SimpleImmutableList.<Node<K, V>>of().iterator();
        } else {
            final Node<K, V> that = (Node<K, V>) this;
            return new Iterator<Node<K, V>>() {

                final Deque<Node<K, V>> stack = pushRightChildren(new ArrayDeque<>(), that);

                @Override
                public boolean hasNext() {
                    return !stack.isEmpty();
                }

                @Override
                public Node<K, V> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Node<K, V> node = stack.pop();
                    if (!node.left.isEmpty()) pushRightChildren(stack, (Node<K, V>) node.left);
                    return node;
                }

                private Deque<Node<K, V>> pushRightChildren(Deque<Node<K, V>> stack, Node<K, V> that) {
                    RedBlackTree<K, V> tree = that;
                    while (!tree.isEmpty()) {
                        final Node<K, V> node = (Node<K, V>) tree;
                        stack.push(node);
                        tree = node.right;
                    }
                    return stack;
                }
            };
        }
    }



    /**
     * Returns the least {@code Node} in this tree greater than or equal to
     * the given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<K, V> ceiling(K e, @NonNull Comparator<? super K> comparator);

    /**
     * Returns the greatest {@code Node} in this tree less than or equal to
     * the given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<K, V> floor(K e, @NonNull Comparator<? super K> comparator);

    /**
     * Returns the least {@code Node} in this tree strictly greater than the
     * given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<K, V> higher(K e, @NonNull Comparator<? super K> comparator);

    /**
     * Returns the greatest {@code Node} in this tree strictly less than the
     * given element, or {@code Empty} if there is no such element.
     */
    @NonNull RedBlackTree<K, V> lower(K e, @NonNull Comparator<? super K> comparator);

    /**
     * Returns this {@code RedBlackTree} if it is nonempty, otherwise return the alternative.
     */
    @SuppressWarnings("unchecked")
    RedBlackTree<K, V> orElse(RedBlackTree<K, V> other);

    /**
     * Returns the key of this RedBlackTree or {@code null} if it is empty.
     */
    @Nullable K keyOrNull();
    /**
     * Returns the value of this RedBlackTree or {@code null} if it is empty.
     */
    @Nullable V valueOrNull();

    /**
     * Returns the map entry of this RedBlackTree or {@code null} if it is empty.
     */
    Map.@Nullable Entry<K, V> entryOrNull();

    /*
     * Returns the mapped value of this RedBlackTree or {@code null} if it is empty.
     */
    @Nullable <E> E mapOrNull(@NonNull BiFunction<K, V, E> f);


}

