/*
 * @(#)ListStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.CssListConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;

import java.lang.reflect.Type;

/**
 * ListStyleableKey.
 *
 * @param <T> the element type of the list
 * @author Werner Randelshofer
 */
public class NullableListStyleableKey<T> extends AbstractReadOnlyStyleableKey<ImmutableList<T>>
        implements WritableStyleableMapAccessor<ImmutableList<T>>, NullableKey<ImmutableList<T>> {

    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param type      the class of the type
     * @param converter String converter for a list element
     */
    public NullableListStyleableKey(@NonNull String name, @NonNull Type type, @NonNull CssConverter<ImmutableList<T>> converter) {
        super(name, type, converter, ImmutableArrayList.of());
    }

    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param type      the class of the type
     * @param converter String converter for a list element
     */
    public NullableListStyleableKey(@NonNull String name, @NonNull TypeToken<ImmutableList<T>> type, @NonNull CssConverter<T> converter) {
        super(name, type.getType(), new CssListConverter<>(converter), ImmutableArrayList.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param type         the class of the type
     * @param converter    String converter for a list element
     * @param defaultValue The default value.
     */
    public NullableListStyleableKey(@NonNull String name, @NonNull TypeToken<ImmutableList<T>> type, @NonNull CssConverter<T> converter, @Nullable ImmutableList<T> defaultValue) {
        super(name, type.getType(), new CssListConverter<>(converter), defaultValue);
    }

    public NullableListStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull TypeToken<ImmutableList<T>> type, @NonNull CssConverter<T> converter, @Nullable ImmutableList<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new CssListConverter<>(converter), defaultValue);
    }

    public NullableListStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull TypeToken<ImmutableList<T>> type, @NonNull CssConverter<T> converter,
                                    @Nullable String delimiter, @Nullable ImmutableList<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new CssListConverter<>(converter, delimiter), defaultValue);
    }

}