/*
 * @(#)BoundingBoxStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.BoundingBox;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.BoundingBoxCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

import java.util.Map;

/**
 * BoundingBoxStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class BoundingBoxStyleableMapAccessor extends AbstractStyleableMapAccessor<BoundingBox> {


    private final @NonNull MapAccessor<Double> xKey;
    private final @NonNull MapAccessor<Double> yKey;
    private final @NonNull MapAccessor<Double> widthKey;
    private final @NonNull MapAccessor<Double> heightKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the rectangle
     * @param yKey      the key for the y coordinate of the rectangle
     * @param widthKey  the key for the width of the rectangle
     * @param heightKey the key for the height of the rectangle
     */
    public BoundingBoxStyleableMapAccessor(String name, @NonNull MapAccessor<Double> xKey, @NonNull MapAccessor<Double> yKey, @NonNull MapAccessor<Double> widthKey, @NonNull MapAccessor<Double> heightKey) {
        super(name, BoundingBox.class, new MapAccessor<?>[]{xKey, yKey, widthKey, heightKey}, new BoundingBox(xKey.getDefaultValue(), yKey.getDefaultValue(), widthKey.getDefaultValue(), heightKey.getDefaultValue()));

        this.xKey = xKey;
        this.yKey = yKey;
        this.widthKey = widthKey;
        this.heightKey = heightKey;
    }

    private final @NonNull Converter<BoundingBox> converter = new BoundingBoxCssConverter(false);

    @Override
    public @NonNull Converter<BoundingBox> getCssConverter() {
        return converter;
    }

    @Override
    public @NonNull BoundingBox get(@NonNull Map<? super Key<?>, Object> a) {
        return new BoundingBox(xKey.get(a), yKey.get(a), widthKey.get(a), heightKey.get(a));
    }

    @Override
    public void set(@NonNull Map<? super Key<?>, Object> a, @Nullable BoundingBox value) {
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
    public @NonNull BoundingBox remove(@NonNull Map<? super Key<?>, Object> a) {
        BoundingBox oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        widthKey.remove(a);
        heightKey.remove(a);
        return oldValue;
    }

    @Override
    public @NonNull ImmutableMap<Key<?>, Object> put(@NonNull ImmutableMap<Key<?>, Object> a, @Nullable BoundingBox value) {
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
    public @NonNull ImmutableMap<Key<?>, Object> remove(@NonNull ImmutableMap<Key<?>, Object> a) {
        a = xKey.remove(a);
        a = yKey.remove(a);
        a = widthKey.remove(a);
        return heightKey.remove(a);
    }
}
