/*
 * @(#)Rectangle2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Rectangle2D;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.Rectangle2DConverter;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

import static java.lang.Double.max;

/**
 * Rectangle2DStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class Rectangle2DStyleableMapAccessor extends AbstractStyleableMapAccessor<Rectangle2D>
        implements NonNullMapAccessor<Rectangle2D> {


    private final NonNullMapAccessor<Double> xKey;
    private final NonNullMapAccessor<Double> yKey;
    private final NonNullMapAccessor<Double> widthKey;
    private final NonNullMapAccessor<Double> heightKey;
    private final Converter<Rectangle2D> converter = new Rectangle2DConverter(false);

    /**
     * Creates a new instance with the specified name.
     *
     * @param name      the name of the accessor
     * @param xKey      the key for the x coordinate of the rectangle
     * @param yKey      the key for the y coordinate of the rectangle
     * @param widthKey  the key for the width of the rectangle
     * @param heightKey the key for the height of the rectangle
     */
    public Rectangle2DStyleableMapAccessor(String name,
                                           NonNullMapAccessor<Double> xKey,
                                           NonNullMapAccessor<Double> yKey,
                                           NonNullMapAccessor<Double> widthKey,
                                           NonNullMapAccessor<Double> heightKey) {
        super(name, Rectangle2D.class, new NonNullMapAccessor<?>[]{xKey, yKey, widthKey, heightKey}, new Rectangle2D(xKey.getDefaultValueNonNull(), yKey.getDefaultValueNonNull(),
                widthKey.getDefaultValueNonNull(), heightKey.getDefaultValueNonNull()));

        this.xKey = xKey;
        this.yKey = yKey;
        this.widthKey = widthKey;
        this.heightKey = heightKey;
    }

    @Override
    public Rectangle2D get(Map<? super Key<?>, Object> a) {
        return new Rectangle2D(xKey.getNonNull(a), yKey.getNonNull(a), max(0.0, widthKey.getNonNull(a)), max(0.0, heightKey.getNonNull(a)));
    }

    @Override
    public Converter<Rectangle2D> getCssConverter() {
        return converter;
    }

    @Override
    public Rectangle2D remove(Map<? super Key<?>, Object> a) {
        Rectangle2D oldValue = get(a);
        xKey.remove(a);
        yKey.remove(a);
        widthKey.remove(a);
        heightKey.remove(a);
        return oldValue;
    }

    @Override
    public void set(Map<? super Key<?>, Object> a, @Nullable Rectangle2D value) {
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
    public PersistentMap<Key<?>, Object> put(PersistentMap<Key<?>, Object> a, @Nullable Rectangle2D value) {
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
