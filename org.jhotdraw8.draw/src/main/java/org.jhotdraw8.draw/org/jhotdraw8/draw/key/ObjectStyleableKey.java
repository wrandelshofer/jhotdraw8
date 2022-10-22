/*
 * @(#)ObjectStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.collection.typesafekey.AbstractKey;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

import java.lang.reflect.Type;

public class ObjectStyleableKey<T> extends AbstractKey<T> implements WritableStyleableMapAccessor<T> {
    private static final long serialVersionUID = 0L;

    private final @NonNull Converter<T> converter;

    public ObjectStyleableKey(String name, Class<T> clazz, @NonNull Converter<T> converter) {
        this(name, clazz, null, converter);
    }

    public ObjectStyleableKey(String name, TypeToken<T> clazz, @NonNull Converter<T> converter) {
        this(name, clazz, null, converter);
    }

    public ObjectStyleableKey(String name, TypeToken<T> clazz, T defaultValue, @NonNull Converter<T> converter) {
        this(name, clazz.getType(), defaultValue, converter);
    }

    public ObjectStyleableKey(String name, Type clazz, T defaultValue, @NonNull Converter<T> converter) {
        super(name, clazz, defaultValue == null, false, defaultValue);
        this.converter = converter;
    }

    @Override
    public @NonNull Converter<T> getCssConverter() {
        return converter;
    }

    @Override
    public @NonNull String getCssName() {
        return getName();
    }
}
