/*
 * @(#)DoubleStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * DoubleStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class DoubleStyleableKey
        extends AbstractStyleableKey<Double>
        implements WritableStyleableMapAccessor<Double>,
        NonNullKey<Double> {

    private final Converter<Double> converter;

    /**
     * Creates a new instance with the specified name and with 0.0 as the
     * default value.
     *
     * @param name The name of the key.
     */
    public DoubleStyleableKey(String name) {
        this(name, 0.0);
    }


    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public DoubleStyleableKey(String name, double defaultValue) {
        this(name, defaultValue, new DoubleCssConverter(false));
    }


    public DoubleStyleableKey(String name, double defaultValue, CssConverter<Double> converter) {
        super(name, Double.class, defaultValue);

        this.converter = converter;
    }

    @Override
    public Converter<Double> getCssConverter() {
        return converter;
    }
}
