/*
 * @(#)PathMetricsStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.CssPathMetricsConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.TypeToken;
import org.jhotdraw8.geom.shape.PathMetrics;

import java.io.Serial;

/**
 * PathMetricsStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class NullablePathMetricsStyleableKey
        extends AbstractStyleableKey<PathMetrics>
        implements WritableStyleableMapAccessor<PathMetrics>,
        NullableKey<PathMetrics> {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public NullablePathMetricsStyleableKey(@NonNull String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public NullablePathMetricsStyleableKey(@NonNull String name, @Nullable PathMetrics defaultValue) {
        super(name, new TypeToken<PathMetrics>() {
        }, defaultValue);

    }

    private final Converter<PathMetrics> converter = new CssPathMetricsConverter(true);

    @Override
    public @NonNull Converter<PathMetrics> getCssConverter() {
        return converter;
    }

}
