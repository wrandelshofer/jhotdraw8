/*
 * @(#)NullableBooleanStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.BooleanCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * NonNullBooleanStyleableKey.
 *
 */
public class NullableBooleanStyleableKey extends AbstractStyleableKey<Boolean>
        implements WritableStyleableMapAccessor<Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableBooleanStyleableKey(String name) {
        this(name, null);
    }


    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name. type parameters are given. Otherwise
     *                     specify them in arrow brackets.
     * @param defaultValue The default value.
     */
    public NullableBooleanStyleableKey(String key, @Nullable Boolean defaultValue) {
        this(null, key, defaultValue);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name. type parameters are given. Otherwise
     *                     specify them in arrow brackets.
     * @param defaultValue The default value.
     */
    public NullableBooleanStyleableKey(@Nullable String namespace, String key, @Nullable Boolean defaultValue) {
        super(namespace, key, Boolean.class, true, defaultValue);
    }

    private Converter<Boolean> converter;

    @Override
    public Converter<Boolean> getCssConverter() {
        if (converter == null) {
            converter = new BooleanCssConverter(isNullable());
        }
        return converter;
    }
}
