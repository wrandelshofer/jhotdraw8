/*
 * @(#)Node.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;

import java.util.Comparator;

/**
 * A non-empty tree node.
 * <p>
 * This class has been derived from 'vavr' RedBlackTree.java.
 * <dl>
 *     <dt>RedBlackTree.java. Copyright 2023 (c) vavr. MIT License.</dt>
 *     <dd><a href="https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/collection/RedBlackTree.java">github.com</a></dd>
 * </dl>
 *
 * @param <T> Component type
 */
public final class Node<T> implements RedBlackTree<T> {

    final Color color;
    final int blackHeight;
    final RedBlackTree<T> left;
    final T value;
    final RedBlackTree<T> right;
    final Empty<T> empty;
    final int size;

    // This is no public API! The RedBlackTree takes care of passing the correct Comparator.
    Node(Color color, int blackHeight, RedBlackTree<T> left, T value, RedBlackTree<T> right, Empty<T> empty) {
        this.color = color;
        this.blackHeight = blackHeight;
        this.left = left;
        this.value = value;
        this.right = right;
        this.empty = empty;
        this.size = left.size() + right.size() + 1;
    }

    @Override
    public Color color() {
        return color;
    }

    @Override
    public Comparator<T> comparator() {
        return empty.comparator;
    }

    @Override
    public boolean contains(T value) {
        final int result = empty.comparator.compare(value, this.value);
        if (result < 0) {
            return left.contains(value);
        } else if (result > 0) {
            return right.contains(value);
        } else {
            return true;
        }
    }

    @Override
    public Empty<T> emptyInstance() {
        return empty;
    }

    @Override
    public Option<T> find(T value) {
        final int result = empty.comparator.compare(value, this.value);
        if (result < 0) {
            return left.find(value);
        } else if (result > 0) {
            return right.find(value);
        } else {
            return Option.some(this.value);
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public RedBlackTree<T> left() {
        return left;
    }

    @Override
    public RedBlackTree<T> right() {
        return right;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public String toString() {
        return isLeaf() ? "(" + color + ":" + value + ")" : toLispString(this);
    }

    private static String toLispString(RedBlackTree<?> tree) {
        if (tree.isEmpty()) {
            return "";
        } else {
            final Node<?> node = (Node<?>) tree;
            final String value = node.color + ":" + node.value;
            if (node.isLeaf()) {
                return value;
            } else {
                final String left = node.left.isEmpty() ? "" : " " + toLispString(node.left);
                final String right = node.right.isEmpty() ? "" : " " + toLispString(node.right);
                return "(" + value + left + right + ")";
            }
        }
    }

    private boolean isLeaf() {
        return left.isEmpty() && right.isEmpty();
    }

    Node<T> color(Color color) {
        return (this.color == color) ? this : new Node<>(color, blackHeight, left, value, right, empty);
    }

    static <T> RedBlackTree<T> color(RedBlackTree<T> tree, Color color) {
        return tree.isEmpty() ? tree : ((Node<T>) tree).color(color);
    }

    private static <T> Node<T> balanceLeft(Color color, int blackHeight, RedBlackTree<T> left, T value,
                                           RedBlackTree<T> right, Empty<T> empty) {
        if (color == Color.BLACK) {
            if (!left.isEmpty()) {
                final Node<T> ln = (Node<T>) left;
                if (ln.color == Color.RED) {
                    if (!ln.left.isEmpty()) {
                        final Node<T> lln = (Node<T>) ln.left;
                        if (lln.color == Color.RED) {
                            final Node<T> newLeft = new Node<>(Color.BLACK, blackHeight, lln.left, lln.value, lln.right,
                                    empty);
                            final Node<T> newRight = new Node<>(Color.BLACK, blackHeight, ln.right, value, right, empty);
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, ln.value, newRight, empty);
                        }
                    }
                    if (!ln.right.isEmpty()) {
                        final Node<T> lrn = (Node<T>) ln.right;
                        if (lrn.color == Color.RED) {
                            final Node<T> newLeft = new Node<>(Color.BLACK, blackHeight, ln.left, ln.value, lrn.left,
                                    empty);
                            final Node<T> newRight = new Node<>(Color.BLACK, blackHeight, lrn.right, value, right, empty);
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, lrn.value, newRight, empty);
                        }
                    }
                }
            }
        }
        return new Node<>(color, blackHeight, left, value, right, empty);
    }

    private static <T> Node<T> balanceRight(Color color, int blackHeight, RedBlackTree<T> left, T value,
                                            RedBlackTree<T> right, Empty<T> empty) {
        if (color == Color.BLACK) {
            if (!right.isEmpty()) {
                final Node<T> rn = (Node<T>) right;
                if (rn.color == Color.RED) {
                    if (!rn.right.isEmpty()) {
                        final Node<T> rrn = (Node<T>) rn.right;
                        if (rrn.color == Color.RED) {
                            final Node<T> newLeft = new Node<>(Color.BLACK, blackHeight, left, value, rn.left, empty);
                            final Node<T> newRight = new Node<>(Color.BLACK, blackHeight, rrn.left, rrn.value, rrn.right,
                                    empty);
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, rn.value, newRight, empty);
                        }
                    }
                    if (!rn.left.isEmpty()) {
                        final Node<T> rln = (Node<T>) rn.left;
                        if (rln.color == Color.RED) {
                            final Node<T> newLeft = new Node<>(Color.BLACK, blackHeight, left, value, rln.left, empty);
                            final Node<T> newRight = new Node<>(Color.BLACK, blackHeight, rln.right, rn.value, rn.right,
                                    empty);
                            return new Node<>(Color.RED, blackHeight + 1, newLeft, rln.value, newRight, empty);
                        }
                    }
                }
            }
        }
        return new Node<>(color, blackHeight, left, value, right, empty);
    }

