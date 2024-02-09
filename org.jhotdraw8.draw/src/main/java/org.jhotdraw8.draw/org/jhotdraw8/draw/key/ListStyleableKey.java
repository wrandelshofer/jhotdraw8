/*
 * @(#)ListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.CssListConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
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
public class ListStyleableKey<T> extends AbstractReadOnlyStyleableKey<ImmutableList<T>>
        implements WritableStyleableMapAccessor<ImmutableList<T>>,
        NonNullKey<ImmutableList<T>> {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param elementType      the class of the type
     * @param elementConverter String converter for a list element
     */
    public ListStyleableKey(@NonNull String name, @NonNull Type elementType, @NonNull CssConverter<T> elementConverter) {
        super(name, new SimpleParameterizedType(ImmutableList.class, elementType), new CssListConverter<>(elementConverter), VectorList.of());
    }


    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param elementType         the class of the type
     * @param elementConverter    String converter for a list element
     * @param defaultValue The default value.
     */
    public ListStyleableKey(@NonNull String name, @NonNull Type elementType, @NonNull CssConverter<T> elementConverter, @NonNull ImmutableList<T> defaultValue) {
        super(name, new SimpleParameterizedType(ImmutableList.class, elementType), new CssListConverter<>(elementConverter), defaultValue);
    }

    public ListStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Type elementType, @NonNull CssConverter<T> elementConverter, @NonNull ImmutableList<T> defaultValue) {
        super(xmlName, cssName, new SimpleParameterizedType(ImmutableList.class, elementType), new CssListConverter<>(elementConverter), defaultValue);
    }

    public ListStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Type elementType, @NonNull CssConverter<T> elementConverter,
                            @Nullable String delimiter, @NonNull ImmutableList<T> defaultValue) {
        super(xmlName, cssName, new SimpleParameterizedType(ImmutableList.class, elementType), new CssListConverter<>(elementConverter, delimiter), defaultValue);
    }

    public ListStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull Type elementType, @NonNull CssConverter<T> elementConverter,
                            @Nullable String delimiter, @Nullable String prefix, @Nullable String suffix, @NonNull ImmutableList<T> defaultValue) {
        super(xmlName, cssName, new SimpleParameterizedType(ImmutableList.class, elementType), new CssListConverter<>(elementConverter, delimiter, prefix, suffix), defaultValue);
    }

}
