/*
 * @(#)CssDimension2DStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.draw.css.converter.CssDimension2DConverter;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * Non-null CssDimension2DStyleableFigureKey.
 *
 * @author Werner Randelshofer
 */
public class CssDimension2DStyleableKey extends AbstractStyleableKey<@NonNull CssDimension2D>
        implements WritableStyleableMapAccessor<@NonNull CssDimension2D>, NonNullKey<@NonNull CssDimension2D> {

    private static final long serialVersionUID = 1L;
    private final Converter<@NonNull CssDimension2D> converter;

    /**
     * Creates a new instance with the specified name and with 0,0 as the
     * default value.
     *
     * @param name The name of the key.
     */
    public CssDimension2DStyleableKey(@NonNull String name) {
        this(name, CssDimension2D.ZERO);
    }


    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name. type parameters are given. Otherwise
     *                     specify them in arrow brackets.
     * @param defaultValue The default value.
     */
    public CssDimension2DStyleableKey(@NonNull String key, @NonNull CssDimension2D defaultValue) {
        this(key, defaultValue, new CssDimension2DConverter(false));
    }

    public CssDimension2DStyleableKey(@NonNull String key, @NonNull CssDimension2D defaultValue, @NonNull CssConverter<CssDimension2D> converter) {
        super(key, CssDimension2D.class, defaultValue);
        this.converter = converter;
    }


    @Override
    public @NonNull Converter<CssDimension2D> getCssConverter() {
        return converter;
    }

}
