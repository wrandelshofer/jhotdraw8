/*
 * @(#)RectangularFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.key.CssRectangle2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;

/**
 * Defines properties and default methods for figures which have a rectangular shape.
 */
public interface RectangularFigure extends Figure {
    CssSizeStyleableKey X = new CssSizeStyleableKey("x", CssSize.ZERO);
    CssSizeStyleableKey Y = new CssSizeStyleableKey("y", CssSize.ZERO);
    CssSizeStyleableKey WIDTH = new CssSizeStyleableKey("width", CssSize.ZERO);
    CssSizeStyleableKey HEIGHT = new CssSizeStyleableKey("height", CssSize.ZERO);
    CssRectangle2DStyleableMapAccessor BOUNDS = new CssRectangle2DStyleableMapAccessor("bounds", X, Y, WIDTH, HEIGHT);

    @Override
    default void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        set(X, width.getValue() < 0 ? x.add(width) : x);
        set(Y, height.getValue() < 0 ? y.add(height) : y);
        set(WIDTH, width.abs());
        set(HEIGHT, height.abs());
    }

    @Override
    default void translateInLocal(CssPoint2D t) {
        set(X, getNonNull(X).add(t.getX()));
        set(Y, getNonNull(Y).add(t.getY()));
    }

    @Override
    default CssRectangle2D getCssLayoutBounds() {
        return getNonNull(BOUNDS);
    }
}
