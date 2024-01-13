/*
 * @(#)BezierPathStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.CssBezierPathConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.TypeToken;
import org.jhotdraw8.geom.shape.BezierPath;

import java.io.Serial;

/**
 * BezierPathStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class BezierPathStyleableKey
        extends AbstractStyleableKey<BezierPath>
        implements WritableStyleableMapAccessor<BezierPath>,
        NonNullKey<BezierPath> {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public BezierPathStyleableKey(@NonNull String name) {
        this(name, BezierPath.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public BezierPathStyleableKey(@NonNull String name, @NonNull BezierPath defaultValue) {
        super(name, new TypeToken<BezierPath>() {
        }, defaultValue);

    }

    private final Converter<BezierPath> converter = new CssBezierPathConverter(false);

    @Override
    public @NonNull Converter<BezierPath> getCssConverter() {
        return converter;
    }

}
