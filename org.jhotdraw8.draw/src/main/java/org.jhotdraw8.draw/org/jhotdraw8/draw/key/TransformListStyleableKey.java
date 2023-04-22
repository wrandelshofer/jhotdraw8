/*
 * @(#)TransformListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.collection.vector.VectorList;
import org.jhotdraw8.css.converter.CssListConverter;
import org.jhotdraw8.draw.css.converter.CssTransformConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

import java.io.Serial;

/**
 * TransformListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class TransformListStyleableKey extends AbstractStyleableKey<ImmutableList<Transform>>
        implements WritableStyleableMapAccessor<ImmutableList<Transform>>, NonNullKey<ImmutableList<Transform>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Converter<ImmutableList<Transform>> converter;

    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name The name of the key.
     */
    public TransformListStyleableKey(@NonNull String name) {
        this(name, VectorList.of());
    }

    /**
     * Creates a new instance with the specified name, and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public TransformListStyleableKey(@NonNull String name, ImmutableList<Transform> defaultValue) {
        super(name, new TypeToken<ImmutableList<Transform>>() {
        }, defaultValue);
        converter = new CssListConverter<>(new CssTransformConverter());
    }

    @Override
    public @NonNull Converter<ImmutableList<Transform>> getCssConverter() {
        return converter;
    }

}
