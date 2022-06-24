/*
 * @(#)SimpleNonNullStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.text.Converter;

import java.lang.reflect.Type;

/**
 * CssSizeStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class SimpleNonNullStyleableKey<T> extends AbstractReadOnlyStyleableKey< T> implements WritableStyleableMapAccessor<@NonNull T>,
        NonNullMapAccessor< T> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     *
     * @param name          The name of the key
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public SimpleNonNullStyleableKey(@NonNull String name, @NonNull Type type, @NonNull Converter<T> converter, @NonNull T defaultValue) {
        super(name, type, converter, defaultValue);
    }

    /**
     * Creates a new instance.
     *
     * @param xmlName          The XML name of the key.
     * @param cssName          The CSS name of the key.
     * @param type         The type of the value.
     * @param converter    the CSS converter
     * @param defaultValue The default value.
     */
    public SimpleNonNullStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Type type, @NonNull Converter<T> converter, @NonNull T defaultValue) {
        super(xmlName, cssName, type, converter, defaultValue);
    }


    @Override
    public @NonNull Converter<T> getCssConverter() {
        return converter;
    }

}
