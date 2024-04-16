/*
 * @(#)AbstractDockable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;

/**
 * Abstract base class for implementations of {@link Dockable}.
 */
public abstract class AbstractDockable implements Dockable {
    protected final @NonNull ObjectProperty<DockParent> dockParent = new SimpleObjectProperty<>(this, DOCK_PARENT_PROPERTY);
    protected final @NonNull ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, GRAPHIC_PROPERTY);
    protected final @NonNull StringProperty text = new SimpleStringProperty(this, TEXT_PROPERTY);
    protected final @NonNull BooleanProperty showing = new SimpleBooleanProperty(this, SHOWING_PROPERTY);

    public AbstractDockable() {
    }

    @Override
    public @NonNull ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    @Override
    public @NonNull StringProperty textProperty() {
        return text;
    }

    @Override
    public @NonNull BooleanProperty showingProperty() {
        return showing;
    }

    @Override
    public @NonNull ObjectProperty<DockParent> dockParentProperty() {
        return dockParent;
    }

    @Override
    public @NonNull ReadOnlyList<DockChild> getDockChildrenReadOnly() {
        return VectorList.of();
    }

}
