/*
 * @(#)AbstractReadOnlyStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.AbstractKey;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * AbstractReadOnlyStyleableKey.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public abstract class AbstractReadOnlyStyleableKey<T> extends AbstractKey<T> implements ReadOnlyStyleableMapAccessor<T> {
    private final String cssName;


    protected final Converter<T> converter;

    /**
     * Creates a new instance with the specified name, type token class, default
     * value null, and allowing null values.
     *
     * @param key       The name of the name.
     * @param clazz     The type of the value.
     * @param converter the converter
     */
    public AbstractReadOnlyStyleableKey(String key, Type clazz, Converter<T> converter) {
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
    public AbstractReadOnlyStyleableKey(String key, Type clazz,
                                        Converter<T> converter,
                                        @Nullable T defaultValue) {
        this(key, ReadOnlyStyleableMapAccessor.toCssName(key), clazz, converter, defaultValue);

    }

    /**
     * Creates a new key.
     *
     * @param name the model name of the key
     * @param cssName the CSS name of the key
     * @param clazz the type of the value
     * @param converter the CSS converter for the value
     * @param defaultValue the default value
     */
    public AbstractReadOnlyStyleableKey(String name, String cssName, Type clazz,
                                        Converter<T> converter,
                                        @Nullable T defaultValue) {
        super(name, clazz, defaultValue == null, defaultValue);
        this.converter = converter;
        this.cssName = cssName;
    }

    @Override
    public Converter<T> getCssConverter() {
        return converter;
    }


    @Override
    public String getCssName() {
        return cssName;
    }

}
