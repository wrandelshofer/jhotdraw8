/*
 * @(#)AbstractReadOnlyStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.key.AbstractKey;
import org.jhotdraw8.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.text.Converter;

import java.lang.reflect.Type;

/**
 * SimpleReadOnlyStyleableKey.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractReadOnlyStyleableKey<T> extends AbstractKey<T> implements ReadOnlyStyleableMapAccessor<T> {
    private final @NonNull String cssName;
    private static final long serialVersionUID = 1L;

    protected final @NonNull Converter<T> converter;

    /**
     * Creates a new instance with the specified name, type token class, default
     * value null, and allowing null values.
     *
     * @param key       The name of the name.
     * @param clazz     The type of the value.
     * @param converter the converter
     */
    public AbstractReadOnlyStyleableKey(@NonNull String key, @NonNull Type clazz, @NonNull Converter<T> converter) {
        this(key, clazz, converter, null);
    }


    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name.
     * @param clazz        The type of the value.
     * @param converter    the converter
     * @param defaultValue The default value.
     */
    public AbstractReadOnlyStyleableKey(@NonNull String key, @NonNull Type clazz,
                                        @NonNull Converter<T> converter,
                                        @Nullable T defaultValue) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), clazz, converter, defaultValue);

    }

    /**
     * Creates a new key.
     *
     * @param xmlName the XML name of the key
     * @param cssName the CSS name of the key
     * @param clazz the type of the value
     * @param converter the CSS converter for the value
     * @param defaultValue the default value
     */
    public AbstractReadOnlyStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Type clazz,
                                        @NonNull Converter<T> converter,
                                        @Nullable T defaultValue) {
        super(xmlName, clazz, defaultValue == null, defaultValue);
        this.converter = converter;
        this.cssName = cssName;
    }

    @Override
    public @NonNull Converter<T> getCssConverter() {
        return converter;
    }


    @Override
    public @NonNull String getCssName() {
        return cssName;
    }

}
