/*
 * @(#)NullablePoint2DStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point2D;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.Point2DConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * Point2DStyleableKey.
 *
 */
public class NullablePoint2DStyleableKey extends AbstractStyleableKey<Point2D> implements WritableStyleableMapAccessor<Point2D> {


    private final Converter<Point2D> converter = new Point2DConverter(true);

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullablePoint2DStyleableKey(String name) {
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
    public NullablePoint2DStyleableKey(String key, @Nullable Point2D defaultValue) {
        super(key, Point2D.class, defaultValue);
    }

    @Override
    public Converter<Point2D> getCssConverter() {
        return converter;
    }
}
