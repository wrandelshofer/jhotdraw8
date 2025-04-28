/*
 * @(#)Scale3DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point3D;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.Scale3DCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Scale3DStyleableMapAccessor.
 */
public class Scale3DStyleableMapAccessor extends AbstractStyleableMapAccessor<Point3D>
        implements NonNullMapAccessor<Point3D> {


    private final MapAccessor<Double> xKey;
    private final MapAccessor<Double> yKey;
    private final MapAccessor<Double> zKey;
    private final Converter<Point3D> converter;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name the name of the accessor
     * @param xKey the key for the x coordinate of the point
     * @param yKey the key for the y coordinate of the point
     * @param zKey the key for the u coordinate of the point
     */
    public Scale3DStyleableMapAccessor(String name, MapAccessor<Double> xKey, MapAccessor<Double> yKey, MapAccessor<Double> zKey) {
        this(name, xKey, yKey, zKey, new Scale3DCssConverter(false));
    }

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the point
     * @param yKey      the key for the y coordinate of the point
     * @param zKey      the key for the u coordinate of the point
     * @param converter String converter for the scale factor with 3 coordinates (x-factor, y-factor, z-factor).
     */
    public Scale3DStyleableMapAccessor(String name, MapAccessor<Double> xKey, MapAccessor<Double> yKey, MapAccessor<Double> zKey, Converter<Point3D> converter) {
        super(name, Point3D.class, new MapAccessor<?>[]{xKey, yKey, zKey}, new Point3D(xKey.getDefaultValue(), yKey.getDefaultValue(), zKey.getDefaultValue()));
        this.converter = converter;

        this.xKey = xKey;
        this.yKey = yKey;
        this.zKey = zKey;
    }

    @Override
    public Point3D get(Map<? super Key<?>, Object> a) {
        Double x = xKey.get(a);
        Double y = yKey.get(a);
        Double z = zKey.get(a);
        return new Point3D(x == null ? 0 : x, y == null ? 0 : y, z == null ? 0 : z);
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable Point3D value) {
        if (value == null) {
            return remove(a);
        } else {
            a = xKey.put(a, value.getX());
            a = yKey.put(a, value.getY());
            return zKey.put(a, value.getZ());
        }
    }

    @Override
    public Converter<Point3D> getCssConverter() {
        return converter;
    }

    @Override
    public Point3D remove(Map<? super Key<?>, Object> a) {
        Point3D oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        zKey.remove(a);
        return oldValue;
    }

    @Override
    public PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a) {
        a = xKey.remove(a);
        a = yKey.remove(a);
        return zKey.remove(a);
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable Point3D value) {
        if (value == null) {
            remove(a);
        } else {
            xKey.put(a, value.getX());
            yKey.put(a, value.getY());
            zKey.put(a, value.getZ());
        }
    }

    @Override
    public PersistentList<String> getExamples() {
        return VectorList.of(
                converter.toString(new Point3D(0.5, 0.5, 1)),
                converter.toString(new Point3D(0.5, 1, 1)),
                converter.toString(new Point3D(1, 0.5, 1)),
                converter.toString(new Point3D(1, 1, 1)),
                converter.toString(new Point3D(1, 2, 1)),
                converter.toString(new Point3D(2, 1, 1)),
                converter.toString(new Point3D(2, 2, 1))
        );
    }
}
