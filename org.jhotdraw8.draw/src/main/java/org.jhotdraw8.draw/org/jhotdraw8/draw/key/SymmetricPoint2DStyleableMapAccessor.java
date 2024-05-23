/*
 * @(#)SymmetricPoint2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point2D;
import org.jhotdraw8.draw.css.converter.SymmetricPoint2DConverter;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

/**
 * SymmetricPoint2DStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class SymmetricPoint2DStyleableMapAccessor extends Point2DStyleableMapAccessor
        implements NonNullMapAccessor<Point2D> {


    public SymmetricPoint2DStyleableMapAccessor(String name,
                                                NonNullMapAccessor<Double> xKey,
                                                NonNullMapAccessor<Double> yKey) {
        super(name, xKey, yKey, new SymmetricPoint2DConverter(false));
    }
}
