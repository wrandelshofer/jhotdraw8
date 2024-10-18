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
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.readable.ReadableList;

/**
 * Abstract base class for implementations of {@link Dockable}.
 */
public abstract class AbstractDockable implements Dockable {
    protected final ObjectProperty<DockParent> dockParent = new SimpleObjectProperty<>(this, DOCK_PARENT_PROPERTY);
    protected final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, GRAPHIC_PROPERTY);
    protected final StringProperty text = new SimpleStringProperty(this, TEXT_PROPERTY);
    protected final BooleanProperty showing = new SimpleBooleanProperty(this, SHOWING_PROPERTY);

    public AbstractDockable() {
    }

    @Override
    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    @Override
    public StringProperty textProperty() {
        return text;
    }

    @Override
    public BooleanProperty showingProperty() {
        return showing;
    }

    @Override
    public ObjectProperty<DockParent> dockParentProperty() {
        return dockParent;
    }

    @Override
    public ReadableList<DockChild> getDockChildrenReadOnly() {
        return VectorList.of();
    }

}
