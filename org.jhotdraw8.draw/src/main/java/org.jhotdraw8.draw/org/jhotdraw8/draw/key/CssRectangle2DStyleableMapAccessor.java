/*
 * @(#)CssRectangle2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.Rectangle2DCssConverter;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Rectangle2DStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class CssRectangle2DStyleableMapAccessor extends AbstractStyleableMapAccessor<CssRectangle2D>
        implements NonNullMapAccessor<CssRectangle2D> {


    private final NonNullMapAccessor<CssSize> xKey;
    private final NonNullMapAccessor<CssSize> yKey;
    private final NonNullMapAccessor<CssSize> widthKey;
    private final NonNullMapAccessor<CssSize> heightKey;

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the rectangle
     * @param yKey      the key for the y coordinate of the rectangle
     * @param widthKey  the key for the width of the rectangle
     * @param heightKey the key for the height of the rectangle
     */
    public CssRectangle2DStyleableMapAccessor(String name, NonNullMapAccessor<CssSize> xKey, NonNullMapAccessor<CssSize> yKey, NonNullMapAccessor<CssSize> widthKey, NonNullMapAccessor<CssSize> heightKey) {
        super(name, CssRectangle2D.class, new NonNullMapAccessor<?>[]{xKey, yKey, widthKey, heightKey}, new CssRectangle2D(
                xKey.getDefaultValueNonNull(),
                yKey.getDefaultValueNonNull(),
                widthKey.getDefaultValueNonNull(),
                heightKey.getDefaultValueNonNull()));

        this.xKey = xKey;
        this.yKey = yKey;
        this.widthKey = widthKey;
        this.heightKey = heightKey;
    }

    private final Converter<CssRectangle2D> converter = new Rectangle2DCssConverter(false);

    @Override
    public Converter<CssRectangle2D> getCssConverter() {
        return converter;
    }

    @Override
    public CssRectangle2D get(Map<? super Key<?>, Object> a) {
        return new CssRectangle2D(xKey.get(a),
                yKey.get(a),
                widthKey.get(a),
                heightKey.get(a));
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable CssRectangle2D value) {
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
    public CssRectangle2D remove(Map<? super Key<?>, Object> a) {
        CssRectangle2D oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        widthKey.remove(a);
        heightKey.remove(a);
        return oldValue;
    }

    @Override
    public ImmutableMap<Key<?>, Object> put(ImmutableMap<Key<?>, Object> a, @Nullable CssRectangle2D value) {
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
    public ImmutableMap<Key<?>, Object> remove(ImmutableMap<Key<?>, Object> a) {
        a = xKey.remove(a);
        a = yKey.remove(a);
        a = widthKey.remove(a);
        return heightKey.remove(a);
    }
}
