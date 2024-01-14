/*
 * @(#)NullableSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.TypeToken;
import org.jhotdraw8.icollection.SimpleImmutableSequencedSet;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;

import java.io.Serial;

/**
 * SetStyleableKey.
 *
 * @param <T> the element type of the set
 * @author Werner Randelshofer
 */
public class NullableSetStyleableKey<T> extends AbstractReadOnlyStyleableKey<ImmutableSequencedSet<T>>
        implements WritableStyleableMapAccessor<ImmutableSequencedSet<T>>,
        NullableKey<ImmutableSequencedSet<T>> {

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
    public NullableSetStyleableKey(@NonNull String name, @NonNull TypeToken<ImmutableSequencedSet<T>> type, @NonNull CssConverter<ImmutableSequencedSet<T>> converter) {
        super(name, type.getType(), converter, SimpleImmutableSequencedSet.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param type         the class of the type
     * @param converter    String converter for a list element
     * @param defaultValue The default value.
     */
    public NullableSetStyleableKey(@NonNull String name, @NonNull TypeToken<ImmutableSequencedSet<T>> type, @NonNull CssConverter<ImmutableSequencedSet<T>> converter, @Nullable ImmutableSequencedSet<T> defaultValue) {
        super(name, type.getType(), converter, defaultValue);
    }
/*
    public SetStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull TypeToken<ImmutableSequencedSet<T>> type, @NonNull CssConverter<T> converter, @NonNull ImmutableSequencedSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new CssSetConverter<>(converter), defaultValue);
    }
    public SetStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull TypeToken<ImmutableSequencedSet<T>> type, @NonNull CssConverter<T> converter,
                           @Nullable String delimiter, @NonNull ImmutableSequencedSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new CssSetConverter<>(converter,delimiter), defaultValue);
    }
*/
}