    private static <T> Tuple2<? extends RedBlackTree<T>, Boolean> blackify(RedBlackTree<T> tree) {
        if (tree instanceof Node) {
            final Node<T> node = (Node<T>) tree;
            if (node.color == Color.RED) {
                return Tuple.of(node.color(Color.BLACK), false);
            }
        }
        return Tuple.of(tree, true);
    }

    static <T> Tuple2<? extends RedBlackTree<T>, Boolean> delete(RedBlackTree<T> tree, T value) {
        if (tree.isEmpty()) {
            return Tuple.of(tree, false);
        } else {
            final Node<T> node = (Node<T>) tree;
            final int comparison = node.comparator().compare(value, node.value);
            if (comparison < 0) {
                final Tuple2<? extends RedBlackTree<T>, Boolean> deleted = delete(node.left, value);
                final RedBlackTree<T> l = deleted._1;
                final boolean d = deleted._2;
                if (d) {
                    return Node.unbalancedRight(node.color, node.blackHeight - 1, l, node.value, node.right,
                            node.empty);
                } else {
                    final Node<T> newNode = new Node<>(node.color, node.blackHeight, l, node.value, node.right,
                            node.empty);
                    return Tuple.of(newNode, false);
                }
            } else if (comparison > 0) {
                final Tuple2<? extends RedBlackTree<T>, Boolean> deleted = delete(node.right, value);
                final RedBlackTree<T> r = deleted._1;
                final boolean d = deleted._2;
                if (d) {
                    return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, node.value, r,
                            node.empty);
                } else {
                    final Node<T> newNode = new Node<>(node.color, node.blackHeight, node.left, node.value, r,
                            node.empty);
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
                    final Node<T> nodeRight = (Node<T>) node.right;
                    final Tuple3<? extends RedBlackTree<T>, Boolean, T> newRight = deleteMin(nodeRight);
                    final RedBlackTree<T> r = newRight._1;
                    final boolean d = newRight._2;
                    final T m = newRight._3;
                    if (d) {
                        return Node.unbalancedLeft(node.color, node.blackHeight - 1, node.left, m, r, node.empty);
                    } else {
                        final RedBlackTree<T> newNode = new Node<>(node.color, node.blackHeight, node.left, m, r,
                                node.empty);
                        return Tuple.of(newNode, false);
                    }
                }
            }
        }
    }

    private static <T> Tuple3<? extends RedBlackTree<T>, Boolean, T> deleteMin(Node<T> node) {
        if (node.color() == Color.BLACK && node.left().isEmpty() && node.right.isEmpty()) {
            return Tuple.of(node.empty, true, node.value());
        } else if (node.color() == Color.BLACK && node.left().isEmpty() && node.right().color() == Color.RED) {
            return Tuple.of(((Node<T>) node.right()).color(Color.BLACK), false, node.value());
        } else if (node.color() == Color.RED && node.left().isEmpty()) {
            return Tuple.of(node.right(), false, node.value());
        } else {
            final Node<T> nodeLeft = (Node<T>) node.left;
            final Tuple3<? extends RedBlackTree<T>, Boolean, T> newNode = deleteMin(nodeLeft);
            final RedBlackTree<T> l = newNode._1;
            final boolean deleted = newNode._2;
            final T m = newNode._3;
            if (deleted) {
                final Tuple2<Node<T>, Boolean> tD = Node.unbalancedRight(node.color, node.blackHeight - 1, l,
                        node.value, node.right, node.empty);
                return Tuple.of(tD._1, tD._2, m);
            } else {
                final Node<T> tD = new Node<>(node.color, node.blackHeight, l, node.value, node.right, node.empty);
                return Tuple.of(tD, false, m);
            }
        }
    }

    static <T> Node<T> insert(RedBlackTree<T> tree, T value) {
        if (tree.isEmpty()) {
            final Empty<T> empty = (Empty<T>) tree;
            return new Node<>(Color.RED, 1, empty, value, empty, empty);
        } else {
            final Node<T> node = (Node<T>) tree;
            final int comparison = node.comparator().compare(value, node.value);
            if (comparison < 0) {
                final Node<T> newLeft = insert(node.left, value);
                return (newLeft == node.left)
                        ? node
                        : Node.balanceLeft(node.color, node.blackHeight, newLeft, node.value, node.right,
                        node.empty);
            } else if (comparison > 0) {
                final Node<T> newRight = insert(node.right, value);
                return (newRight == node.right)
                        ? node
                        : Node.balanceRight(node.color, node.blackHeight, node.left, node.value, newRight,
                        node.empty);
            } else {
                // DEV-NOTE: Even if there is no _comparison_ difference, the object may not be _equal_.
                //           To save an equals() call, which may be expensive, we return a new instance.
                return new Node<>(node.color, node.blackHeight, node.left, value, node.right, node.empty);
            }
        }
    }

    private static boolean isRed(RedBlackTree<?> tree) {
        return !tree.isEmpty() && ((Node<?>) tree).color == Color.RED;
    }

    static <T> RedBlackTree<T> join(RedBlackTree<T> t1, T value, RedBlackTree<T> t2) {
        if (t1.isEmpty()) {
            return t2.insert(value);
        } else if (t2.isEmpty()) {
            return t1.insert(value);
        } else {
            final Node<T> n1 = (Node<T>) t1;
            final Node<T> n2 = (Node<T>) t2;
            final int comparison = n1.blackHeight - n2.blackHeight;
            if (comparison < 0) {
                return Node.joinLT(n1, value, n2, n1.blackHeight).color(Color.BLACK);
            } else if (comparison > 0) {
                return Node.joinGT(n1, value, n2, n2.blackHeight).color(Color.BLACK);
            } else {
                return new Node<>(Color.BLACK, n1.blackHeight + 1, n1, value, n2, n1.empty);
            }
        }
    }

    private static <T> Node<T> joinGT(Node<T> n1, T value, Node<T> n2, int h2) {
        if (n1.blackHeight == h2) {
            return new Node<>(Color.RED, h2 + 1, n1, value, n2, n1.empty);
        } else {
            final Node<T> node = joinGT((Node<T>) n1.right, value, n2, h2);
            return Node.balanceRight(n1.color, n1.blackHeight, n1.left, n1.value, node, n2.empty);
        }
    }

    private static <T> Node<T> joinLT(Node<T> n1, T value, Node<T> n2, int h1) {
        if (n2.blackHeight == h1) {
            return new Node<>(Color.RED, h1 + 1, n1, value, n2, n1.empty);
        } else {
            final Node<T> node = joinLT(n1, value, (Node<T>) n2.left, h1);
            return Node.balanceLeft(n2.color, n2.blackHeight, node, n2.value, n2.right, n2.empty);
        }
    }

    static <T> RedBlackTree<T> merge(RedBlackTree<T> t1, RedBlackTree<T> t2) {
        if (t1.isEmpty()) {
            return t2;
        } else if (t2.isEmpty()) {
            return t1;
        } else {
            final Node<T> n1 = (Node<T>) t1;
            final Node<T> n2 = (Node<T>) t2;
            final int comparison = n1.blackHeight - n2.blackHeight;
            if (comparison < 0) {
                final Node<T> node = Node.mergeLT(n1, n2, n1.blackHeight);
                return Node.color(node, Color.BLACK);
            } else if (comparison > 0) {
                final Node<T> node = Node.mergeGT(n1, n2, n2.blackHeight);
                return Node.color(node, Color.BLACK);
            } else {
                final Node<T> node = Node.mergeEQ(n1, n2);
                return Node.color(node, Color.BLACK);
            }
        }
    }

    private static <T> Node<T> mergeEQ(Node<T> n1, Node<T> n2) {
        final T m = Node.minimum(n2);
        final RedBlackTree<T> t2 = Node.deleteMin(n2)._1;
        final int h2 = t2.isEmpty() ? 0 : ((Node<T>) t2).blackHeight;
        if (n1.blackHeight == h2) {
            return new Node<>(Color.RED, n1.blackHeight + 1, n1, m, t2, n1.empty);
        } else if (isRed(n1.left)) {
            final Node<T> node = new Node<>(Color.BLACK, n1.blackHeight, n1.right, m, t2, n1.empty);
            return new Node<>(Color.RED, n1.blackHeight + 1, Node.color(n1.left, Color.BLACK), n1.value, node, n1.empty);
        } else if (isRed(n1.right)) {
            final RedBlackTree<T> rl = ((Node<T>) n1.right).left;
            final T rx = ((Node<T>) n1.right).value;
            final RedBlackTree<T> rr = ((Node<T>) n1.right).right;
            final Node<T> left = new Node<>(Color.RED, n1.blackHeight, n1.left, n1.value, rl, n1.empty);
            final Node<T> right = new Node<>(Color.RED, n1.blackHeight, rr, m, t2, n1.empty);
            return new Node<>(Color.BLACK, n1.blackHeight, left, rx, right, n1.empty);
        } else {
            return new Node<>(Color.BLACK, n1.blackHeight, n1.color(Color.RED), m, t2, n1.empty);
        }
    }

    private static <T> Node<T> mergeGT(Node<T> n1, Node<T> n2, int h2) {
        if (n1.blackHeight == h2) {
            return Node.mergeEQ(n1, n2);
        } else {
            final Node<T> node = Node.mergeGT((Node<T>) n1.right, n2, h2);
            return Node.balanceRight(n1.color, n1.blackHeight, n1.left, n1.value, node, n1.empty);
        }
    }

    private static <T> Node<T> mergeLT(Node<T> n1, Node<T> n2, int h1) {
        if (n2.blackHeight == h1) {
            return Node.mergeEQ(n1, n2);
        } else {
            final Node<T> node = Node.mergeLT(n1, (Node<T>) n2.left, h1);
            return Node.balanceLeft(n2.color, n2.blackHeight, node, n2.value, n2.right, n2.empty);
        }
    }

    static <T> T maximum(Node<T> node) {
        Node<T> curr = node;
        while (!curr.right.isEmpty()) {
            curr = (Node<T>) curr.right;
        }
        return curr.value;
    }

    static <T> T minimum(Node<T> node) {
        Node<T> curr = node;
        while (!curr.left.isEmpty()) {
            curr = (Node<T>) curr.left;
        }
        return curr.value;
    }

    static <T> Tuple2<RedBlackTree<T>, RedBlackTree<T>> split(RedBlackTree<T> tree, T value) {
        if (tree.isEmpty()) {
            return Tuple.of(tree, tree);
        } else {
            final Node<T> node = (Node<T>) tree;
            final int comparison = node.comparator().compare(value, node.value);
            if (comparison < 0) {
                final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(node.left, value);
                return Tuple.of(split._1, Node.join(split._2, node.value, Node.color(node.right, Color.BLACK)));
            } else if (comparison > 0) {
                final Tuple2<RedBlackTree<T>, RedBlackTree<T>> split = Node.split(node.right, value);
                return Tuple.of(Node.join(Node.color(node.left, Color.BLACK), node.value, split._1), split._2);
            } else {
                return Tuple.of(Node.color(node.left, Color.BLACK), Node.color(node.right, Color.BLACK));
            }
        }
    }

    private static <T> Tuple2<Node<T>, Boolean> unbalancedLeft(Color color, int blackHeight, RedBlackTree<T> left,
                                                               T value, RedBlackTree<T> right, Empty<T> empty) {
        if (!left.isEmpty()) {
            final Node<T> ln = (Node<T>) left;
            if (ln.color == Color.BLACK) {
                final Node<T> newNode = Node.balanceLeft(Color.BLACK, blackHeight, ln.color(Color.RED), value, right, empty);
                return Tuple.of(newNode, color == Color.BLACK);
            } else if (color == Color.BLACK && !ln.right.isEmpty()) {
                final Node<T> lrn = (Node<T>) ln.right;
                if (lrn.color == Color.BLACK) {
                    final Node<T> newRightNode = Node.balanceLeft(Color.BLACK, blackHeight, lrn.color(Color.RED), value, right,
                            empty);
                    final Node<T> newNode = new Node<>(Color.BLACK, ln.blackHeight, ln.left, ln.value, newRightNode,
                            empty);
                    return Tuple.of(newNode, false);
                }
            }
        }
        throw new IllegalStateException("unbalancedLeft(" + color + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
    }

    private static <T> Tuple2<Node<T>, Boolean> unbalancedRight(Color color, int blackHeight, RedBlackTree<T> left,
                                                                T value, RedBlackTree<T> right, Empty<T> empty) {
        if (!right.isEmpty()) {
            final Node<T> rn = (Node<T>) right;
            if (rn.color == Color.BLACK) {
                final Node<T> newNode = Node.balanceRight(Color.BLACK, blackHeight, left, value, rn.color(Color.RED), empty);
                return Tuple.of(newNode, color == Color.BLACK);
            } else if (color == Color.BLACK && !rn.left.isEmpty()) {
                final Node<T> rln = (Node<T>) rn.left;
                if (rln.color == Color.BLACK) {
                    final Node<T> newLeftNode = Node.balanceRight(Color.BLACK, blackHeight, left, value, rln.color(Color.RED),
                            empty);
                    final Node<T> newNode = new Node<>(Color.BLACK, rn.blackHeight, newLeftNode, rn.value, rn.right,
                            empty);
                    return Tuple.of(newNode, false);
                }
            }
        }
        throw new IllegalStateException("unbalancedRight(" + color + ", " + blackHeight + ", " + left + ", " + value + ", " + right + ")");
    }
}
