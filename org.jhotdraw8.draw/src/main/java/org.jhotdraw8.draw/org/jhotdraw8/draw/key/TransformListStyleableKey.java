/*
 * @(#)TransformListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.scene.transform.Transform;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.draw.css.converter.TransformCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

/**
 * TransformListStyleableKey.
 *
 */
public class TransformListStyleableKey extends AbstractStyleableKey<PersistentList<Transform>>
        implements WritableStyleableMapAccessor<PersistentList<Transform>>, NonNullKey<PersistentList<Transform>> {


    private final Converter<PersistentList<Transform>> converter;

    /**
     * Creates a new instance with the specified name and with an empty list as the
     * default value.
     *
     * @param name The name of the key.
     */
    public TransformListStyleableKey(String name) {
        this(name, VectorList.of());
    }

    /**
     * Creates a new instance with the specified name, and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public TransformListStyleableKey(String name, PersistentList<Transform> defaultValue) {
        super(name, new SimpleParameterizedType(PersistentList.class, Transform.class), defaultValue);
        converter = new ListCssConverter<>(new TransformCssConverter());
    }

    @Override
    public Converter<PersistentList<Transform>> getCssConverter() {
        return converter;
    }

}
