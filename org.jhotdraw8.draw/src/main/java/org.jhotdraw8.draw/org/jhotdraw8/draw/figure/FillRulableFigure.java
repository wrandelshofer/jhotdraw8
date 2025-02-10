/*
 * @(#)FillRulableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.shape.FillRule;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import org.jhotdraw8.draw.key.NonNullEnumStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jspecify.annotations.Nullable;

/**
 * Interface figures which render a {@code javafx.scene.shape.Shape} and can have
 * a fill rule.
 *
 */
public interface FillRulableFigure extends Figure {

    /**
     * Defines the fill-rule used for filling the interior of the figure..
     * <p>
     * Default value: {@code StrokeType.NON_ZERO}.
     */
    NonNullEnumStyleableKey<FillRule> FILL_RULE = new NonNullEnumStyleableKey<>("fill-rule", FillRule.class, FillRule.NON_ZERO);

    /**
     * Updates a shape node.
     *
     * @param ctx   the render context
     * @param shape a shape node
     */
    default void applyFillRulableFigureProperties(@Nullable RenderContext ctx, Shape shape) {
        if (shape instanceof Path) {
            ((Path) shape).setFillRule(getStyled(FILL_RULE));
        }
    }

}
