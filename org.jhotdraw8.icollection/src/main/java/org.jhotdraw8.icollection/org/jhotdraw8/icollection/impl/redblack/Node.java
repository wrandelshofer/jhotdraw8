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
    @Override
    public String toLispString() {
        return isLeaf() ? "(" + (color ? 'R' : 'B') + ":" + key +
                (value == null ? "" : "=" + value)
                + ")" : toLispString(this);
    }

    private static String toLispString(RedBlackTree<?, ?> tree) {
        if (tree.isEmpty()) {
            return "";
        } else {
            final Node<?, ?> node = (Node<?, ?>) tree;
            final String value = (node.color ? 'R' : 'B') + ":" + node.key +
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

    private boolean isLeaf() {
        return left.isEmpty() && right.isEmpty();
    }

    final boolean color;
    final byte blackHeight;
    final RedBlackTree<K, V> left;
    final K key;
    final V value;
    final RedBlackTree<K, V> right;
    final int size;

    // This is no public API! The RedBlackTree takes care of passing the correct Comparator.
    Node(boolean color, int blackHeight, RedBlackTree<K, V> left, K key, V value, RedBlackTree<K, V> right) {
        this.color = color;
        this.blackHeight = (byte) blackHeight;
        this.left = left;
        this.key = key;
        this.value = value;
        this.right = right;
        this.size = left.size() + right.size() + 1;
        /*
        assert isRed() &&!left.isRed()&&!right.isRed()
                || !isRed()&&!right.isRed()
                :"a red node must only have black children," +
                "a black node must not have a right child that is red: "
                +toLispString();

         */
    }

    @Override
    public boolean color() {
        return color;
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
    public RedBlackTree<K, V> orElse(RedBlackTree<K, V> other) {
        return this;
    }

    @Override
    public @Nullable K keyOrNull() {
        return key;
    }

    @Override
    public @Nullable V valueOrNull() {
        return value;
    }

    @Override
    public Map.@Nullable Entry<K, V> entryOrNull() {
        return this;
    }

    @Override
    public <E> @Nullable E mapOrNull(@NonNull BiFunction<K, V, E> f) {
        return f.apply(key, value);
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
    public boolean isEmpty() {
        return false;
    }

    @Override
    public RedBlackTree<K, V> left() {
        return left;
    }

    @Override
    public RedBlackTree<K, V> right() {
        return right;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    Node<K, V> color(boolean color) {
        return (this.color == color) ? this : new Node<>(color, blackHeight, left, key, value, right);
    }

    static <K, V> RedBlackTree<K, V> color(RedBlackTree<K, V> tree, boolean color) {
        return tree.isEmpty() ? tree : ((Node<K, V>) tree).color(color);
    }

    private static <K, V> Node<K, V> balanceLeft(boolean color, int blackHeight, RedBlackTree<K, V> left, K key,
                                                 V value, RedBlackTree<K, V> right) {
        if (color == Color.BLACK) {
            if (!left.isEmpty()) {
                final Node<K, V> ln = (Node<K, V>) left;
                if (ln.color == Color.RED) {
                    if (!ln.left.isEmpty()) {
                        final Node<K, V> lln = (Node<K, V>) ln.left;
                        if (lln.color == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, blackHeight, lln.left, lln.key, lln.value, lln.right
                            );
                            final Node<K, V> newRight = new Node<>(Color.BLACK, blackHeight, ln.right, key, value, right);
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, ln.key, ln.value, newRight);
                        }
                    }
                    if (!ln.right.isEmpty()) {
                        final Node<K, V> lrn = (Node<K, V>) ln.right;
                        if (lrn.color == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, blackHeight, ln.left, ln.key, ln.value, lrn.left
                            );
                            final Node<K, V> newRight = new Node<>(Color.BLACK, blackHeight, lrn.right, key, value, right);
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, lrn.key, lrn.value, newRight);
                        }
                    }
                }
            }
        }
        return new Node<>(color, blackHeight, left, key, value, right);
    }

    private static <K, V> Node<K, V> balanceRight(boolean color, int blackHeight, RedBlackTree<K, V> left, K key,
                                                  V value, RedBlackTree<K, V> right) {
        if (color == Color.BLACK) {
            if (!right.isEmpty()) {
                final Node<K, V> rn = (Node<K, V>) right;
                if (rn.color == Color.RED) {
                    if (!rn.right.isEmpty()) {
                        final Node<K, V> rrn = (Node<K, V>) rn.right;
                        if (rrn.color == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, blackHeight, left, key, value, rn.left);
                            final Node<K, V> newRight = new Node<>(Color.BLACK, blackHeight, rrn.left, rrn.key, rrn.value, rrn.right
                            );
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, rn.key, rn.value, newRight);
                        }
                    }
                    if (!rn.left.isEmpty()) {
                        final Node<K, V> rln = (Node<K, V>) rn.left;
                        if (rln.color == Color.RED) {
                            final Node<K, V> newLeft = new Node<>(Color.BLACK, blackHeight, left, key, value, rln.left);
                            final Node<K, V> newRight = new Node<>(Color.BLACK, blackHeight, rln.right, rn.key, rn.value, rn.right
                            );
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, rln.key, rln.value, newRight);
                        }
                    }
                }
            }
        }
        return new Node<>(color, blackHeight, left, key, value, right);
    }

    private static <K, V> Tuple2<? extends RedBlackTree<K, V>, Boolean> blackify(RedBlackTree<K, V> tree) {
        if (tree instanceof Node) {
            final Node<K, V> node = (Node<K, V>) tree;
            if (node.color == Color.RED) {
                return Tuple.of(node.color(Color.BLACK), false);
            }
        }
        return Tuple.of(tree, true);
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
                    return Node.unbalancedRight(node.color, node.blackHeight - 1, l, node.key, node.value, node.right,
                            Empty.empty());
                } else {
                    final Node<K, V> newNode = new Node<>(node.color, node.blackHeight, l, node.key, node.value, node.right
                    );
                    return Tuple.of(newNode, false);
                }
            } else if (comparison > 0) {
                final Tuple2<? extends RedBlackTree<K, V>, Boolean> deleted = delete(node.right, key, comparator);
                final RedBlackTree<K, V> r = deleted._1;
                final boolean d = deleted._2;
                if (d) {
                    return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, node.key, node.value, r,
                            Empty.empty());
                } else {
                    final Node<K, V> newNode = new Node<>(node.color, node.blackHeight, node.left, node.key, node.value, r
                    );
                    return Tuple.of(newNode, false);
                }
            } else {
                if (node.right.isEmpty()) {
                    if (node.color == Color.BLACK) {
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
                        return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, m, mv, r, Empty.empty());
                    } else {
                        final RedBlackTree<K, V> newNode = new Node<>(node.color, node.blackHeight, node.left, m, mv, r
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
                final Tuple2<Node<K, V>, Boolean> tD = Node.unbalancedRight(node.color, node.blackHeight - 1, l,
                        node.key, node.value, node.right, Empty.empty());
                return Tuple.of(tD._1, tD._2, m, mv);
            } else {
                final Node<K, V> tD = new Node<>(node.color, node.blackHeight, l, node.key, node.value, node.right);
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
            return new Node<>(Color.RED, 1, empty, key, value, empty);
        } else {
            final Node<K, V> node = (Node<K, V>) tree;
            final int comparison = comparator.compare(node.key, key);
            if (comparison < 0) {
                final Node<K, V> newLeft = insert(node.left, key, value, comparator);
                return (newLeft == node.left)
                        ? node
                        : Node.balanceLeft(node.color, node.blackHeight, newLeft, node.key, node.value, node.right
                );
            } else if (comparison > 0) {
                final Node<K, V> newRight = insert(node.right, key, value, comparator);
                return (newRight == node.right)
                        ? node
                        : Node.balanceRight(node.color, node.blackHeight, node.left, node.key, node.value, newRight
                );
            } else {
                return Objects.equals(node.getValue(), value) ? node : new Node<>(node.color, node.blackHeight, node.left, key, value, node.right);
            }
        }
    }

    private static boolean isRed(RedBlackTree<?, ?> tree) {
        return !tree.isEmpty() && ((Node<?, ?>) tree).color == Color.RED;
    }

    @Override
    public boolean isRed() {
        return color == Color.RED;
    }

    static <K, V> RedBlackTree<K, V> join(RedBlackTree<K, V> t1, K key, V value, RedBlackTree<K, V> t2, Comparator<? super K> comparator) {
        if (t1.isEmpty()) {
            return t2.insert(key, value, comparator);
        } else if (t2.isEmpty()) {
            return t1.insert(key, value, comparator);
        } else {
            final Node<K, V> n1 = (Node<K, V>) t1;
            final Node<K, V> n2 = (Node<K, V>) t2;
            final int comparison = n1.blackHeight - n2.blackHeight;
            if (comparison < 0) {
                return Node.joinLT(n1, key, value, n2, n1.blackHeight).color(Color.BLACK);
            } else if (comparison > 0) {
                return Node.joinGT(n1, key, value, n2, n2.blackHeight).color(Color.BLACK);
            } else {
                return new Node<>(Color.BLACK, n1.blackHeight + 1, n1, key, value, n2);
            }
        }
    }

    private static <K, V> Node<K, V> joinGT(Node<K, V> n1, K key, V value, Node<K, V> n2, int h2) {
        if (n1.blackHeight == h2) {
            return new Node<>(Color.RED, h2 + 1, n1, key, value, n2);
        } else {
            final Node<K, V> node = joinGT((Node<K, V>) n1.right, key, value, n2, h2);
            return Node.balanceRight(n1.color, n1.blackHeight, n1.left, n1.key, n1.value, node);
        }
    }

    private static <K, V> Node<K, V> joinLT(Node<K, V> n1, K key, V value, Node<K, V> n2, int h1) {
        if (n2.blackHeight == h1) {
            return new Node<>(Color.RED, h1 + 1, n1, key, value, n2);
        } else {
            final Node<K, V> node = joinLT(n1, key, value, (Node<K, V>) n2.left, h1);
            return Node.balanceLeft(n2.color, n2.blackHeight, node, n2.key, n2.value, n2.right);
        }
    }

    static <K, V> RedBlackTree<K, V> merge(RedBlackTree<K, V> t1, RedBlackTree<K, V> t2) {
        if (t1.isEmpty()) {
            return t2;
        } else if (t2.isEmpty()) {
            return t1;
        } else {
            final Node<K, V> n1 = (Node<K, V>) t1;
            final Node<K, V> n2 = (Node<K, V>) t2;
            final int comparison = n1.blackHeight - n2.blackHeight;
            if (comparison < 0) {
                final Node<K, V> node = Node.mergeLT(n1, n2, n1.blackHeight);
                return Node.color(node, Color.BLACK);
            } else if (comparison > 0) {
                final Node<K, V> node = Node.mergeGT(n1, n2, n2.blackHeight);
                return Node.color(node, Color.BLACK);
            } else {
                final Node<K, V> node = Node.mergeEQ(n1, n2);
                return Node.color(node, Color.BLACK);
            }
        }
    }

    private static <K, V> Node<K, V> mergeEQ(Node<K, V> n1, Node<K, V> n2) {
        final Node<K, V> m = Node.minimum(n2);
        final RedBlackTree<K, V> t2 = Node.deleteMin(n2)._1;
        final int h2 = t2.isEmpty() ? 0 : ((Node<K, V>) t2).blackHeight;
        if (n1.blackHeight == h2) {
            return new Node<>(Color.RED, n1.blackHeight + 1, n1, m.key, m.value, t2);
        } else if (n1.left.isRed()) {
            final Node<K, V> node = new Node<>(Color.BLACK, n1.blackHeight, n1.right, m.key, m.value, t2);
            return new Node<>(Color.RED, n1.blackHeight + 1, Node.color(n1.left, Color.BLACK), n1.key, n1.value, node);
        } else if (n1.right.isRed()) {
            final RedBlackTree<K, V> rl = ((Node<K, V>) n1.right).left;
            final Node<K, V> rx = ((Node<K, V>) n1.right);
            final RedBlackTree<K, V> rr = ((Node<K, V>) n1.right).right;
            final Node<K, V> left = new Node<>(Color.RED, n1.blackHeight, n1.left, n1.key, n1.value, rl);
            final Node<K, V> right = new Node<>(Color.RED, n1.blackHeight, rr, m.key, m.value, t2);
            return new Node<>(Color.BLACK, n1.blackHeight, left, rx.key, rx.value, right);
        } else {
            return new Node<>(Color.BLACK, n1.blackHeight, n1.color(Color.RED), m.key, m.value, t2);
        }
    }

    private static <K, V> Node<K, V> mergeGT(Node<K, V> n1, Node<K, V> n2, int h2) {
        if (n1.blackHeight == h2) {
            return Node.mergeEQ(n1, n2);
        } else {
            final Node<K, V> node = Node.mergeGT((Node<K, V>) n1.right, n2, h2);
            return Node.balanceRight(n1.color, n1.blackHeight, n1.left, n1.key, n1.value, node);
        }
    }

    private static <K, V> Node<K, V> mergeLT(Node<K, V> n1, Node<K, V> n2, int h1) {
        if (n2.blackHeight == h1) {
            return Node.mergeEQ(n1, n2);
        } else {
            final Node<K, V> node = Node.mergeLT(n1, (Node<K, V>) n2.left, h1);
            return Node.balanceLeft(n2.color, n2.blackHeight, node, n2.key, n2.value, n2.right);
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

    static <K, V> Tuple2<RedBlackTree<K, V>, RedBlackTree<K, V>> split(@NonNull RedBlackTree<K, V> tree, K key, V value, @NonNull Comparator<? super K> comparator) {
        if (tree.isEmpty()) {
            return Tuple.of(tree, tree);
        } else {
            final Node<K, V> node = (Node<K, V>) tree;
            final int comparison = comparator.compare(node.key, key);
            if (comparison < 0) {
                final Tuple2<RedBlackTree<K, V>, RedBlackTree<K, V>> split = Node.split(node.left, key, value, comparator);
                return Tuple.of(split._1, Node.join(split._2, node.key, node.value, Node.color(node.right, Color.BLACK), comparator));
            } else if (comparison > 0) {
                final Tuple2<RedBlackTree<K, V>, RedBlackTree<K, V>> split = Node.split(node.right, key, value, comparator);
                return Tuple.of(Node.join(Node.color(node.left, Color.BLACK), node.key, node.value, split._1, comparator), split._2);
            } else {
                return Tuple.of(Node.color(node.left, Color.BLACK), Node.color(node.right, Color.BLACK));
            }
        }
    }

    private static <K, V> Tuple2<Node<K, V>, Boolean> unbalancedLeft(boolean color, int blackHeight, RedBlackTree<K, V> left,
                                                                     K key, V value, RedBlackTree<K, V> right, Empty<K, V> empty) {
        if (!left.isEmpty()) {
            final Node<K, V> ln = (Node<K, V>) left;
            if (ln.color == Color.BLACK) {
                final Node<K, V> newNode = Node.balanceLeft(Color.BLACK, blackHeight, ln.color(Color.RED), key, value, right);
                return Tuple.of(newNode, color == Color.BLACK);
            } else if (color == Color.BLACK && !ln.right.isEmpty()) {
                final Node<K, V> lrn = (Node<K, V>) ln.right;
                if (lrn.color == Color.BLACK) {
                    final Node<K, V> newRightNode = Node.balanceLeft(Color.BLACK, blackHeight, lrn.color(Color.RED), key, value, right
                    );
                    final Node<K, V> newNode = new Node<>(Color.BLACK, ln.blackHeight, ln.left, ln.key, ln.value, newRightNode
                    );
                    return Tuple.of(newNode, false);
                }
            }
        }
        throw new IllegalStateException("unbalancedLeft(" + color + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
    }

    private static <K, V> Tuple2<Node<K, V>, Boolean> unbalancedRight(boolean color, int blackHeight, RedBlackTree<K, V> left,
                                                                      K key, V value, RedBlackTree<K, V> right, Empty<K, V> empty) {
        if (!right.isEmpty()) {
            final Node<K, V> rn = (Node<K, V>) right;
            if (rn.color == Color.BLACK) {
                final Node<K, V> newNode = Node.balanceRight(Color.BLACK, blackHeight, left, key, value, rn.color(Color.RED));
                return Tuple.of(newNode, color == Color.BLACK);
            } else if (color == Color.BLACK && !rn.left.isEmpty()) {
                final Node<K, V> rln = (Node<K, V>) rn.left;
                if (rln.color == Color.BLACK) {
                    final Node<K, V> newLeftNode = Node.balanceRight(Color.BLACK, blackHeight, left, key, value, rln.color(Color.RED)
                    );
                    final Node<K, V> newNode = new Node<>(Color.BLACK, rn.blackHeight, newLeftNode, rn.key, rn.value, rn.right
                    );
                    return Tuple.of(newNode, false);
                }
            }
        }
        throw new IllegalStateException("unbalancedRight(" + color + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Map.Entry<?, ?> e
                && Objects.equals(key, e.getKey())
                && Objects.equals(value, e.getValue());
    }

    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) ^
                (value == null ? 0 : value.hashCode());
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return key + "=" + value;
    }
}
