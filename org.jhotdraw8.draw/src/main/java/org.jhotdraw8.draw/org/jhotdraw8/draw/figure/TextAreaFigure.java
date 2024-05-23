/*
 * @(#)TextAreaFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.PathConnector;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.SimpleRenderContext;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.FXShapes;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

public class TextAreaFigure extends AbstractLeafFigure
        implements StrokableFigure, FillableFigure, TransformableFigure,
        ResizableFigure, HideableFigure, StyleableFigure, LockableFigure, CompositableFigure,
        ConnectableFigure, PathIterableFigure, RectangularFigure, ShapeableFigure,
        TextableFigure, TextFontableFigure, TextLayoutableFigure, TextFillableFigure, PaddableFigure, TextEditableFigure {
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "TextArea";
    private Path path;

    public TextAreaFigure() {
    }

    @Override
    public Node createNode(RenderContext ctx) {
        Group n = new Group();
        n.setManaged(false);
        n.setAutoSizeChildren(false);
        Path p = new Path();
        Text text = new Text();
        n.getChildren().addAll(p, text);
        return n;
    }

    @Override
    public TextEditorData getTextEditorDataFor(@Nullable Point2D pointInLocal, Node node) {
        return new TextEditorData(this, getLayoutBounds(), TEXT);
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        Group g = (Group) node;
        Path p = (Path) g.getChildren().get(0);
        Text text = (Text) g.getChildren().get(1);
        applyShapeableProperties(ctx, p);
        applyStrokableFigureProperties(ctx, p);
        applyFillableFigureProperties(ctx, p);

        // We must set the font before we set the text, so that JavaFx does not need to retrieve
        // the system default font, which on Windows requires that the JavaFx Toolkit is launched.
        applyTextFontableFigureProperties(ctx, text);
        applyTextLayoutableFigureProperties(ctx, text);
        text.setText(getStyled(TEXT));

        applyTextFillableFigureProperties(ctx, text);
        applyTransformableFigureProperties(ctx, node);

        UnitConverter converter = ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        Insets padding = getStyledNonNull(PADDING).getConvertedValue(converter);
        double size = text.getFont().getSize();
        Bounds bounds = getLayoutBounds();

        double y = switch (text.getTextOrigin()) {
            default -> bounds.getMinY() + padding.getTop();
            case CENTER -> bounds.getMinY() + bounds.getHeight() * 0.5;
            case BASELINE -> bounds.getMinY() + size + padding.getTop();
            case BOTTOM -> bounds.getMaxY() - padding.getBottom();
        };

        text.setX(bounds.getMinX() + padding.getLeft());
        text.setY(y);
        text.setWrappingWidth(bounds.getWidth() - padding.getLeft() - padding.getRight());

    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }


    @Override
    public void layout(RenderContext ctx) {
        if (path == null) {
            path = new Path();
        }
        applyShapeableProperties(ctx, path);
    }

    @Override
    public @Nullable Connector findConnector(Point2D pointInLocal, Figure connectingFigure, double tolerance) {
        return new PathConnector(new BoundsLocator(getLayoutBounds(), pointInLocal));
    }

    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        if (path == null) {
            layout(new SimpleRenderContext());
        }
        return path == null ? AwtShapes.emptyPathIterator() : FXShapes.fxShapeToAwtShape(path).getPathIterator(tx);
    }
}
