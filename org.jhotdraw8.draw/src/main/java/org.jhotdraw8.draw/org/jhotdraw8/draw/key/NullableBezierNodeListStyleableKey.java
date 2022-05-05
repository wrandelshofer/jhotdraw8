/*
 * @(#)NullableBezierNodeListStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.PersistentList;
import org.jhotdraw8.css.text.CssBezierNodeListConverter;
import org.jhotdraw8.geom.BezierNode;
import org.jhotdraw8.reflect.TypeToken;
import org.jhotdraw8.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.text.Converter;

/**
 * BezierNodeListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableBezierNodeListStyleableKey
        extends AbstractStyleableKey<@NonNull PersistentList<BezierNode>>
        implements WritableStyleableMapAccessor<@NonNull PersistentList<BezierNode>> {

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
    public NullableBezierNodeListStyleableKey(@NonNull String name, @Nullable PersistentList<BezierNode> defaultValue) {
        super(name, new TypeToken<PersistentList<BezierNode>>() {
        }, defaultValue);

    }

    private final Converter<PersistentList<BezierNode>> converter = new CssBezierNodeListConverter(false);

    @Override
    public @NonNull Converter<PersistentList<BezierNode>> getCssConverter() {
        return converter;
    }

}
