/*
 * @(#)LabelFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;

/**
 * LabelFigure presents a text on a drawing.
 *
 * @author Werner Randelshofer
 */
public class LabelFigure extends AbstractLabelFigure
        implements HideableFigure, TextFontableFigure, TextLayoutableFigure,
        TextableFigure, StyleableFigure, LockableFigure, TransformableFigure,
        CompositableFigure, TextEditableFigure {
    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Label";

    public LabelFigure() {
        this(0, 0, "");
    }

    public LabelFigure(@NonNull Point2D position, String text) {
        this(position.getX(), position.getY(), text);
    }

    public LabelFigure(double x, double y, String text, Object... keyValues) {
        set(TEXT, text);
        set(ORIGIN, new CssPoint2D(x, y));
        for (int i = 0; i < keyValues.length; i += 2) {
            @SuppressWarnings("unchecked") // the set() method will perform the check for us
            Key<Object> key = (Key<Object>) keyValues[i];
            set(key, keyValues[i + 1]);
        }
    }

    @Override
    public @NonNull TextEditorData getTextEditorDataFor(Point2D pointInLocal, Node node) {
        return new TextEditorData(this, getLayoutBounds(), TEXT);
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        super.updateNode(ctx, node);
        applyTransformableFigureProperties(ctx, node);
        applyCompositableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
        applyHideableFigureProperties(ctx, node);
    }

    @Override
    protected String getText(@NonNull RenderContext ctx) {
        return getStyled(TEXT);
    }


    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
