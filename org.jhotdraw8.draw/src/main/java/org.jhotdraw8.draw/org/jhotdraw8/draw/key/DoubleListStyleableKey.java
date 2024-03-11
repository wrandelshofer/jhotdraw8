/*
 * @(#)DoubleListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.immutable.ImmutableList;

/**
 * DoubleListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class DoubleListStyleableKey extends AbstractStyleableKey<ImmutableList<Double>> implements WritableStyleableMapAccessor<ImmutableList<Double>> {


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
        super(name, new SimpleParameterizedType(ImmutableList.class, Double.class), defaultValue);

        converter = new ListCssConverter<>(new DoubleCssConverter(false));
    }

    @Override
    public @NonNull Converter<ImmutableList<Double>> getCssConverter() {
        return converter;
    }

}
