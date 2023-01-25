/*
 * @(#)SimpleLabelConnectionFigure.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.Key;

/**
 * LabelConnectionFigure.
 *
 * @author Werner Randelshofer
 */
public class SimpleLabelConnectionFigure extends AbstractLabelConnectionFigure
        implements HideableFigure, TextFontableFigure, TextLayoutableFigure,
        TextableFigure, StyleableFigure, LockableFigure, TransformableFigure, CompositableFigure {

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "LabelConnection";

    public SimpleLabelConnectionFigure(@NonNull Point2D position, String text) {
        this(position.getX(), position.getY(), text);
    }

    public SimpleLabelConnectionFigure(double x, double y, String text, @NonNull Object... keyValues) {
        set(TEXT, text);
        set(ORIGIN, new CssPoint2D(x, y));
        for (int i = 0; i < keyValues.length; i += 2) {
            @SuppressWarnings("unchecked") // the set() method will perform the check for us
            Key<Object> key = (Key<Object>) keyValues[i];
            set(key, keyValues[i + 1]);
        }
    }

    @Override
    protected String getText(RenderContext ctx) {
        return get(TEXT);
    }

    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
