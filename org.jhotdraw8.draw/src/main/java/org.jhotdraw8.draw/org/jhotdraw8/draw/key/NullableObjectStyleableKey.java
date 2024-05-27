/*
 * @(#)NonNullObjectStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * A simple nullable StyleableKey.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class NullableObjectStyleableKey<T> extends AbstractReadOnlyStyleableKey<T> implements WritableStyleableMapAccessor<T> {


    /**
     * Creates a new instance with a null default value.
     *
     * @param name      The name of the key
     * @param type      The type of the value.
     * @param converter the CSS converter
     */
    public NullableObjectStyleableKey(String name, Type type, Converter<T> converter) {
        super(name, type, converter, null);
    }

    /**
     * Creates a new instance.
     *
     * @param name         The name of the key
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public NullableObjectStyleableKey(String name, Type type, Converter<T> converter, @Nullable T defaultValue) {
        super(name, type, converter, defaultValue);
    }

    /**
     * Creates a new instance.
     *
     * @param name      The name of the key.
     * @param cssName      The CSS name of the key.
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public NullableObjectStyleableKey(String name, String cssName, Type type, Converter<T> converter, @Nullable T defaultValue) {
        super(name, cssName, type, converter, defaultValue);
    }


}
