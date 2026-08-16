/*
 * @(#)DockRoot.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.application.Activity;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

/// The root node of a docking hierarchy.
///
/// The root node manages drag and drop of [Dockable] nodes, and
/// creates or destroys [Track] nodes that hold the [Dockable]s.
public interface DockRoot extends DockParent {
    /// Data format used for dragging a DockItem with the drag board.
    /// The value of this data format is the [System#identityHashCode(Object)]
    /// of the dragged leaf.
    DataFormat DOCKABLE_DATA_FORMAT = new DataFormat("application/x-jhotdraw8-dragged-dock-leaf");
    /// We store the dragged item here, because we move the _reference_
    /// of a DockItem with the drag board rather than a value of the DockItem.
    ObjectProperty<Dockable> draggedDockable = new SimpleObjectProperty<>();

    static ObjectProperty<Dockable> draggedDockableProperty() {
        return draggedDockable;
    }

    static @Nullable Dockable getDraggedDockable() {
        return draggedDockable.get();
    }

    static void setDraggedDockable(@Nullable Dockable value) {
        draggedDockable.set(value);
    }


    /// Only [Dockable]s accepted by this filter can be docked.
    ///
    /// This can be used to restrict docking to dockables that belong
    /// to the same [Activity].
    ///
    /// @return filter for accepting [Dockable]s
    ObjectProperty<Predicate<Dockable>> dockablePredicateProperty();

    default Predicate<Dockable> getDockablePredicate() {
        return dockablePredicateProperty().get();
    }

    default void setDockablePredicate(Predicate<Dockable> value) {
        dockablePredicateProperty().set(value);
    }

    @Override
    Parent getNode();
}
