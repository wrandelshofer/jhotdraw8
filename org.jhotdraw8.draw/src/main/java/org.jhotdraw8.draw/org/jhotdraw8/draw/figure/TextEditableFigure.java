/*
 * @(#)TextEditableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;

/**
 * Interface for figures that support text editing at one or more
 * locations on the figure.
 */
public interface TextEditableFigure extends Figure {
    record TextEditorData(@NonNull TextEditableFigure figure, @NonNull Bounds boundsInLocal,
                          @NonNull MapAccessor<String> textKey) {
    }

    /**
     * Returns text editor data for the given point in local.
     *
     * @param pointInLocal a point in local, or null to return the default text editor data
     * @return text editor data or null if no text can be edited at the given point
     */
    TextEditorData getTextEditorDataFor(@Nullable Point2D pointInLocal, @Nullable Node node);
}
