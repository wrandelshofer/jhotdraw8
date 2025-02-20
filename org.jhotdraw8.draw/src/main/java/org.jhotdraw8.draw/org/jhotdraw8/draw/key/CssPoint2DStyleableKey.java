/*
 * @(#)CssPoint2DStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.converter.Point2DCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * Non-null CssPoint2DStyleableFigureKey.
 *
 */
public class CssPoint2DStyleableKey extends AbstractStyleableKey<CssPoint2D>
        implements WritableStyleableMapAccessor<CssPoint2D>, NonNullKey<CssPoint2D> {


    private final Converter<CssPoint2D> converter;

    /**
     * Creates a new instance with the specified name and with 0,0 as the
     * default value.
     *
     * @param name The name of the key.
     */
    public CssPoint2DStyleableKey(String name) {
        this(name, CssPoint2D.ZERO);
    }


    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name. type parameters are given. Otherwise
     *                     specify them in arrow brackets.
     * @param defaultValue The default value.
     */
    public CssPoint2DStyleableKey(String key, CssPoint2D defaultValue) {
        this(key, defaultValue, new Point2DCssConverter(false));
    }

    public CssPoint2DStyleableKey(String key, CssPoint2D defaultValue, CssConverter<CssPoint2D> converter) {
        super(key, CssPoint2D.class, defaultValue);
        this.converter = converter;
    }


    @Override
    public Converter<CssPoint2D> getCssConverter() {
        return converter;
    }

}
