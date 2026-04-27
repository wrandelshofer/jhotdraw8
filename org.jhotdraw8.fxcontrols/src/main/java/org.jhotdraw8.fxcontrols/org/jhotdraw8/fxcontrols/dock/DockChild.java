/*
 * @(#)DockChild.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.BooleanProperty;

/// Represents a dock item that can be docked to a `DockParent`.
public interface DockChild extends DockNode {
    /// The name of the [#showingProperty()] ()}.
    String SHOWING_PROPERTY = "showing";

    /// Whether this dock child is showing.
    ///
    /// A dock child that is not showing should not consume CPU resources.
    ///
    /// This property is set by [DockParent], for example depending
    /// on whether this dockable is in a collapsed pane.
    ///
    /// @return true if this dock child is showing.
    BooleanProperty showingProperty();


    default boolean isShowing() {
        return showingProperty().get();
    }

    default void setShowing(boolean newValue) {
        showingProperty().set(newValue);
    }
}
