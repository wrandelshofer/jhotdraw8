/*
 * @(#)NullablePaintableStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.PaintableCssConverter;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * PaintStyleableFigureKey.
 *
 * @author Werner Randelshofer
 */
public class NullablePaintableStyleableKey extends AbstractStyleableKey<Paintable> implements WritableStyleableMapAccessor<Paintable> {


    private final Converter<Paintable> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullablePaintableStyleableKey(String name) {
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
    public NullablePaintableStyleableKey(String key, Paintable defaultValue) {
        super(key, Paintable.class, defaultValue);

        converter = new PaintableCssConverter(true);
    }

    @Override
    public Converter<Paintable> getCssConverter() {
        return converter;
    }
}
