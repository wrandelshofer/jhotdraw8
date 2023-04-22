/*
 * @(#)NullableAwtSvgPathStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.CssAwtSvgPathConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

import java.awt.geom.Path2D;
import java.io.Serial;

/**
 * NullableAwtSvgPathStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableAwtSvgPathStyleableKey extends AbstractStyleableKey<Path2D.Double> implements WritableStyleableMapAccessor<Path2D.Double> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull Converter<Path2D.Double> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableAwtSvgPathStyleableKey(@NonNull String name) {
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
    public NullableAwtSvgPathStyleableKey(@NonNull String key, @Nullable Path2D.Double defaultValue) {
        super(null, key, Path2D.Double.class, true, defaultValue);

        converter = new CssAwtSvgPathConverter(isNullable());
    }

    @Override
    public @NonNull Converter<Path2D.Double> getCssConverter() {
        return converter;
    }
}
