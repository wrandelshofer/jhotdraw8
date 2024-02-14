/*
 * @(#)SvgDrawing.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.figure;

import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.StringCssConverter;
import org.jhotdraw8.draw.css.converter.Rectangle2DCssConverter;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.figure.AbstractDrawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.NonTransformableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.key.NullableObjectStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;

/**
 * Represents an SVG 'svg' element.
 */
public class SvgDrawing extends AbstractDrawing
        implements StyleableFigure, LockableFigure, NonTransformableFigure, SvgDefaultableFigure,
        SvgElementFigure {

    public static final @NonNull Key<CssRectangle2D> SVG_VIEW_BOX = new NullableObjectStyleableKey<>("viewBox", "viewBox", CssRectangle2D.class, new Rectangle2DCssConverter(true), null);
    public static final @NonNull Key<String> BASE_PROFILE = new NullableObjectStyleableKey<>("baseProfile", "baseProfile", String.class, new StringCssConverter(true), null);
    public static final @NonNull Key<String> VERSION = new NullableObjectStyleableKey<>("version", "version", String.class, new StringCssConverter(true), null);

    @Override
    public boolean isSuitableChild(@NonNull Figure newChild) {
        return true;
    }


    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node n) {
        super.updateNode(ctx, n);
        applyStyleableFigureProperties(ctx, n);
    }

    @Override
    public @NonNull String getTypeSelector() {
        return "svg";
    }
}
