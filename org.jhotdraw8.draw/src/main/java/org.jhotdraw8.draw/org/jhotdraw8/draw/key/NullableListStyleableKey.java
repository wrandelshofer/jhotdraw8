/*
 * @(#)NullableListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.io.Serial;
import java.lang.reflect.Type;

/**
 * Nullable NonNullListStyleableKey.
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
     * @param elementType      the class of the type
     * @param elementConverter String converter for a list element
     */
    public NullableListStyleableKey(@NonNull String name, @NonNull Type elementType, @NonNull CssConverter<T> elementConverter) {
        super(name, new SimpleParameterizedType(ImmutableList.class, elementType), new ListCssConverter<>(elementConverter), VectorList.of());
    }
}
