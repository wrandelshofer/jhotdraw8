/*
 * @(#)NonNullSetStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.icollection.ChampVectorSet;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedSet;

import java.io.Serial;
import java.lang.reflect.Type;

/**
 * NonNullSetStyleableKey.
 *
 * @param <T> the element type of the set
 * @author Werner Randelshofer
 */
public class NonNullSetStyleableKey<T> extends AbstractReadOnlyStyleableKey<ImmutableSequencedSet<T>>
        implements WritableStyleableMapAccessor<ImmutableSequencedSet<T>>,
        NonNullKey<ImmutableSequencedSet<T>> {

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
    public NonNullSetStyleableKey(@NonNull String name, @NonNull Type type, @NonNull CssConverter<ImmutableSequencedSet<T>> converter) {
        super(name, type, converter, ChampVectorSet.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param type         the class of the type
     * @param converter    String converter for a list element
     * @param defaultValue The default value.
     */
    public NonNullSetStyleableKey(@NonNull String name, @NonNull Type type, @NonNull CssConverter<ImmutableSequencedSet<T>> converter, @NonNull ImmutableSequencedSet<T> defaultValue) {
        super(name, type, converter, defaultValue);
    }
/*
    public NonNullSetStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull TypeToken<ImmutableSequencedSet<T>> type, @NonNull CssConverter<T> converter, @NonNull ImmutableSequencedSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new XmlSetConverter<>(converter), defaultValue);
    }
    public NonNullSetStyleableKey(@NonNull String xmlName, @NonNull String cssName, @NonNull TypeToken<ImmutableSequencedSet<T>> type, @NonNull CssConverter<T> converter,
                           @Nullable String delimiter, @NonNull ImmutableSequencedSet<T> defaultValue) {
        super(xmlName, cssName, type.getType(), new XmlSetConverter<>(converter,delimiter), defaultValue);
    }
*/
}
