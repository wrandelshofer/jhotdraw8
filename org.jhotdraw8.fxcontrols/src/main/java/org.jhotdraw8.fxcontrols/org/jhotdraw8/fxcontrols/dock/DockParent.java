/*
 * @(#)DockParent.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import org.jhotdraw8.icollection.facade.ReadableListFacade;
import org.jhotdraw8.icollection.readable.ReadableList;

/**
 * A DockParent provides screen space for one or more {@link DockChild}ren.
 * <p>
 * The DockParent lays out the screen space along an implementation-specific
 * {@link TrackAxis}.
 */
public interface DockParent extends DockChild {
    /**
     * The name of the {@link #editableProperty()}.
     */
    String EDITABLE_PROPERTY = "editable";

    ObservableList<DockChild> getDockChildren();

    @Override
    default ReadableList<DockChild> getDockChildrenReadOnly() {
        return new ReadableListFacade<>(getDockChildren());
    }

    /**
     * Whether this dock parent is editable.
     *
     * @return true if this dock parent is editable.
     */
    BooleanProperty editableProperty();

    /**
     * Returns whether the user can edit this dock parent.
     *
     * @return true if the user can edit this dock parent.
     */
    default boolean isEditable() {
        return editableProperty().get();
    }

    /**
     * Sets whether the user can edit this dock parent.
     *
     * @param value true if the user can edit this dock parent.
     */
    default void setEditable(boolean value) {
        editableProperty().set(value);
    }

    TrackAxis getDockAxis();

    /**
     * Returns true if this parent dock resizes the items. If this method returns
     * true, a dock child should not provide resize controls.
     *
     * @return true if the track resizes items.
     */
    boolean isResizesDockChildren();
}
