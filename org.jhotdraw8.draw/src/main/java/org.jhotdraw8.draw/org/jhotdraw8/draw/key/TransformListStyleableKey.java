/*
 * @(#)TransformListStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.collection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.draw.css.text.CssListConverter;
import org.jhotdraw8.draw.css.text.CssTransformConverter;
import org.jhotdraw8.styleable.WritableStyleableMapAccessor;

/**
 * TransformListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class TransformListStyleableKey extends AbstractStyleableKey<ImmutableList<Transform>>
        implements WritableStyleableMapAccessor<ImmutableList<Transform>>, NonNullMapAccessor<ImmutableList<Transform>> {

    private static final long serialVersionUID = 1L;

    private final Converter<ImmutableList<Transform>> converter;

    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name The name of the key.
     */
    public TransformListStyleableKey(@NonNull String name) {
        this(name, ImmutableArrayList.of());
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
