/*
 * @(#)NullableSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.SetCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;

import java.lang.reflect.Type;

/**
 * NonNullSetStyleableKey.
 *
 * @param <T> the element type of the set
 * @author Werner Randelshofer
 */
public class NullableSetStyleableKey<T> extends AbstractReadOnlyStyleableKey<ImmutableSequencedSet<T>>
        implements WritableStyleableMapAccessor<ImmutableSequencedSet<T>>,
        NullableKey<ImmutableSequencedSet<T>> {


    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name      The name of the key.
     * @param elementType      the class of the type
     * @param elementConverter String converter for a list element
     */
    public NullableSetStyleableKey(@NonNull String name, @NonNull Type elementType, @NonNull CssConverter<T> elementConverter) {
        super(name, new SimpleParameterizedType(ImmutableSequencedSet.class, elementType), new SetCssConverter<>(elementConverter), ChampVectorSet.of());
    }
}
