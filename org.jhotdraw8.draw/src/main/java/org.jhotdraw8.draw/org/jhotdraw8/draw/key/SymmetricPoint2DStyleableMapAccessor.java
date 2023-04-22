/*
 * @(#)SymmetricPoint2DStyleableMapAccessor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.css.converter.SymmetricPoint2DConverter;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;

import java.io.Serial;

/**
 * SymmetricPoint2DStyleableMapAccessor.
 *
 * @author Werner Randelshofer
 */
public class SymmetricPoint2DStyleableMapAccessor extends Point2DStyleableMapAccessor
        implements NonNullMapAccessor<@NonNull Point2D> {

    @Serial
    private static final long serialVersionUID = 1L;

    public SymmetricPoint2DStyleableMapAccessor(@NonNull String name,
                                                @NonNull NonNullMapAccessor<@NonNull Double> xKey,
                                                @NonNull NonNullMapAccessor<@NonNull Double> yKey) {
        super(name, xKey, yKey, new SymmetricPoint2DConverter(false));
    }
}
