/*
 * @(#)NullableSvgPathStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.SvgPathCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * NullableSvgPathStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableSvgPathStyleableKey extends AbstractStyleableKey<String> implements WritableStyleableMapAccessor<String> {



    private final @NonNull Converter<String> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableSvgPathStyleableKey(@NonNull String name) {
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
    @SuppressWarnings("this-escape")
    public NullableSvgPathStyleableKey(@NonNull String key, String defaultValue) {
        super(null, key, String.class, true, defaultValue);

        converter = new SvgPathCssConverter(isNullable());
    }

    @Override
    public @NonNull Converter<String> getCssConverter() {
        return converter;
    }
}
