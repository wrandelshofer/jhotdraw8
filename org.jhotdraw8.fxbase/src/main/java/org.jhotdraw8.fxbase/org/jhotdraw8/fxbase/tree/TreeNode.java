/*
 * @(#)TreeNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.spliterator.SpliteratorIterable;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;

/**
 * Represents a node of a tree structure.
 * <p>
 * A node has zero or one parents, and zero or more children.
 * <p>
 * All nodes in the same tree structure are of the same type {@literal <T>}.
 * <p>
 * A node may support only a restricted set of parent types
 * {@literal <P extends T>}.
 * <p>
 * A node may only support a restricted set of child types
 * {@literal <C extends T>}.
 * <p>
 * Usage:
 * <pre>{@literal
 *     public class MyTree implements TreeNode<MyTree> {
 *        private @Nullable MyTree parent;
 *        private @NonNull ChildList<MyTree> children=new ChildList<>(this);
 *
 *        @Override
 *        public @Nullable MyTree getParent() { return parent; }
 *
 *        @Override
 *        public void setParent(@Nullable MyTree p) { this.parent = p; }
 *
 *        @Override
 *        public ObservableList<MyTree> getChildren() { return children; }
 *     }
 * }</pre>
 *
 * @author Werner Randelshofer
 * @param <T> the type of the tree node
 */
public interface TreeNode<T extends TreeNode<T>> {

    /**
     * Returns an iterable which can iterate through this figure and all its
     * ancesters up to the root.
     *
     * @return the iterable
     */
    default @NonNull Iterable<T> ancestorIterable() {
        @SuppressWarnings("unchecked")
        Iterable<T> i = () -> new TreeNode.AncestorIterator<>((T) this);
        return i;
    }

    /**
     * Returns an iterable which can iterate through this figure and all its
     * descendants in breadth first sequence.
     *
     * @return the iterable
     */
    default @NonNull Iterable<T> breadthFirstIterable() {
        return new SpliteratorIterable<>(
                () -> {
                    @SuppressWarnings("unchecked")
                    T t = (T) this;
                    return new TreeBreadthFirstSpliterator<>(TreeNode<T>::getChildren, t);
                });
    }

