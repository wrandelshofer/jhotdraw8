/*
 * @(#)TextFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.RectangleConnector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.key.CssPoint2DStyleableKey;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.SimpleRenderContext;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * {@code TextFigure} is a {@code TextFontableFigure} which supports stroking and
 * filling of the text.
 *
 * @author Werner Randelshofer
 */
public class TextFigure extends AbstractLeafFigure
        implements StrokableFigure, FillableFigure, TransformableFigure, TextFontableFigure, TextLayoutableFigure,
        TextableFigure, HideableFigure, StyleableFigure, LockableFigure, CompositableFigure,
        ConnectableFigure, PathIterableFigure, TextEditableFigure {

    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Text";
    public static final CssPoint2DStyleableKey ORIGIN = new CssPoint2DStyleableKey("origin", new CssPoint2D(0, 0));

    private Text textNode;

    public TextFigure() {
        this(0, 0, "");
    }

    public TextFigure(Point2D position, String text) {
        this(position.getX(), position.getY(), text);
    }

    public TextFigure(double x, double y, String text) {
        set(TEXT, text);
        set(ORIGIN, new CssPoint2D(x, y));
    }

    @Override
    public Bounds getLayoutBounds() {
        // FIXME the text node should be computed during layout
        if (textNode == null) {
            layout(new SimpleRenderContext());
        }

        Bounds b = textNode.getLayoutBounds();
        return new BoundingBox(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
    }

    @Override
    public TextEditorData getTextEditorDataFor(Point2D pointInLocal, Node node) {
        return new TextEditorData(this, getLayoutBounds(), TEXT);
    }

    @Override
    public void layout(RenderContext ctx) {
        if (textNode == null) {
            textNode = new Text();
        }
        updateNode(ctx, textNode);
    }

    @Override
    public CssRectangle2D getCssLayoutBounds() {
        return new CssRectangle2D(getLayoutBounds());
    }

    @Override
    public void reshapeInLocal(Transform transform) {
        Point2D o = getNonNull(ORIGIN).getConvertedValue();
        o = FXTransforms.transform(transform, o);
        set(ORIGIN, new CssPoint2D(o));
    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        Bounds b = getLayoutBounds();
        reshapeInLocal(Transform.translate(x.getConvertedValue() - b.getMinX(), y.getConvertedValue() - b.getMinY()));
    }

    @Override
    public Node createNode(RenderContext drawingView) {
        Text n = new Text();
        n.setManaged(false);
        return n;
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        Text tn = (Text) node;
        tn.setX(getStyledNonNull(ORIGIN).getX().getConvertedValue());
        tn.setY(getStyledNonNull(ORIGIN).getY().getConvertedValue());
        tn.setBoundsType(TextBoundsType.VISUAL);
        applyHideableFigureProperties(ctx, node);
        applyTransformableFigureProperties(ctx, tn);
        applyTextableFigureProperties(ctx, tn);
        applyStrokableFigureProperties(ctx, tn);
        applyFillableFigureProperties(ctx, tn);
        applyCompositableFigureProperties(ctx, tn);
        applyTextFontableFigureProperties(ctx, tn);
        applyTextLayoutableFigureProperties(ctx, tn);
        applyStyleableFigureProperties(ctx, node);

        // We must set the font before we set the text, so that JavaFx does not need to retrieve
        // the system default font, which on Windows requires that the JavaFx Toolkit is launched.
        tn.setText(get(TEXT));
    }

    @Override
    public @Nullable Connector findConnector(Point2D p, Figure prototype, double tolerance) {
        return new RectangleConnector(new BoundsLocator(getLayoutBounds(), p));
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        if (textNode == null) {
            layout(new SimpleRenderContext());
        }
        return FXShapes.fxShapeToAwtShape(textNode).getPathIterator(tx);
    }


}
