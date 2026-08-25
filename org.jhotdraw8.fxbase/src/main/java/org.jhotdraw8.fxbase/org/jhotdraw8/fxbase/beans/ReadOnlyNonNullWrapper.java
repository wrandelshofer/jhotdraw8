/*
 * @(#)ReadOnlyNonNullWrapper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.Objects;

/// ReadOnlyNonNullWrapper.
///
/// @param <T> the type of the wrapped object
public class ReadOnlyNonNullWrapper<T> extends ReadOnlyObjectWrapper<T> {
    /// Creates a new instance
    ///
    /// @param bean         the bean
    /// @param name         the name of the property
    /// @param initialValue the initial value
    public ReadOnlyNonNullWrapper(Object bean, String name, T initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        Objects.requireNonNull(get(), "new value");
        super.fireValueChangedEvent();
    }

}
