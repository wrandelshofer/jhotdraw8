/*
 * @(#)NullableCssRectangle2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssRectangle2D;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.Rectangle2DCssConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Rectangle2DStyleableMapAccessor.
 *
 */
public class NullableCssRectangle2DStyleableMapAccessor extends AbstractStyleableMapAccessor<@Nullable CssRectangle2D>
        implements MapAccessor<CssRectangle2D> {


    private final MapAccessor<CssSize> xKey;
    private final MapAccessor<CssSize> yKey;
    private final MapAccessor<CssSize> widthKey;
    private final MapAccessor<CssSize> heightKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the rectangle
     * @param yKey      the key for the y coordinate of the rectangle
     * @param widthKey  the key for the width of the rectangle
     * @param heightKey the key for the height of the rectangle
     */
    public NullableCssRectangle2DStyleableMapAccessor(String name, MapAccessor<CssSize> xKey, MapAccessor<CssSize> yKey, MapAccessor<CssSize> widthKey, MapAccessor<CssSize> heightKey) {
        super(name, CssRectangle2D.class, new MapAccessor<?>[]{xKey, yKey, widthKey, heightKey}, null);

        this.xKey = xKey;
        this.yKey = yKey;
        this.widthKey = widthKey;
        this.heightKey = heightKey;
    }

    private final Converter<CssRectangle2D> converter = new Rectangle2DCssConverter(true);

    @Override
    public Converter<CssRectangle2D> getCssConverter() {
        return converter;
    }

    @Override
    public @Nullable CssRectangle2D get(Map<? super Key<?>, Object> a) {
        final CssSize x = xKey.get(a);
        final CssSize y = yKey.get(a);
        final CssSize width = widthKey.get(a);
        final CssSize height = heightKey.get(a);
        return (x == null || y == null || width == null || height == null)
                ? null
                : new CssRectangle2D(x, y, width, height);
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable CssRectangle2D value) {
        if (value == null) {
            xKey.put(a, null);
            yKey.put(a, null);
            widthKey.put(a, null);
            heightKey.put(a, null);
        } else {
            xKey.put(a, value.getMinX());
            yKey.put(a, value.getMinY());
            widthKey.put(a, value.getWidth());
            heightKey.put(a, value.getHeight());
        }
    }

    @Override
    public CssRectangle2D remove(Map<? super Key<?>, Object> a) {
        CssRectangle2D oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        widthKey.remove(a);
        heightKey.remove(a);
        return oldValue;
    }

    @Override
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable CssRectangle2D value) {
        if (value == null) {
            a = xKey.put(a, null);
            a = yKey.put(a, null);
            a = widthKey.put(a, null);
            return heightKey.put(a, null);
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
