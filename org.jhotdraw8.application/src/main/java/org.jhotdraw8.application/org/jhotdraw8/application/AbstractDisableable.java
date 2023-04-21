/*
 * @(#)AbstractDisableable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxbase.control.Disableable;

/**
 * AbstractDisableable.
 * <p>
 * Binds {@code disabled} to {@code disablers.emptyProperty().not()}.
 * <p>
 * If a subclass wants to bind {@code disabled} to additional reasons, it must
 * unbind {@code disabled} first.
 *
 * @author Werner Randelshofer
 */
public class AbstractDisableable implements Disableable {

    /**
     * Holds the disablers.
     * <p>
     * This field is protected, so that it can be accessed by subclasses.
     */
    protected final ObservableSet<Object> disablers = FXCollections.observableSet();
    /**
     * Holds the disabled state.
     * <p>
     * This field is protected, so that it can be bound to or-combinations of
     * disablers.
     */
    protected final ReadOnlyBooleanWrapper disabled = new ReadOnlyBooleanWrapper(this, DISABLED_PROPERTY);

    {
        disabled.bind(Bindings.isNotEmpty(disablers));
    }

    public AbstractDisableable() {
    }

    @Override
    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled.getReadOnlyProperty();
    }

    @Override
    public @NonNull ObservableSet<Object> disablers() {
        return disablers;
    }
}
