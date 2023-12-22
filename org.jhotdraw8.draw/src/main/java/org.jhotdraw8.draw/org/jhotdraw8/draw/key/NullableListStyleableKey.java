/*
 * @(#)NullableListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.CssListConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.TypeToken;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.io.Serial;
import java.lang.reflect.Type;

/**
 * ListStyleableKey.
 *
 * @param <T> the element type of the list
 * @author Werner Randelshofer
 */
public class NullableListStyleableKey<T> extends AbstractReadOnlyStyleableKey<ImmutableList<T>>
        implements WritableStyleableMapAccessor<ImmutableList<T>>, NullableKey<ImmutableList<T>> {

    @Serial
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
        super(name, type, converter, VectorList.of());
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
        super(name, type.getType(), new CssListConverter<>(converter), VectorList.of());
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
