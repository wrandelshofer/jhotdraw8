/*
 * @(#)NonNullObjectStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

import java.io.Serial;
import java.lang.reflect.Type;

/**
 * A simple non-nullable StyleableKey.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class NonNullObjectStyleableKey<T> extends AbstractReadOnlyStyleableKey<T> implements WritableStyleableMapAccessor<@NonNull T>,
        NonNullKey<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     *
     * @param name         The name of the key
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public NonNullObjectStyleableKey(@NonNull String name, @NonNull Type type, @NonNull Converter<T> converter, @NonNull T defaultValue) {
        super(name, type, converter, defaultValue);
    }

    /**
     * Creates a new instance.
     *
     * @param xmlName      The XML name of the key.
     * @param cssName      The CSS name of the key.
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public NonNullObjectStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Type type, @NonNull Converter<T> converter, @NonNull T defaultValue) {
        super(xmlName, cssName, type, converter, defaultValue);
    }

}
