/*
 * @(#)CssPoint2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.Point2DCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * CssPoint2DStyleableMapAccessor.
 *
 */
public class CssPoint2DStyleableMapAccessor
        extends AbstractStyleableMapAccessor<CssPoint2D>
        implements NonNullMapAccessor<CssPoint2D> {


    private final Converter<CssPoint2D> converter;
    private final NonNullMapAccessor<CssSize> xKey;
    private final NonNullMapAccessor<CssSize> yKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name the name of the accessor
     * @param xKey the key for the x coordinate of the point
     * @param yKey the key for the y coordinate of the point
     */
    public CssPoint2DStyleableMapAccessor(String name, NonNullMapAccessor<CssSize> xKey, NonNullMapAccessor<CssSize> yKey) {
        this(name, xKey, yKey, new Point2DCssConverter(false));
    }

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the point
     * @param yKey      the key for the y coordinate of the point
     * @param converter String converter for the point
     */
    public CssPoint2DStyleableMapAccessor(String name, NonNullMapAccessor<CssSize> xKey, NonNullMapAccessor<CssSize> yKey, Converter<CssPoint2D> converter) {
        super(name, CssPoint2D.class, new NonNullMapAccessor<?>[]{xKey, yKey}, new CssPoint2D(xKey.getDefaultValue(), yKey.getDefaultValue()));

        this.converter = converter;
        this.xKey = xKey;
        this.yKey = yKey;
    }

    @Override
    public CssPoint2D get(Map<? super Key<?>, Object> a) {
        return new CssPoint2D(xKey.get(a), yKey.get(a));
    }


    @Override
    public Converter<CssPoint2D> getCssConverter() {
        return converter;
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable CssPoint2D value) {
        if (value == null) {
            xKey.remove(a);
            yKey.remove(a);
        } else {
            xKey.put(a, value.getX());
            yKey.put(a, value.getY());
        }
    }

    @Override
    public CssPoint2D remove(Map<? super Key<?>, Object> a) {
        CssPoint2D oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        return oldValue;
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable CssPoint2D value) {
        if (value == null) {
            a = xKey.remove(a);
            return yKey.remove(a);
        } else {
            a = xKey.put(a, value.getX());
            return yKey.put(a, value.getY());
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a) {
        a = xKey.remove(a);
        return yKey.remove(a);
    }
}
