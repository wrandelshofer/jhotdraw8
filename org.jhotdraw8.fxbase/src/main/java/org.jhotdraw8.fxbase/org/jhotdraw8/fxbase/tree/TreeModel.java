/*
 * @(#)TreeModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.tree;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.fxbase.beans.ObservableMixin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TreeModel.
 *
 * @param <N> the node type
 * @author Werner Randelshofer
 */
public interface TreeModel<N> extends ObservableMixin {
    /**
     * Name of the root property.
     */
    String ROOT_PROPERTY = "root";

    /**
     * List of drawing model listeners.
     *
     * @return a list of drawing model listeners
     */
    @NonNull CopyOnWriteArrayList<Listener<TreeModelEvent<N>>> getTreeModelListeners();

    /**
     * List of invalidation listeners.
     *
     * @return a list of drawing model listeners
     */
    @Override
    @NonNull CopyOnWriteArrayList<InvalidationListener> getInvalidationListeners();

    /**
     * The root of the drawing model.
     *
     * @return the root
     */
    @NonNull
    ObjectProperty<N> rootProperty();

    /**
     * Adds a listener for {@code TreeModelEvent<E>}s.
     *
     * @param l the listener
     */
    default void addTreeModelListener(@NonNull Listener<TreeModelEvent<N>> l) {
        getTreeModelListeners().add(l);
    }

    /**
     * Removes a listener for {@code TreeModelEvent<E>}s.
     *
     * @param l the listener
     */
    default void removeTreeModelListener(@NonNull Listener<TreeModelEvent<N>> l) {
        getTreeModelListeners().remove(l);
    }

    /**
     * Gets the root of the tree.
     *
     * @return the drawing
     */
    default @Nullable N getRoot() {
        return rootProperty().get();
    }

    /**
     * Sets the root of the tree and fires appropriate
     * {@code TreeModelEvent<E>}s.
     *
     * @param root the new root
     */
    default void setRoot(@Nullable N root) {
        rootProperty().set(root);
    }

    /**
     * Gets the children of the specified node.
     *
     * @param node the node.
     * @return the getChildren.
     */
    @NonNull List<N> getChildren(@NonNull N node);

    /**
     * Gets the child count of the specified figure.
     *
     * @param node the parent.
     * @return the number of getChildren
     */
    int getChildCount(@NonNull N node);

    /**
     * Gets the child at the given index from the parent.
     *
     * @param parent the parent.
     * @param index  the index.
     * @return the child
     */
    @NonNull N getChild(@NonNull N parent, int index);

    /**
     * Removes the specified child from its parent and fires appropriate
     * {@code TreeModelEvent<E>}s.
     *
     * @param child the child
     */
    void removeFromParent(@NonNull N child);

    /**
     * Adds the specified child to a parent and fires appropriate
     * {@code TreeModelEvent<E>}s.
     *
     * @param child  the new child
     * @param parent the parent.
     * @param index  the index
     */
    void insertChildAt(@NonNull N child, @NonNull N parent, int index);

    /**
     * Adds the specified child to a parent and fires appropriate
     * {@code TreeModelEvent<E>}s.
     *
     * @param child  the new child
     * @param parent the parent.
     */
    default void addChildTo(@NonNull N child, @NonNull N parent) {
        insertChildAt(child, parent, getChildCount(parent));
    }

    /**
     * Fires the specified event.
     *
     * @param event the event
     */
    default void fireTreeModelEvent(@NonNull TreeModelEvent<N> event) {
        for (Listener<TreeModelEvent<N>> l : getTreeModelListeners()) {
            l.handle(event);
        }
    }

    // ---
    // convenience methods
    // ---

    /**
     * Fires "node invalidated" event for the specified node.
     *
     * @param node the node
     */
    default void fireNodeInvalidated(@NonNull N node) {
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, node));
    }
}
