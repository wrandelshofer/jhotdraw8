/*
 * @(#)BezierNodeListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.draw.css.converter.CssBezierNodeListConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.geom.shape.BezierNode;

import java.io.Serial;

/**
 * BezierNodeListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class BezierNodeListStyleableKey
        extends AbstractStyleableKey<@NonNull ImmutableList<BezierNode>>
        implements WritableStyleableMapAccessor<@NonNull ImmutableList<BezierNode>>,
        NonNullKey<@NonNull ImmutableList<BezierNode>> {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public BezierNodeListStyleableKey(@NonNull String name) {
        this(name, VectorList.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public BezierNodeListStyleableKey(@NonNull String name, @NonNull ImmutableList<BezierNode> defaultValue) {
        super(name, new TypeToken<ImmutableList<BezierNode>>() {
        }, defaultValue);

    }

    private final Converter<ImmutableList<BezierNode>> converter = new CssBezierNodeListConverter(false);

    @Override
    public @NonNull Converter<ImmutableList<BezierNode>> getCssConverter() {
        return converter;
    }

}
