/*
 * @(#)HideableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.key.NonNullBooleanStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;

/**
 * HideableFigure.
 *
 * @author Werner Randelshofer
 */
public interface HideableFigure extends Figure {

    /**
     * Defines the visibility of the figure. Default value: {@code true}.
     */
    NonNullBooleanStyleableKey VISIBLE = new NonNullBooleanStyleableKey("visible", true);

    /**
     * Updates a figure node with all style and effect properties defined in
     * this interface.
     * <p>
     * Applies the following properties: {@code STYLE_ID}, {@code VISIBLE}.
     * <p>
     * This method is intended to be used by {@link #updateNode}.
     *
     * @param ctx  the render context
     * @param node a node which was created with method {@link #createNode}.
     */
    default void applyHideableFigureProperties(@Nullable RenderContext ctx, @NonNull Node node) {
        node.setVisible(getStyledNonNull(VISIBLE));
    }

}
