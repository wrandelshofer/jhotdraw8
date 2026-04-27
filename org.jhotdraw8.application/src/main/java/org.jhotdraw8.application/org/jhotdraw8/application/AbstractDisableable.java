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
import org.jhotdraw8.fxbase.control.Disableable;

/// AbstractDisableable.
///
/// Binds `disabled` to `disablers.emptyProperty().not()`.
///
/// If a subclass wants to bind `disabled` to additional reasons, it must
/// unbind `disabled` first.
public class AbstractDisableable implements Disableable {

    /// Holds the disablers.
    ///
    /// This field is protected, so that it can be accessed by subclasses.
    protected final ObservableSet<Object> disablers = FXCollections.observableSet();
    /// Holds the disabled state.
    ///
    /// This field is protected, so that it can be bound to or-combinations of
    /// disablers.
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
    public ObservableSet<Object> disablers() {
        return disablers;
    }
}
