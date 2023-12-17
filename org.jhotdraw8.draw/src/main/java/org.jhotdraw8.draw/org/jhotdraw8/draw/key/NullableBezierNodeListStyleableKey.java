/*
 * @(#)NullableBezierNodeListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.draw.css.converter.CssBezierNodeListConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.io.Serial;

/**
 * BezierNodeListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableBezierNodeListStyleableKey
        extends AbstractStyleableKey<@NonNull ImmutableList<BezierNode>>
        implements WritableStyleableMapAccessor<@NonNull ImmutableList<BezierNode>> {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableBezierNodeListStyleableKey(@NonNull String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public NullableBezierNodeListStyleableKey(@NonNull String name, @Nullable ImmutableList<BezierNode> defaultValue) {
        super(name, new TypeToken<ImmutableList<BezierNode>>() {
        }, defaultValue);

    }

    private final Converter<ImmutableList<BezierNode>> converter = new CssBezierNodeListConverter(false);

    @Override
    public @NonNull Converter<ImmutableList<BezierNode>> getCssConverter() {
        return converter;
    }

}
