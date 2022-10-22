/*
 * @(#)EnumStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.typesafekey.Key;
import org.jhotdraw8.collection.typesafekey.NonNullKey;
import org.jhotdraw8.collection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.draw.css.text.CssKebabCaseEnumConverter;
import org.jhotdraw8.styleable.WritableStyleableMapAccessor;

/**
 * Convenience class for creating a {@link Key} for an enum type.
 *
 * @author Werner Randelshofer
 */
public class EnumStyleableKey< T extends Enum<T>> extends SimpleStyleableKey< T>
        implements WritableStyleableMapAccessor<T>, NonNullMapAccessor< T>, NonNullKey<T> {

    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name, enum class, mask and
     * default value.
     *
     * @param xmlName         The XML name of the key.
     * @param clazz        The enum class.
     * @param defaultValue The default value.
     */
    public EnumStyleableKey(@NonNull String xmlName, @NonNull Class<T> clazz, @NonNull T defaultValue) {
        super(xmlName, clazz, new CssKebabCaseEnumConverter<>(clazz, false), defaultValue);
    }
    /**
     * Creates a new instance with {@link CssKebabCaseEnumConverter}.
     *
     * @param xmlName         The XML name of the key.
     * @param cssName      The CSS name of the key.
     * @param clazz        The enum class.
     * @param defaultValue The default value.
     */
    public EnumStyleableKey(@NonNull String xmlName,@NonNull String cssName, @NonNull Class<T> clazz, @NonNull T defaultValue) {
        super(xmlName,cssName, clazz, new CssKebabCaseEnumConverter<>(clazz, false), defaultValue);
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
    public EnumStyleableKey(@NonNull String xmlName,@NonNull String cssName, @NonNull Class<T> clazz,
                            @NonNull Converter<T> converter,
                            @NonNull T defaultValue) {
        super(xmlName,cssName, clazz, converter, defaultValue);
    }
}
