/*
 * @(#)SimpleStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.fxbase.styleable.ReadOnlyStyleableMapAccessor;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

import java.lang.reflect.Type;

/**
 * SimpleStyleableKey.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class SimpleStyleableKey<T> extends AbstractReadOnlyStyleableKey<T>
        implements WritableStyleableMapAccessor<T> {

    private static final long serialVersionUID = 1L;


   /**
     * Creates a new instance.
     *
     * @param name          The XML name of the key. The CSS name is
     *                      generated using {@link ReadOnlyStyleableMapAccessor#toCssName(String)}.
     * @param type         The type of the value.
     * @param converter    the converter
     * @param defaultValue The default value.
     */
    public SimpleStyleableKey(@NonNull String name, @NonNull Type type, @NonNull Converter<T> converter, T defaultValue) {
        super(name,ReadOnlyStyleableMapAccessor.toCssName(name), type, converter, defaultValue);
    }

    /**
     * Creates a new instance.
     *
     * @param xmlName          The XML name of the key.
     * @param cssName          The CSS name of the key.
     * @param type         The type of the value.
     * @param converter    the converter
     * @param defaultValue The default value.
     */
    public SimpleStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Type type, @NonNull Converter<T> converter, T defaultValue) {
        super(xmlName, cssName, type, converter, defaultValue);
    }


}
