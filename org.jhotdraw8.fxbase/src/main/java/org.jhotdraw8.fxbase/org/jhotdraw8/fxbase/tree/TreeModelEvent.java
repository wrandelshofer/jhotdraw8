/*
 * @(#)TreeModelEvent.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.tree;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Event;

/**
 * TreeModelEvent.
 *
 * @param <N> the node type
 * @author Werner Randelshofer
 */
public class TreeModelEvent<N> extends Event<TreeModel<N>> {

    private static final long serialVersionUID = 1L;

    public enum EventType {
        /**
         * The root of the model changed.
         * <ul>
         * <li>node is the new root.</li>
         * <li>root is also the new root.</li>
         * <li>parent is the old root.</li>
         * </ul>
         */
        ROOT_CHANGED,
        /**
         * All JavaFX Nodes in a subtree of the figures have been invalidated.
         */
        SUBTREE_NODES_CHANGED,
        /**
         * A subtree of figures  has been added to a parent.
         * <p>
         * The subtree of figures is already part of the root, and has just been removed from another parent.
         */
        NODE_ADDED_TO_PARENT,
        /**
         * A subtree of figures has been removed from its parent.
         * <p>
         * The subtree of figures is still part of the root, and is about to be added to another parent.
         */
        NODE_REMOVED_FROM_PARENT,
        /**
         * A subtree of figures has been added to the root.
         * <p>
         * The subtree of figures has become part of the root.
         * This event is fired, before NODE_ADDED_TO_PARENT is fired.
         */
        NODE_ADDED_TO_TREE,
        /**
         * A subtree of figures has been removed from the root.
         * <p>
         * The subtree of figures is no longer part of the root.
         * This event is fired, after NODE_REMOVED_FROM_PARENT is fired.
         */
        NODE_REMOVED_FROM_TREE,
        /**
         * The JavaFX Node of a single figure has been invalidated.
         */
        NODE_CHANGED,

    }

    private final N node;

    private final N parentOrOldRoot;
    private final N root;
    private final int childIndex;
    private final TreeModelEvent.EventType eventType;

    private TreeModelEvent(@NonNull TreeModel<N> source, EventType eventType, N node, N parentOrOldRoot, N root, int childIndex) {
        super(source);
        this.node = node;
        this.parentOrOldRoot = parentOrOldRoot;
        this.root = root;
        this.childIndex = childIndex;
        this.eventType = eventType;
    }

    public static @NonNull <E> TreeModelEvent<E> subtreeNodesInvalidated(@NonNull TreeModel<E> source, E subtreeRot) {
        return new TreeModelEvent<>(source, EventType.SUBTREE_NODES_CHANGED, subtreeRot, null, null, -1);
    }

    public static @NonNull <E> TreeModelEvent<E> nodeAddedToParent(@NonNull TreeModel<E> source, E child, E parent, int index) {
        return new TreeModelEvent<>(source, EventType.NODE_ADDED_TO_PARENT, child, parent, null, index);
    }

    public static @NonNull <E> TreeModelEvent<E> nodeRemovedFromParent(@NonNull TreeModel<E> source, E child, E parent, int index) {
        return new TreeModelEvent<>(source, EventType.NODE_REMOVED_FROM_PARENT, child, parent, null, index);
    }

    public static @NonNull <E> TreeModelEvent<E> nodeAddedToTree(@NonNull TreeModel<E> source, E root, E node) {
        return new TreeModelEvent<>(source, EventType.NODE_ADDED_TO_TREE, node, null, root, -1);
    }

    public static @NonNull <E> TreeModelEvent<E> nodeRemovedFromTree(@NonNull TreeModel<E> source, E root, E node) {
        return new TreeModelEvent<>(source, EventType.NODE_REMOVED_FROM_TREE, node, null, root, -1);
    }

    public static @NonNull <E> TreeModelEvent<E> nodeChanged(@NonNull TreeModel<E> source, E node) {
        return new TreeModelEvent<>(source, EventType.NODE_CHANGED, node, null, null, -1);
    }

    public static @NonNull <E> TreeModelEvent<E> rootChanged(@NonNull TreeModel<E> source, @Nullable E oldRoot, @Nullable E newRoot) {
        return new TreeModelEvent<>(source, EventType.ROOT_CHANGED, newRoot, oldRoot, newRoot, -1);
    }

    /**
     * If the root has changed, returns the old root.
     */
    public @Nullable N getOldRoot() {
        if (EventType.ROOT_CHANGED != eventType) {
            throw new IllegalStateException();
        }
        return parentOrOldRoot;
    }

    /**
     * If the root has changed, returns the new root.
     */
    public @Nullable N getNewRoot() {
        if (EventType.ROOT_CHANGED != eventType) {
            throw new IllegalStateException();
        }
        return root;
    }

    /**
     * The figure which was added, removed or of which a property changed.
     *
     * @return the figure
     */
    public N getNode() {
        return node;
    }

    /**
     * If a child was added or removed from a parent, returns the parent.
     *
     * @return the parent
     */
    public N getParent() {
        return parentOrOldRoot;
    }

    /**
     * If a child was added or removed from a root, returns the root.
     *
     * @return the root
     */
    public N getRoot() {
        return root;
    }

    /**
     * If a child was added or removed, returns the child.
     *
     * @return the child
     */
    public N getChild() {
        return node;
    }

    /**
     * If the figure was added or removed, returns the child index.
     *
     * @return an index. Returns -1 if the figure was neither added or removed.
     */
    public int getChildIndex() {
        return childIndex;
    }

    /**
     * Returns the event type.
     *
     * @return the event type
     */
    public TreeModelEvent.EventType getEventType() {
        return eventType;
    }

    @Override
    public @NonNull String toString() {
        return "TreeModelEvent{"
                + "node=" + node
                + ", parent=" + parentOrOldRoot
                + ", index=" + childIndex + ", eventType="
                + eventType + ", source=" + source + '}';
    }


}
