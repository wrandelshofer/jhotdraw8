/*
 * @(#)PointLocator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.locator;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

/**
 * A {@link Locator} which locates a node on a point of a Figure.
 *
 * @author Werner Randelshofer
 */
public class PointLocator extends AbstractLocator {

    private final NonNullMapAccessor<CssPoint2D> key;

    public PointLocator(NonNullMapAccessor<CssPoint2D> key) {
        this.key = key;
    }

    @Override
    public @NonNull Point2D locate(@NonNull Figure owner) {
        return owner.getNonNull(key).getConvertedValue();
    }
}
