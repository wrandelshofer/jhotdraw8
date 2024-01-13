/*
 * @(#)Node.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A non-empty tree node.
 * <p>
 * This class has been derived from 'vavr' RedBlackTree.java.
 * <dl>
 *     <dt>RedBlackTree.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/RedBlackTree.java">github.com</a></dd>
 * </dl>
 *
 * @param <K> Component type
 */
public final class Node<K, V> implements RedBlackTree<K, V>, Map.Entry<K, V> {

    final RedBlackTree<K, V> left;
    final K key;
    final V value;
    final RedBlackTree<K, V> right;
    /**
     * The sign bit encodes the color.
     * The size is Math.abs(sizeAndColor).
     */
    final int sizeAndColor;
    // This is no public API! The RedBlackTree takes care of passing the correct Comparator.
    Node(boolean color, RedBlackTree<K, V> left, K key, V value, RedBlackTree<K, V> right) {
        this.left = left;
        this.key = key;
        this.value = value;
        this.right = right;
        int size = left.size() + right.size() + 1;
        this.sizeAndColor = color == Color.RED ? -size : size;
        /*
        assert isRed() &&!left.isRed()&&!right.isRed()
                || !isRed()&&!right.isRed()
                :"a red node must only have black children," +
                "a black node must not have a right child that is red: "
                +toLispString();

         */
    }

