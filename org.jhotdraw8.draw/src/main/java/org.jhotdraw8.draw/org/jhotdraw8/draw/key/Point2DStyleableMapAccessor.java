/*
 * @(#)Point2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.Point2DConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

import java.io.Serial;
import java.util.Map;

/**
 * Point2DStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class Point2DStyleableMapAccessor extends AbstractStyleableMapAccessor<Point2D> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull NonNullMapAccessor<Double> xKey;
    private final @NonNull NonNullMapAccessor<Double> yKey;
    private final Converter<Point2D> converter;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name the name of the accessor
     * @param xKey the key for the x coordinate of the point
     * @param yKey the key for the y coordinate of the point
     */
    public Point2DStyleableMapAccessor(String name, @NonNull NonNullMapAccessor<Double> xKey, @NonNull NonNullMapAccessor<Double> yKey) {
        this(name, xKey, yKey, new Point2DConverter(false));
    }

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the point
     * @param yKey      the key for the y coordinate of the point
     * @param converter String converter for the point
     */
    public Point2DStyleableMapAccessor(String name, @NonNull NonNullMapAccessor<Double> xKey, @NonNull NonNullMapAccessor<Double> yKey, Converter<Point2D> converter) {
        super(name, Point2D.class, new MapAccessor<?>[]{xKey, yKey}, new Point2D(xKey.getDefaultValueNonNull(), yKey.getDefaultValueNonNull()));

        this.converter = converter;
        this.xKey = xKey;
        this.yKey = yKey;
    }

    @Override
    public @NonNull Point2D get(@NonNull Map<? super Key<?>, Object> a) {
        return new Point2D(xKey.getNonNull(a), yKey.getNonNull(a));
    }

    @Override
    public final @NonNull Converter<Point2D> getCssConverter() {
        return converter;
    }

    @Override
    public @NonNull Point2D remove(@NonNull Map<? super Key<?>, Object> a) {
        Point2D oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        return oldValue;
    }

    @Override
    public void set(@NonNull Map<? super Key<?>, Object> a, @Nullable Point2D value) {
        if (value == null) {
            remove(a);
        } else {
            xKey.put(a, value.getX());
            yKey.put(a, value.getY());
        }
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> put(@NonNull ImmutableMap<Key<?>, Object> a, @Nullable Point2D value) {
        if (value == null) {
            return remove(a);
        } else {
            a = xKey.put(a, value.getX());
            return yKey.put(a, value.getY());
        }
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> remove(@NonNull ImmutableMap<Key<?>, Object> a) {
        a = xKey.remove(a);
        return yKey.remove(a);
    }
}
