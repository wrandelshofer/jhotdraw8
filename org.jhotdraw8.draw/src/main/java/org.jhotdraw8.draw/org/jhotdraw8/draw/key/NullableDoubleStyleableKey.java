/*
 * @(#)NullableDoubleStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.CssDoubleConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * NullableDoubleStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableDoubleStyleableKey extends AbstractStyleableKey<Double> implements WritableStyleableMapAccessor<Double> {
    static final long serialVersionUID = 1L;

    private final Converter<Double> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableDoubleStyleableKey(@NonNull String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public NullableDoubleStyleableKey(@NonNull String name, Double defaultValue) {
        this(name, defaultValue, new CssDoubleConverter(true));
    }


    public NullableDoubleStyleableKey(@NonNull String name, Double defaultValue, CssConverter<Double> converter) {
        super(name, Double.class, defaultValue);
        this.converter = converter;
    }

    @Override
    public @NonNull Converter<Double> getCssConverter() {
        return converter;
    }
}
