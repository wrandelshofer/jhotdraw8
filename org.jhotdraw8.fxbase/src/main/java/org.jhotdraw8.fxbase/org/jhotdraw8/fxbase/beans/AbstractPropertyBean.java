/*
 * @(#)AbstractPropertyBean.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.beans;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

import java.util.LinkedHashMap;

/**
 * An abstrac implementation of the {@link PropertyBean} interface.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractPropertyBean implements PropertyBean {

    /**
     * Holds the properties.
     */
    protected final ObservableMap<Key<?>, Object> properties = FXCollections.observableMap(new LinkedHashMap<>());

    public AbstractPropertyBean() {
    }

    @Override
    public final @NonNull ObservableMap<Key<?>, Object> getProperties() {
        return properties;
    }

    @Override
    public <T> @Nullable T get(@NonNull MapAccessor<T> key) {
        return PropertyBean.super.get(key);
    }

    @Override
    public <T> @NonNull T getNonNull(@NonNull NonNullMapAccessor<T> key) {
        return PropertyBean.super.getNonNull(key);
    }

}
