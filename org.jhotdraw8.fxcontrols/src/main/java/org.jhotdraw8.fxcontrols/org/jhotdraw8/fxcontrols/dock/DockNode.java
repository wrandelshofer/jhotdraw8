/*
 * @(#)DockNode.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jspecify.annotations.Nullable;

/**
 * Represents a node in a tree structure.
 */
public interface DockNode {
    /**
     * The name of the {@link #dockParentProperty()}.
     */
    String DOCK_PARENT_PROPERTY = "dockParent";

    /**
     * Gets the parent of this node.
     */
    ObjectProperty<DockParent> dockParentProperty();

    /**
     * Gets the children of this node.
     *
     * @return the children
     */
    ReadableList<DockChild> getDockChildrenReadOnly();

    default @Nullable DockRoot getDockRoot() {
        for (DockNode node = this; node != null; node = node.getDockParent()) {
            if (node instanceof DockRoot) {
                return (DockRoot) node;
            }
        }
        return null;
    }

    default @Nullable DockParent getDockParent() {
        return dockParentProperty().get();
    }

    default void setDockParent(@Nullable DockParent value) {
        dockParentProperty().set(value);
    }

    Node getNode();
}
