/*
 * @(#)AbstractAction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.AbstractDisableable;
import org.jhotdraw8.fxcollection.typesafekey.Key;

/**
 * AbstractAction.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractAction extends AbstractDisableable implements Action {

    /**
     * Holds the properties.
     */
    protected final ObservableMap<Key<?>, Object> properties//
            = FXCollections.observableHashMap();

    private final BooleanProperty selected = new SimpleBooleanProperty(this, SELECTED_PROPERTY);

    /**
     * Creates a new instance. Binds {@code disabled} to {@code disable}.
     */
    public AbstractAction() {
        this(null);

    }

    /**
     * Creates a new instance. Binds {@code disabled} to {@code disable}.
     *
     * @param id the id of the action
     */
    public AbstractAction(String id) {
        set(Action.ID_KEY, id);

    }

    @Override
    public final @NonNull ObservableMap<Key<?>, Object> getProperties() {
        return properties;
    }

    /**
     * Invokes {@link #onActionPerformed} if the action is not disabled and the
     * event is not consumed. Consumes the event after invoking {@code
     * handleActionPerformed}.
     *
     * @param event the action event
     */
    @Override
    public final void handle(@NonNull ActionEvent event) {
        if (!isDisabled() && !event.isConsumed()) {
            onActionPerformed(event);
            event.consume();
        }
    }

    /**
     * This method is invoked when the action is not disabled and the event is
     * not consumed.
     *
     * @param event the action event
     */
    protected abstract void onActionPerformed(@NonNull ActionEvent event);

    @Override
    public @NonNull BooleanProperty selectedProperty() {
        return selected;
    }
}
