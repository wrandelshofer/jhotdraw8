/*
 * @(#)NullableEnumStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.KebabCaseEnumCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * NullableEnumStyleableKey.
 *
 * @param <T> the value type
 * @author Werner Randelshofer
 */
public class NullableEnumStyleableKey<T extends Enum<T>> extends NullableObjectStyleableKey<T> implements WritableStyleableMapAccessor<T> {


    /**
     * Creates a new instance with the specified name, enum class, mask and with
     * null as the default value.
     *
     * @param name  The name of the key.
     * @param clazz The enum class.
     */
    public NullableEnumStyleableKey(String name, Class<T> clazz) {
        this(name, clazz, null);
    }

    /**
     * Creates a new instance with the specified name, enum class, mask and
     * default value.
     *
     * @param xmlName         The name of the key.
     * @param clazz        The enum class.
     * @param defaultValue The default value.
     */
    public NullableEnumStyleableKey(String xmlName, Class<T> clazz, @Nullable T defaultValue) {
        super(xmlName, clazz, new KebabCaseEnumCssConverter<>(clazz, true), defaultValue);
    }

    /**
     * Creates a new instance with the specified name, enum class, mask and
     * default value.
     *
     * @param xmlName         The XML name of the key.
     * @param cssName      The CSS name of the key.
     * @param clazz        The enum class.
     * @param converter    The CSS converter
     * @param defaultValue The default value.
     */
    public NullableEnumStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Class<T> clazz,
                            @NonNull Converter<T> converter,
                            @Nullable T defaultValue) {
        super(xmlName,cssName, clazz, converter, defaultValue);
    }
}