    private static <K, V> Node<K, V> balanceLeft(boolean color, RedBlackTree<K, V> left, K key,
                                                 V value, RedBlackTree<K, V> right) {
        if (color == Color.BLACK) {
            if (!left.isEmpty()) {
                final Node<K, V> ln = (Node<K, V>) left;
                if (ln.color() == Color.RED) {
                    if (!ln.left.isEmpty()) {
                        final Node<K, V> lln = (Node<K, V>) ln.left;
                        if (lln.color() == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, lln.left, lln.key, lln.value, lln.right
                            );
                            final Node<K, V> newRight = new Node<>(Color.BLACK, ln.right, key, value, right);
                            return new Node<>(Color.RED, newLeft, ln.key, ln.value, newRight);
                        }
                    }
                    if (!ln.right.isEmpty()) {
                        final Node<K, V> lrn = (Node<K, V>) ln.right;
                        if (lrn.color() == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, ln.left, ln.key, ln.value, lrn.left
                            );
                            final Node<K, V> newRight = new Node<>(Color.BLACK, lrn.right, key, value, right);
                            return new Node<>(Color.RED, newLeft, lrn.key, lrn.value, newRight);
                        }
                    }
                }
            }
        }
        return new Node<>(color, left, key, value, right);
    }

    private static <K, V> Node<K, V> balanceRight(boolean color, RedBlackTree<K, V> left, K key,
                                                  V value, RedBlackTree<K, V> right) {
        if (color == Color.BLACK) {
            if (!right.isEmpty()) {
                final Node<K, V> rn = (Node<K, V>) right;
                if (rn.color() == Color.RED) {
                    if (!rn.right.isEmpty()) {
                        final Node<K, V> rrn = (Node<K, V>) rn.right;
                        if (rrn.color() == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, left, key, value, rn.left);
                            final Node<K, V> newRight = new Node<>(Color.BLACK, rrn.left, rrn.key, rrn.value, rrn.right
                            );
                            return new Node<>(Color.RED, newLeft, rn.key, rn.value, newRight);
                        }
                    }
                    if (!rn.left.isEmpty()) {
                        final Node<K, V> rln = (Node<K, V>) rn.left;
                        if (rln.color() == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, left, key, value, rln.left);
                            final Node<K, V> newRight = new Node<>(Color.BLACK, rln.right, rn.key, rn.value, rn.right
                            );
                            return new Node<>(Color.RED, newLeft, rln.key, rln.value, newRight);
                        }
                    }
                }
            }
        }
        return new Node<>(color, left, key, value, right);
    }

    private static <K, V> Tuple2<? extends RedBlackTree<K, V>, Boolean> blackify(RedBlackTree<K, V> tree) {
        if (tree instanceof Node) {
            final Node<K, V> node = (Node<K, V>) tree;
            if (node.color() == Color.RED) {
                return Tuple.of(node.color(Color.BLACK), false);
            }
        }
        return Tuple.of(tree, true);
    }

    static <K, V> RedBlackTree<K, V> color(RedBlackTree<K, V> tree, boolean color) {
        return tree.isEmpty() ? tree : ((Node<K, V>) tree).color(color);
    }

    static <K, V> Tuple2<? extends RedBlackTree<K, V>, Boolean> delete(RedBlackTree<K, V> tree, K key, Comparator<? super K> comparator) {
        if (tree.isEmpty()) {
            return Tuple.of(tree, false);
        } else {
            final Node<K, V> node = (Node<K, V>) tree;
            final int comparison = comparator.compare(node.key, key);
            if (comparison < 0) {
                final Tuple2<? extends RedBlackTree<K, V>, Boolean> deleted = delete(node.left, key, comparator);
                final RedBlackTree<K, V> l = deleted._1;
                final boolean d = deleted._2;
                if (d) {
                    return Node.unbalancedRight(node.color(), l, node.key, node.value, node.right
                    );
                } else {
                    final Node<K, V> newNode = new Node<>(node.color(), l, node.key, node.value, node.right
                    );
                    return Tuple.of(newNode, false);
                }
            } else if (comparison > 0) {
                final Tuple2<? extends RedBlackTree<K, V>, Boolean> deleted = delete(node.right, key, comparator);
                final RedBlackTree<K, V> r = deleted._1;
                final boolean d = deleted._2;
                if (d) {
                    return Node.unbalancedLeft(node.color(), node.left, node.key, node.value, r,
                            Empty.empty());
                } else {
                    final Node<K, V> newNode = new Node<>(node.color(), node.left, node.key, node.value, r
                    );
                    return Tuple.of(newNode, false);
                }
            } else {
                if (node.right.isEmpty()) {
                    if (node.color() == Color.BLACK) {
                        return blackify(node.left);
                    } else {
                        return Tuple.of(node.left, false);
                    }
                } else {
                    final Node<K, V> nodeRight = (Node<K, V>) node.right;
                    final var newRight = deleteMin(nodeRight);
                    final RedBlackTree<K, V> r = newRight._1;
                    final boolean d = newRight._2;
                    final K m = newRight._3;
                    final V mv = newRight._4;
                    if (d) {
                        return Node.unbalancedLeft(node.color(), node.left, m, mv, r, Empty.empty());
                    } else {
                        final RedBlackTree<K, V> newNode = new Node<>(node.color(), node.left, m, mv, r
                        );
                        return Tuple.of(newNode, false);
                    }
                }
            }
        }
    }

    private static <K, V> Tuple4<? extends RedBlackTree<K, V>, Boolean, K, V> deleteMin(Node<K, V> node) {
        if (node.color() == Color.BLACK && node.left().isEmpty() && node.right.isEmpty()) {
            return Tuple.of(Empty.empty(), true, node.getKey(), node.getValue());
        } else if (node.color() == Color.BLACK && node.left().isEmpty() && node.right().color() == Color.RED) {
            return Tuple.of(((Node<K, V>) node.right()).color(Color.BLACK), false, node.getKey(), node.getValue());
        } else if (node.color() == Color.RED && node.left().isEmpty()) {
            return Tuple.of(node.right(), false, node.getKey(), node.getValue());
        } else {
            final Node<K, V> nodeLeft = (Node<K, V>) node.left;
            final var newNode = deleteMin(nodeLeft);
            final RedBlackTree<K, V> l = newNode._1;
            final boolean deleted = newNode._2;
            final K m = newNode._3;
            final V mv = newNode._4;
            if (deleted) {
                final Tuple2<Node<K, V>, Boolean> tD = Node.unbalancedRight(node.color(), l,
                        node.key, node.value, node.right);
                return Tuple.of(tD._1, tD._2, m, mv);
            } else {
                final Node<K, V> tD = new Node<>(node.color(), l, node.key, node.value, node.right);
                return Tuple.of(tD, false, m, mv);
            }
        }
    }

    /**
     * Inserts or updates an element in the tree.
     *
     * @param <K>        the element type
     * @param tree       the tree
     * @param key        the new element value
     * @param value      the new element value
     * @param comparator the comparator on which the elements are sorted in the tree
     * @return the same tree instance if the element value is already in the tree, otherwise a new tree instance
     */
    static <K, V> Node<K, V> insert(RedBlackTree<K, V> tree, K key, V value, Comparator<? super K> comparator) {
        if (tree.isEmpty()) {
            final Empty<K, V> empty = (Empty<K, V>) tree;
            return new Node<>(Color.RED, empty, key, value, empty);
        } else {
            final Node<K, V> node = (Node<K, V>) tree;
            final int comparison = comparator.compare(node.key, key);
            if (comparison < 0) {
                final Node<K, V> newLeft = insert(node.left, key, value, comparator);
                return (newLeft == node.left)
                        ? node
                        : Node.balanceLeft(node.color(), newLeft, node.key, node.value, node.right
                );
            } else if (comparison > 0) {
                final Node<K, V> newRight = insert(node.right, key, value, comparator);
                return (newRight == node.right)
                        ? node
                        : Node.balanceRight(node.color(), node.left, node.key, node.value, newRight
                );
            } else {
                return Objects.equals(node.getValue(), value) ? node : new Node<>(node.color(), node.left, key, value, node.right);
            }
        }
    }


    static <K, V> Node<K, V> maximum(Node<K, V> node) {
        Node<K, V> curr = node;
        while (!curr.right.isEmpty()) {
            curr = (Node<K, V>) curr.right;
        }
        return curr;
    }




    static <K, V> Node<K, V> minimum(Node<K, V> node) {
        Node<K, V> curr = node;
        while (!curr.left.isEmpty()) {
            curr = (Node<K, V>) curr.left;
        }
        return curr;
    }


    private static String toLispString(RedBlackTree<?, ?> tree) {
        if (tree.isEmpty()) {
            return "";
        } else {
            final Node<?, ?> node = (Node<?, ?>) tree;
            final String value = (node.color() ? 'R' : 'B') + ":" + node.key +
                    (node.value == null ? "" : "=" + node.value);
            if (node.isLeaf()) {
                return value;
            } else {
                final String left = node.left.isEmpty() ? "" : " " + toLispString(node.left);
                final String right = node.right.isEmpty() ? "" : " " + toLispString(node.right);
                return "(" + value + "," + left + "," + right + ")";
            }
        }
    }

    private static <K, V> Tuple2<Node<K, V>, Boolean> unbalancedLeft(boolean color, RedBlackTree<K, V> left,
                                                                     K key, V value, RedBlackTree<K, V> right, Empty<K, V> empty) {
        if (!left.isEmpty()) {
            final Node<K, V> ln = (Node<K, V>) left;
            if (ln.color() == Color.BLACK) {
                final Node<K, V> newNode = Node.balanceLeft(Color.BLACK, ln.color(Color.RED), key, value, right);
                return Tuple.of(newNode, color == Color.BLACK);
            } else if (color == Color.BLACK && !ln.right.isEmpty()) {
                final Node<K, V> lrn = (Node<K, V>) ln.right;
                if (lrn.color() == Color.BLACK) {
                    final Node<K, V> newRightNode = Node.balanceLeft(Color.BLACK, lrn.color(Color.RED), key, value, right
                    );
                    final Node<K, V> newNode = new Node<>(Color.BLACK, ln.left, ln.key, ln.value, newRightNode
                    );
                    return Tuple.of(newNode, false);
                }
            }
        }
        throw new IllegalStateException("unbalancedLeft(" + color + ", " + left + ", " + value + ", " + right + ")");
    }

    private static <K, V> Tuple2<Node<K, V>, Boolean> unbalancedRight(boolean color, RedBlackTree<K, V> left,
                                                                      K key, V value, RedBlackTree<K, V> right) {
        if (!right.isEmpty()) {
            final Node<K, V> rn = (Node<K, V>) right;
            if (rn.color() == Color.BLACK) {
                final Node<K, V> newNode = Node.balanceRight(Color.BLACK, left, key, value, rn.color(Color.RED));
                return Tuple.of(newNode, color == Color.BLACK);
            } else if (color == Color.BLACK && !rn.left.isEmpty()) {
                final Node<K, V> rln = (Node<K, V>) rn.left;
                if (rln.color() == Color.BLACK) {
                    final Node<K, V> newLeftNode = Node.balanceRight(Color.BLACK, left, key, value, rln.color(Color.RED)
                    );
                    final Node<K, V> newNode = new Node<>(Color.BLACK, newLeftNode, rn.key, rn.value, rn.right
                    );
                    return Tuple.of(newNode, false);
                }
            }
        }
        throw new IllegalStateException("unbalancedRight(" + color + ", " + left + ", " + value + ", " + right + ")");
    }

    @Override
    public @NonNull RedBlackTree<K, V> ceiling(K value, Comparator<? super K> comparator) {
        final int result = comparator.compare(this.key, value);
        if (result < 0) {
            return left.ceiling(value, comparator);
        } else if (result > 0) {
            return right.ceiling(value, comparator).orElse(this);
        } else {
            return this;
        }
    }

    @Override
    public boolean color() {
        return sizeAndColor < 0;
    }

    Node<K, V> color(boolean color) {
        return (this.color() == color) ? this : new Node<>(color, left, key, value, right);
    }

    @Override
    public boolean contains(K key, Comparator<? super K> comparator) {
        final int result = comparator.compare(this.key, key);
        if (result < 0) {
            return left.contains(key, comparator);
        }
        if (result > 0) {
            return right.contains(key, comparator);
        }
        return true;
    }

    @Override
    public Map.@Nullable Entry<K, V> entryOrNull() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Map.Entry<?, ?> e
                && Objects.equals(key, e.getKey())
                && Objects.equals(value, e.getValue());
    }

    @Override
    public RedBlackTree<K, V> find(K key, Comparator<? super K> comparator) {
        final int result = comparator.compare(this.key, key);
        if (result < 0) {
            return left.find(key, comparator);
        } else if (result > 0) {
            return right.find(key, comparator);
        } else {
            return this;
        }
    }

    @Override
    public @NonNull RedBlackTree<K, V> floor(K value, Comparator<? super K> comparator) {
        final int result = comparator.compare(this.key, value);
        if (result < 0) {
            return left.floor(value, comparator).orElse(this);
        } else if (result > 0) {
            return right.floor(value, comparator);
        } else {
            return this;
        }
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) ^
                (value == null ? 0 : value.hashCode());
    }

    @Override
    public @NonNull RedBlackTree<K, V> higher(K value, Comparator<? super K> comparator) {
        final int result = comparator.compare(this.key, value);
        if (result < 0) {
            return left.higher(value, comparator);
        } else if (result > 0) {
            return right.higher(value, comparator).orElse(this);
        } else {
            return right.higher(value, comparator);
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private boolean isLeaf() {
        return left.isEmpty() && right.isEmpty();
    }

    @Override
    public boolean isRed() {
        return color() == Color.RED;
    }

    @Override
    public @Nullable K keyOrNull() {
        return key;
    }

    @Override
    public RedBlackTree<K, V> left() {
        return left;
    }

    @Override
    public @NonNull RedBlackTree<K, V> lower(K value, Comparator<? super K> comparator) {
        final int result = comparator.compare(this.key, value);
        if (result < 0) {
            return left.lower(value, comparator).orElse(this);
        } else if (result > 0) {
            return right.lower(value, comparator);
        } else {
            return left.lower(value, comparator);
        }
    }

    @Override
    public <E> @Nullable E mapOrNull(@NonNull BiFunction<K, V, E> f) {
        return f.apply(key, value);
    }

    @Override
    public RedBlackTree<K, V> orElse(RedBlackTree<K, V> other) {
        return this;
    }

    @Override
    public RedBlackTree<K, V> right() {
        return right;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return Math.abs(sizeAndColor);
    }

    @Override
    public String toLispString() {
        return isLeaf() ? "(" + (color() ? 'R' : 'B') + ":" + key +
                (value == null ? "" : "=" + value)
                + ")" : toLispString(this);
    }

    public String toString() {
        return key + "=" + value;
    }

    @Override
    public @Nullable V valueOrNull() {
        return value;
    }
}
