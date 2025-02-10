/*
 * @(#)SvgDefaultablePaintStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.key.AbstractStyleableKey;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.svg.css.SvgDefaultablePaint;
import org.jhotdraw8.svg.css.text.SvgDefaultablePaintConverter;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * TListStyleableFigureKey.
 *
 */
public class SvgDefaultablePaintStyleableKey<T extends Paintable> extends AbstractStyleableKey<SvgDefaultablePaint<T>>
        implements WritableStyleableMapAccessor<SvgDefaultablePaint<T>>,
        SvgDefaultablePaintStyleableMapAccessor<T> {


    private final Converter<SvgDefaultablePaint<T>> converter;
    private final @Nullable T initialValue;


    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name                   The name of the key.
     * @param type                   The full type
     * @param converter              String converter for a list element
     * @param initialDefaultingValue The default value.
     */
    public SvgDefaultablePaintStyleableKey(String name, Type type, CssConverter<T> converter,
                                           SvgDefaultablePaint<T> initialDefaultingValue,
                                           @Nullable T initialValue) {
        super(name, type, initialDefaultingValue);
        this.initialValue = initialValue;

        this.converter = new SvgDefaultablePaintConverter<>(converter);
    }

    @Override
    public Converter<SvgDefaultablePaint<T>> getCssConverter() {
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
     * <del>"The initial value of an attribute or property is
     * the value used when that attribute or property is not
     * specified, or when it has an invalid value."</del>
     * <p>
     * References:
     * <dl>
     *     <dt>CSS Cascading and Inheritance Level 4, Chapter 7.1 Initial Values</dt>
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
