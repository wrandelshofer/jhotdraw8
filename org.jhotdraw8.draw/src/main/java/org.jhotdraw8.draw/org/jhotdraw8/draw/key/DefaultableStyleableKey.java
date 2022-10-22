/*
 * @(#)DefaultableStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.draw.css.converter.CssDefaultableValueConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * TListStyleableFigureKey.
 *
 * @author Werner Randelshofer
 */
public class DefaultableStyleableKey<T> extends AbstractStyleableKey<@NonNull CssDefaultableValue<T>>
        implements WritableStyleableMapAccessor<@NonNull CssDefaultableValue<T>>,
        DefaultableStyleableMapAccessor<T> {

    private static final long serialVersionUID = 1L;

    private final @NonNull Converter<@NonNull CssDefaultableValue<T>> converter;
    private final T initialValue;


    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name                   The name of the key.
     * @param type                   The full type
     * @param converter              String converter for a list element
     * @param initialDefaultingValue The default value.
     */
    public DefaultableStyleableKey(@NonNull String name, @NonNull TypeToken<CssDefaultableValue<T>> type, @NonNull CssConverter<T> converter,
                                   @NonNull CssDefaultableValue<T> initialDefaultingValue,
                                   @Nullable T initialValue) {
        super(name, type, initialDefaultingValue);
        this.initialValue = initialValue;

        this.converter = new CssDefaultableValueConverter<>(converter);
    }

    @Override
    public @NonNull Converter<CssDefaultableValue<T>> getCssConverter() {
        return converter;
    }

    /**
     * Returns the initial value of the attribute.
     * <p>
     * We use the definition from CSS initial value:
     * <p>
     * "Each property has an initial value, defined in
     * the property's definition table. If the property
     * is not an inherited property, and the cascade does not
     * result in a value, then the specified value of the
     * property is its initial value."
     * <p>
     * We intentionally do <b>not</b> use the definition from SVG
     * initial value:
     * <p>
     * <s>"The initial value of an attribute or property is
     * the value used when that attribute or property is not
     * specified, or when it has an invalid value."</s>
     * <p>
     * References:
     * <dl>
     *     <dt>CSS Cascading and Inheritance Level 4, Chapter 7.1 Initial Values></dt>
     *     <dd><a href="https://www.w3.org/TR/css-cascade-4/#initial-values">w3.org</a></dd>
     *
     *     <dt>SVG, Chapter 4: Basic Data Types and Interfaces, 4.1 Definitions, Initial Value</dt>
     *     <dd><a href="https://www.w3.org/TR/SVG/types.html#definitions">w3.org</a></dd>
     * </dl>
     *
     * @return the initial value.
     */
    @Override
    public T getInitialValue() {
        return initialValue;
    }
}
