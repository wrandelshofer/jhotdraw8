/*
 * @(#)Scale2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point2D;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.Scale2DCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Scale2DStyleableMapAccessor.
 *
 */
public class Scale2DStyleableMapAccessor extends AbstractStyleableMapAccessor<Point2D> {


    private final MapAccessor<Double> xKey;
    private final MapAccessor<Double> yKey;
    private final Converter<Point2D> converter = new Scale2DCssConverter();

    /**
     * Creates a new instance with the specified name.
     *
     * @param name the name of the accessor
     * @param xKey the key for the x coordinate of the point
     * @param yKey the key for the y coordinate of the point
     */
    public Scale2DStyleableMapAccessor(String name, MapAccessor<Double> xKey, MapAccessor<Double> yKey) {
        super(name, Point2D.class, new MapAccessor<?>[]{xKey, yKey}, new Point2D(xKey.getDefaultValue(), yKey.getDefaultValue()));

        this.xKey = xKey;
        this.yKey = yKey;
    }

    @Override
    public Point2D get(Map<? super Key<?>, Object> a) {

        Double x = xKey.get(a);
        Double y = yKey.get(a);
        return new Point2D(x == null ? 0 : x, y == null ? 0 : y);
    }

    @Override
    public Converter<Point2D> getCssConverter() {
        return converter;
    }

    @Override
    public Point2D remove(Map<? super Key<?>, Object> a) {
        Point2D oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        return oldValue;
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable Point2D value) {
        if (value == null) {
            remove(a);
        } else {
            xKey.put(a, value.getX());
            yKey.put(a, value.getY());
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable Point2D value) {
        if (value == null) {
            return remove(a);
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
