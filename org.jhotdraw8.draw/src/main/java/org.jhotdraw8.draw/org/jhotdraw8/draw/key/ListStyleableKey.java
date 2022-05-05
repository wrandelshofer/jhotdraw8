/*
 * @(#)ListStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableArrayList;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.key.NonNullMapAccessor;
import org.jhotdraw8.css.text.CssConverter;
import org.jhotdraw8.css.text.CssListConverter;
import org.jhotdraw8.reflect.TypeToken;
import org.jhotdraw8.styleable.WritableStyleableMapAccessor;

import java.lang.reflect.Type;

/**
 * TListStyleableFigureKey.
 *
 * @author Werner Randelshofer
 */
public class ListStyleableKey<T> extends AbstractReadOnlyStyleableKey<ImmutableList<T>>
        implements WritableStyleableMapAccessor<ImmutableList<T>>,
        NonNullMapAccessor<ImmutableList<T>> {

    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param type      the class of the type
     * @param converter String converter for a list element
     */
    public ListStyleableKey(@NonNull String name, @NonNull Type type, @NonNull CssConverter<ImmutableList<T>> converter) {
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
    public ListStyleableKey(@NonNull String name, @NonNull TypeToken<ImmutableList<T>> type, @NonNull CssConverter<T> converter) {
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
    public ListStyleableKey(@NonNull String name, @NonNull TypeToken<ImmutableList<T>> type, @NonNull CssConverter<T> converter, @NonNull ImmutableList<T> defaultValue) {
        super(name, type.getType(), new CssListConverter<>(converter), defaultValue);
    }

    public ListStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull TypeToken<ImmutableList<T>> type, @NonNull CssConverter<T> converter, @NonNull ImmutableList<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new CssListConverter<>(converter), defaultValue);
    }

}
