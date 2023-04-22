/*
 * @(#)DoubleListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.css.converter.CssDoubleConverter;
import org.jhotdraw8.css.converter.CssListConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

import java.io.Serial;

/**
 * DoubleListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class DoubleListStyleableKey extends AbstractStyleableKey<ImmutableList<Double>> implements WritableStyleableMapAccessor<ImmutableList<Double>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Converter<ImmutableList<Double>> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public DoubleListStyleableKey(@NonNull String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public DoubleListStyleableKey(@NonNull String name, ImmutableList<Double> defaultValue) {
        super(name, new TypeToken<ImmutableList<Double>>() {
        }, defaultValue);

        converter = new CssListConverter<>(new CssDoubleConverter(false));
    }

    @Override
    public @NonNull Converter<ImmutableList<Double>> getCssConverter() {
        return converter;
    }

}
