/*
 * @(#)ReadOnlyNonNullWrapper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.Objects;

/**
 * ReadOnlyNonNullWrapper.
 *
 * @param <T> the type of the wrapped object
 */
public class ReadOnlyNonNullWrapper<T> extends ReadOnlyObjectWrapper<T> {

    public ReadOnlyNonNullWrapper(Object bean, String name, T initialValue) {
        super(bean, name, initialValue);
    }

    @Override
    protected void fireValueChangedEvent() {
        Objects.requireNonNull(get(), "new value");
        super.fireValueChangedEvent();
    }

}
