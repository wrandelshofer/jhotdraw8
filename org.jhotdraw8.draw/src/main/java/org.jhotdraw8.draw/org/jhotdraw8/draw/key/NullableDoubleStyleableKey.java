/*
 * @(#)NullableDoubleStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * NullableDoubleStyleableKey.
 *
 */
public class NullableDoubleStyleableKey extends AbstractStyleableKey<Double> implements WritableStyleableMapAccessor<Double> {
    private static final long serialVersionUID = 1L;

    private final Converter<Double> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableDoubleStyleableKey(String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public NullableDoubleStyleableKey(String name, @Nullable Double defaultValue) {
        this(name, defaultValue, new DoubleCssConverter(true));
    }


    public NullableDoubleStyleableKey(String name, @Nullable Double defaultValue, CssConverter<Double> converter) {
        super(name, Double.class, defaultValue);
        this.converter = converter;
    }

    @Override
    public Converter<Double> getCssConverter() {
        return converter;
    }
}
