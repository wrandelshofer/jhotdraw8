/*
 * @(#)Point2DListStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point2D;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.draw.css.converter.Point2DConverter;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

/**
 * Point2DListStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class Point2DListStyleableKey extends AbstractStyleableKey<PersistentList<Point2D>>
        implements WritableStyleableMapAccessor<PersistentList<Point2D>>, NonNullKey<PersistentList<Point2D>> {


    private final Converter<PersistentList<Point2D>> converter;

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public Point2DListStyleableKey(String name) {
        this(name, VectorList.of());
    }

    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public Point2DListStyleableKey(String name, PersistentList<Point2D> defaultValue) {
        super(name, new SimpleParameterizedType(PersistentList.class, Point2D.class), defaultValue);

        this.converter = new ListCssConverter<>(
                new Point2DConverter(false, false));
    }

    @Override
    public Converter<PersistentList<Point2D>> getCssConverter() {
        return converter;
    }

}