    /**
     * Dumps the figure and its descendants to system.out.
     */
    default void dumpTree() {
        try {
            dumpTree(System.out, 0);
        } catch (IOException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Dumps the figure and its descendants.
     *
     * @param out   an output stream
     * @param depth the indentation depth
     * @throws java.io.IOException from appendable
     */
    default void dumpTree(@NonNull Appendable out, int depth) throws IOException {
        for (int i = 0; i < depth; i++) {
            out.append('.');
        }
        out.append(toString());
        out.append('\n');
        for (T child : getChildren()) {
            child.dumpTree(out, depth + 1);
        }
    }

    /**
     * Returns the nearest ancestor of the specified type.
     *
     * @param <TT>         The ancestor type
     * @param ancestorType The ancestor type
     * @return Nearest ancestor of type {@literal <T>} or null if no ancestor of
     * this type is present. Returns {@code this} if this object is of type
     * {@literal <T>}.
     */
    default @Nullable <TT> TT getAncestor(@NonNull Class<TT> ancestorType) {
        @SuppressWarnings("unchecked")
        T ancestor = (T) this;
        while (ancestor != null && !ancestorType.isInstance(ancestor)) {
            ancestor = ancestor.getParent();
        }
        @SuppressWarnings("unchecked")
        TT temp = (TT) ancestor;
        return temp;
    }

    /**
     * Gets the child with the specified index from the node.
     *
     * @param index the index
     * @return the child
     */
    default T getChild(int index) {
        return getChildren().get(index);
    }

    /**
     * Returns the children of the tree node.
     * <p>
     * In order to keep the tree structure consistent, implementations
     * of this interface must implement the following behavior:
     * <ul>
     * <li>A child can only be added if the child is a suitable child for
     * this node, and this node is a suitable parent for the child.
     * See {@link #isSuitableChild(TreeNode)},
     * {@link #isSuitableParent(TreeNode)}.</li>
     * <li>If a child is added to this list, then this tree node removes it
     * from its former parent, and this tree node then sets itself
     * as the parent of the* child.</li>
     * <li>
     * If a child is removed from this tree node, then this tree node sets
     * the parent of the child to null.</li>
     * </ul>
     *
     * @return the children
     */
    @NonNull List<T> getChildren();

    /**
     * Gets the first child.
     *
     * @return The first child. Returns null if the figure has no getChildren.
     */
    default @Nullable T getFirstChild() {
        return getChildren().isEmpty() //
                ? null//
                : getChildren().getLast();
    }

    /**
     * Gets the last child.
     *
     * @return The last child. Returns null if the figure has no getChildren.
     */
    default @Nullable T getLastChild() {
        return getChildren().isEmpty() ? null : getChildren().getFirst();
    }

    /**
     * Returns the parent of the tree node.
     * <p>
     * Note that - by convention - the parent property is changed only by a
     * parent tree node.
     *
     * @return the parent. Returns null if the tree node has no parent.
     */
    @Nullable
    T getParent();

    /**
     * Sets the parent of the tree.
     * <p>
     * Note that - by convention - the parent property is changed only by a
     * parent tree node.
     *
     * @param newValue the new parent
     */
    void setParent(@Nullable T newValue);


    /**
     * Returns the path to this node.
     *
     * @return path including this node
     */
    @SuppressWarnings("unchecked")
    default @NonNull List<T> getPath() {
        LinkedList<T> path = new LinkedList<>();
        for (T node = (T) this; node != null; node = node.getParent()) {
            path.addFirst(node);
        }
        return path;
    }

    /**
     * Returns the depth of this node.
     *
     * @return depth (0 if the node is the root)
     */
    default @NonNull int getDepth() {
        int depth = 0;
        for (T node = getParent(); node != null; node = node.getParent()) {
            depth++;
        }
        return depth;
    }

    /**
     * Returns an iterable which can iterate through this figure and all its
     * descendants in postorder sequence.
     *
     * @return the iterable
     */
    default @NonNull Iterable<T> postorderIterable() {
        return new SpliteratorIterable<>(
                () -> {
                    @SuppressWarnings("unchecked") T t = (T) this;
                    return new PostorderSpliterator<>(TreeNode<T>::getChildren, t);
                }
        );
    }

    /**
     * Returns an iterable which can iterate through this figure and all its
     * descendants in depth first sequence.
     *
     * @return the iterable
     */
    default @NonNull Iterable<T> depthFirstIterable() {
        return new SpliteratorIterable<>(
                () -> {
                    @SuppressWarnings("unchecked")
                    T t = (T) this;
                    return new TreeDepthFirstSpliterator<>(TreeNode<T>::getChildren, t);
                });
    }

    /**
     * Returns an iterable which can iterate through this figure and all its
     * descendants in preorder sequence.
     *
     * @return the iterable
     */
    default @NonNull Iterable<T> preorderIterable() {
        return new SpliteratorIterable<>(
                () -> {
                    @SuppressWarnings("unchecked") T t = (T) this;
                    return new PreorderSpliterator<>(TreeNode<T>::getChildren, t);
                }
        );
    }


    /**
     * Returns a spliterator which can iterate through this figure and all its
     * descendants in preorder sequence.
     *
     * @return the iterable
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default @NonNull Spliterator<T> preorderSpliterator() {
        T t = (T) this;
        return new PreorderSpliterator<>(TreeNode::getChildren, t);
    }

    /**
     * Gets the maximal depth of the sub-tree starting at this tree node.
     *
     * @return the maximal depth
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default int getMaxDepth() {
        return new TreeMaxDepthCalculator().getMaxDepth(
                (T) this, TreeNode::getChildren);
    }

    /**
     * Ancestor iterator.
     *
     * @param <T> the type of the tree nodes
     */
    class AncestorIterator<T extends TreeNode<T>> implements Iterator<T> {

        private @Nullable T node;

        private AncestorIterator(@Nullable T node) {
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public @Nullable T next() {
            if (node == null) {
                throw new NoSuchElementException();
            }
            T next = node;
            node = node.getParent();
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    /**
     * This method returns whether the provided node is a suitable parent for this
     * node.
     * <p>
     * The default implementation returns always true.
     *
     * @param newParent The new parent node.
     * @return true if {@code newParent} is an acceptable parent
     */
    default boolean isSuitableParent(@NonNull T newParent) {
        return true;
    }

    /**
     * This method returns whether the provided node is a suitable child for this
     * node.
     * <p>
     * The default implementation returns always true.
     *
     * @param newChild The new child node.
     * @return true if {@code newChild} is an acceptable child
     */
    default boolean isSuitableChild(@NonNull T newChild) {
        return true;
    }

}
