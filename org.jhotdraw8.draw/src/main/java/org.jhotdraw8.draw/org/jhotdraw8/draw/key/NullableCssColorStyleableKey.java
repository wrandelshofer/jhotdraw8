/*
 * @(#)NullableCssColorStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.ColorCssConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

import java.io.Serial;

/**
 * NullableCssColorStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableCssColorStyleableKey extends AbstractStyleableKey<CssColor>
        implements WritableStyleableMapAccessor<CssColor> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Converter<CssColor> converter = new ColorCssConverter(true);

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableCssColorStyleableKey(@NonNull String name) {
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
    public NullableCssColorStyleableKey(@NonNull String key, CssColor defaultValue) {
        super(key, CssColor.class, defaultValue);
    }

    @Override
    public @NonNull Converter<CssColor> getCssConverter() {
        return converter;
    }
}
