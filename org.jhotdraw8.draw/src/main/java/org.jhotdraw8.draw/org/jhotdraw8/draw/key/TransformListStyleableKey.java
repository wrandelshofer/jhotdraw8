/*
 * @(#)TransformListStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.PersistentArrayList;
import org.jhotdraw8.collection.PersistentList;
import org.jhotdraw8.collection.key.NonNullMapAccessor;
import org.jhotdraw8.css.text.CssListConverter;
import org.jhotdraw8.css.text.CssTransformConverter;
import org.jhotdraw8.reflect.TypeToken;
import org.jhotdraw8.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.text.Converter;

/**
 * TransformListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class TransformListStyleableKey extends AbstractStyleableKey<PersistentList<Transform>>
        implements WritableStyleableMapAccessor<PersistentList<Transform>>, NonNullMapAccessor<PersistentList<Transform>> {

    private static final long serialVersionUID = 1L;

    private Converter<PersistentList<Transform>> converter;

    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name The name of the key.
     */
    public TransformListStyleableKey(@NonNull String name) {
        this(name, PersistentArrayList.of());
    }

    /**
     * Creates a new instance with the specified name, and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public TransformListStyleableKey(@NonNull String name, PersistentList<Transform> defaultValue) {
        super(name, new TypeToken<PersistentList<Transform>>() {
        }, defaultValue);
        converter = new CssListConverter<>(new CssTransformConverter());
    }

    @Override
    public @NonNull Converter<PersistentList<Transform>> getCssConverter() {
        return converter;
    }

}
