/*
 * @(#)FillableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Interface figures which render a {@code javafx.scene.shape.Shape} and can be
 * filled.
 *
 * @author Werner Randelshofer
 */
public interface FillableFigure extends Figure {

    /**
     * Defines the paint used for filling the interior of the figure.
     * <p>
     * Default value: {@code Color.WHITE}.
     */
    NullablePaintableStyleableKey FILL = new NullablePaintableStyleableKey("fill", new CssColor("canvas", Color.WHITE));

    /**
     * Updates a shape node.
     *
     * @param ctx   the render context
     * @param shape a shape node
     */
    default void applyFillableFigureProperties(@Nullable RenderContext ctx, Shape shape) {
        Paint p = Paintable.getPaint(getStyled(FILL), ctx);
        if (!Objects.equals(shape.getFill(), p)) {
            shape.setFill(p);
        }
    }

}
