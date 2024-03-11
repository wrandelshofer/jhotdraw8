/*
 * @(#)NonNullEnumStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.KebabCaseEnumCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * Convenience class for creating a {@link Key} for an enum type.
 *
 * @param <T> the enum type
 * @author Werner Randelshofer
 */
public class NonNullEnumStyleableKey<T extends Enum<T>> extends NonNullObjectStyleableKey<T>
        implements WritableStyleableMapAccessor<T>, NonNullKey<T> {


    /**
     * Creates a new instance with the specified name, enum class, mask and
     * default value.
     *
     * @param xmlName         The XML name of the key.
     * @param clazz        The enum class.
     * @param defaultValue The default value.
     */
    public NonNullEnumStyleableKey(@NonNull String xmlName, @NonNull Class<T> clazz, @NonNull T defaultValue) {
        super(xmlName, clazz, new KebabCaseEnumCssConverter<>(clazz, false), defaultValue);
    }
    /**
     * Creates a new instance with {@link KebabCaseEnumCssConverter}.
     *
     * @param xmlName         The XML name of the key.
     * @param cssName      The CSS name of the key.
     * @param clazz        The enum class.
     * @param defaultValue The default value.
     */
    public NonNullEnumStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Class<T> clazz, @NonNull T defaultValue) {
        super(xmlName, cssName, clazz, new KebabCaseEnumCssConverter<>(clazz, false), defaultValue);
    }
    /**
     * Creates a new instance.
     *
     * @param xmlName         The XML name of the key.
     * @param cssName      The CSS name of the key.
     * @param clazz        The enum class.
     * @param converter    The CSS converter
     * @param defaultValue The default value.
     */
    public NonNullEnumStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Class<T> clazz,
                                   @NonNull Converter<T> converter,
                                   @NonNull T defaultValue) {
        super(xmlName,cssName, clazz, converter, defaultValue);
    }
}
