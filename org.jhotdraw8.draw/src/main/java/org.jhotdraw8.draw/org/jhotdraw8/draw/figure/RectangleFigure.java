/*
 * @(#)RectangleFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.RectangleConnector;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.SymmetricCssPoint2DStyleableMapAccessor;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.icollection.VectorList;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * Renders a {@code javafx.scene.shape.Rectangle}.
 */
public class RectangleFigure extends AbstractLeafFigure
        implements StrokableFigure, FillableFigure, TransformableFigure,
        ResizableFigure, HideableFigure, StyleableFigure, LockableFigure, CompositableFigure,
        ConnectableFigure, PathIterableFigure, RectangularFigure {

    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Rectangle";

    public static final CssSizeStyleableKey ARC_HEIGHT = new CssSizeStyleableKey("arcHeight", CssSize.ZERO);
    public static final CssSizeStyleableKey ARC_WIDTH = new CssSizeStyleableKey("arcWidth", CssSize.ZERO);
    public static final @Nullable SymmetricCssPoint2DStyleableMapAccessor ARC = new SymmetricCssPoint2DStyleableMapAccessor("arc", ARC_WIDTH, ARC_HEIGHT,
            VectorList.of("0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"));

    public RectangleFigure() {
        this(0, 0, 1, 1);
    }

    public RectangleFigure(double x, double y, double width, double height) {
        reshapeInLocal(x, y, width, height);
    }

    public RectangleFigure(Rectangle2D rect) {
        this(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        Rectangle shape = new Rectangle();
        shape.setX(getNonNull(X).getConvertedValue());
        shape.setY(getNonNull(Y).getConvertedValue());
        shape.setWidth(getNonNull(WIDTH).getConvertedValue());
        shape.setHeight(getNonNull(HEIGHT).getConvertedValue());
        shape.setArcWidth(getStyledNonNull(ARC_WIDTH).getConvertedValue());
        shape.setArcHeight(getStyledNonNull(ARC_HEIGHT).getConvertedValue());
        applyFillableFigureProperties(ctx, shape);
        applyStrokableFigureProperties(ctx, shape);

        return FXShapes.fxShapeToAwtShape(shape).getPathIterator(tx);
    }


    @Override
    public Node createNode(RenderContext drawingView) {
        Rectangle n = new Rectangle();
        n.setManaged(false);
        return n;
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        Rectangle rectangleNode = (Rectangle) node;
        applyHideableFigureProperties(ctx, node);
        applyTransformableFigureProperties(ctx, rectangleNode);
        applyFillableFigureProperties(ctx, rectangleNode);
        applyStrokableFigureProperties(ctx, rectangleNode);
        applyCompositableFigureProperties(ctx, rectangleNode);
        applyStyleableFigureProperties(ctx, node);
        rectangleNode.setX(getNonNull(X).getConvertedValue());
        rectangleNode.setY(getNonNull(Y).getConvertedValue());
        rectangleNode.setWidth(getNonNull(WIDTH).getConvertedValue());
        rectangleNode.setHeight(getNonNull(HEIGHT).getConvertedValue());
        rectangleNode.setArcWidth(getStyledNonNull(ARC_WIDTH).getConvertedValue());
        rectangleNode.setArcHeight(getStyledNonNull(ARC_HEIGHT).getConvertedValue());
    }

    @Override
    public @Nullable Connector findConnector(Point2D p, Figure prototype, double tolerance) {
        return new RectangleConnector(new BoundsLocator(getLayoutBounds(), p));
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
