/*
 * @(#)LabelFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jspecify.annotations.Nullable;

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

    public LabelFigure(Point2D position, String text) {
        this(position.getX(), position.getY(), text);
    }

    @SuppressWarnings("this-escape")
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
    public TextEditorData getTextEditorDataFor(@Nullable Point2D pointInLocal, @Nullable Node node) {
        return new TextEditorData(this, getLayoutBounds(), TEXT);
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        super.updateNode(ctx, node);
        applyTransformableFigureProperties(ctx, node);
        applyCompositableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
        applyHideableFigureProperties(ctx, node);
    }

    @Override
    protected @Nullable String getText(RenderContext ctx) {
        return getStyled(TEXT);
    }


    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
