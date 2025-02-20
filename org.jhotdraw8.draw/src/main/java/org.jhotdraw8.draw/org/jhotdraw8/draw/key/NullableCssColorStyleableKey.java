/*
 * @(#)NullableCssColorStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.ColorCssConverter;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * NullableCssColorStyleableKey.
 *
 */
public class NullableCssColorStyleableKey extends AbstractStyleableKey<CssColor>
        implements WritableStyleableMapAccessor<CssColor> {


    private final Converter<CssColor> converter = new ColorCssConverter(true);

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableCssColorStyleableKey(String name) {
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
    public NullableCssColorStyleableKey(String key, @Nullable CssColor defaultValue) {
        super(key, CssColor.class, defaultValue);
    }

    @Override
    public Converter<CssColor> getCssConverter() {
        return converter;
    }
}
