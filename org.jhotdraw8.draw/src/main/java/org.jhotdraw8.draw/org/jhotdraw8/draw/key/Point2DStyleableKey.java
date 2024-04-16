/*
 * @(#)Point2DStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.Point2DConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * Point2DStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class Point2DStyleableKey extends AbstractStyleableKey<Point2D> implements WritableStyleableMapAccessor<Point2D>,
        NonNullKey<Point2D> {


    private final @NonNull Converter<Point2D> converter = new Point2DConverter(false);

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public Point2DStyleableKey(@NonNull String name) {
        this(name, Point2D.ZERO);
    }

    /**
     * Creates a new instance with the specified name, type token class, default
     * value, and allowing or disallowing null values.
     *
     * @param key          The name of the name. type parameters are given. Otherwise
     *                     specify them in arrow brackets.
     * @param defaultValue The default value.
     */
    public Point2DStyleableKey(@NonNull String key, @NonNull Point2D defaultValue) {
        super(key, Point2D.class, defaultValue);
    }

    @Override
    public @NonNull Converter<Point2D> getCssConverter() {
        return converter;
    }
}
