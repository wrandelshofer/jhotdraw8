/*
 * @(#)BoundingBoxStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.BoundingBox;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.BoundingBoxCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * BoundingBoxStyleableMapAccessor.
 *
 */
public class BoundingBoxStyleableMapAccessor extends AbstractStyleableMapAccessor<BoundingBox> {


    private final MapAccessor<Double> xKey;
    private final MapAccessor<Double> yKey;
    private final MapAccessor<Double> widthKey;
    private final MapAccessor<Double> heightKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the rectangle
     * @param yKey      the key for the y coordinate of the rectangle
     * @param widthKey  the key for the width of the rectangle
     * @param heightKey the key for the height of the rectangle
     */
    public BoundingBoxStyleableMapAccessor(String name, MapAccessor<Double> xKey, MapAccessor<Double> yKey, MapAccessor<Double> widthKey, MapAccessor<Double> heightKey) {
        super(name, BoundingBox.class, new MapAccessor<?>[]{xKey, yKey, widthKey, heightKey}, new BoundingBox(xKey.getDefaultValue(), yKey.getDefaultValue(), widthKey.getDefaultValue(), heightKey.getDefaultValue()));

        this.xKey = xKey;
        this.yKey = yKey;
        this.widthKey = widthKey;
        this.heightKey = heightKey;
    }

    private final Converter<BoundingBox> converter = new BoundingBoxCssConverter(false);

    @Override
    public Converter<BoundingBox> getCssConverter() {
        return converter;
    }

    @Override
    public BoundingBox get(Map<? super Key<?>, Object> a) {
        return new BoundingBox(xKey.get(a), yKey.get(a), widthKey.get(a), heightKey.get(a));
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable BoundingBox value) {
        if (value == null) {
            remove(a);
        } else {
            xKey.put(a, value.getMinX());
            yKey.put(a, value.getMinY());
            widthKey.put(a, value.getWidth());
            heightKey.put(a, value.getHeight());
        }
    }

    @Override
    public BoundingBox remove(Map<? super Key<?>, Object> a) {
        BoundingBox oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        widthKey.remove(a);
        heightKey.remove(a);
        return oldValue;
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable BoundingBox value) {
        if (value == null) {
            return remove(a);
        } else {
            a = xKey.put(a, value.getMinX());
            a = yKey.put(a, value.getMinY());
            a = widthKey.put(a, value.getWidth());
            return heightKey.put(a, value.getHeight());
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> remove(PersistentMap<Key<?>, Object> a) {
        a = xKey.remove(a);
        a = yKey.remove(a);
        a = widthKey.remove(a);
        return heightKey.remove(a);
    }
}
