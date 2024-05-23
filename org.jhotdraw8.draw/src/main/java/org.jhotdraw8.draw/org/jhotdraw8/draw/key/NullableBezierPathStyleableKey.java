/*
 * @(#)NullableBezierPathStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.BezierPathCssConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jspecify.annotations.Nullable;

/**
 * BezierPathStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullableBezierPathStyleableKey
        extends AbstractStyleableKey<BezierPath>
        implements WritableStyleableMapAccessor<BezierPath> {


    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullableBezierPathStyleableKey(String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public NullableBezierPathStyleableKey(String name, @Nullable BezierPath defaultValue) {
        super(name, BezierPath.class, defaultValue);

    }

    private final Converter<BezierPath> converter = new BezierPathCssConverter(false);

    @Override
    public Converter<BezierPath> getCssConverter() {
        return converter;
    }

}
