/*
 * @(#)EllipseFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Ellipse;
import org.jhotdraw8.css.value.CssRectangle2D;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.EllipseConnector;
import org.jhotdraw8.draw.key.CssPoint2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXShapes;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * Renders a {@code javafx.scene.shape.Ellipse}.
 *
 */
public class EllipseFigure extends AbstractLeafFigure
        implements StrokableFigure, ResizableFigure, FillableFigure, TransformableFigure, HideableFigure, StyleableFigure,
        LockableFigure, CompositableFigure, ConnectableFigure, PathIterableFigure {

    public static final CssSizeStyleableKey CENTER_X = new CssSizeStyleableKey("centerX", CssSize.ZERO);
    public static final CssSizeStyleableKey CENTER_Y = new CssSizeStyleableKey("centerY", CssSize.ZERO);
    public static final CssPoint2DStyleableMapAccessor CENTER = new CssPoint2DStyleableMapAccessor("center", CENTER_X, CENTER_Y);
    public static final CssSizeStyleableKey RADIUS_X = new CssSizeStyleableKey("radiusX", CssSize.ONE);
    public static final CssSizeStyleableKey RADIUS_Y = new CssSizeStyleableKey("radiusY", CssSize.ONE);
    public static final CssPoint2DStyleableMapAccessor RADIUS = new CssPoint2DStyleableMapAccessor("radius", RADIUS_X, RADIUS_Y);
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Ellipse";

    public EllipseFigure() {
        this(0, 0, 2, 2);// the values must correspond to the default values of the property keys
    }

    public EllipseFigure(double x, double y, double width, double height) {
        reshapeInLocal(x, y, width, height);
    }

    public EllipseFigure(Rectangle2D rect) {
        reshapeInLocal(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public Node createNode(RenderContext drawingView) {
        Ellipse n = new Ellipse();
        n.setManaged(false);
        return n;
    }

    @Override
    public @Nullable Connector findConnector(Point2D p, Figure prototype, double tolerance) {
        return new EllipseConnector(new BoundsLocator(getLayoutBounds(), p));
    }

    @Override
    public Bounds getLayoutBounds() {
        double rx = getNonNull(RADIUS_X).getConvertedValue();
        double ry = getNonNull(RADIUS_Y).getConvertedValue();
        double cx = getNonNull(CENTER_X).getConvertedValue();
        double cy = getNonNull(CENTER_Y).getConvertedValue();
        return new BoundingBox(cx - rx, cy - ry, rx * 2.0, ry * 2.0);
    }

    @Override
    public CssRectangle2D getCssLayoutBounds() {
        CssSize rx = getNonNull(RADIUS_X);
        CssSize ry = getNonNull(RADIUS_Y);
        return new CssRectangle2D(getNonNull(CENTER_X).subtract(rx), getNonNull(CENTER_Y).subtract(ry), rx.multiply(2.0), ry.multiply(2.0));
    }


    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        Ellipse shape = new Ellipse();
        shape.setCenterX(getStyledNonNull(CENTER_X).getConvertedValue());
        shape.setCenterY(getStyledNonNull(CENTER_Y).getConvertedValue());

        double strokeWidth = getStyledNonNull(STROKE_WIDTH).getConvertedValue();
        double offset = switch (getStyledNonNull(STROKE_TYPE)) {
            default -> 0;
            case INSIDE -> -strokeWidth * 0.5;
            case OUTSIDE -> strokeWidth * 0.5;
        };
        shape.setRadiusX(getStyledNonNull(RADIUS_X).getConvertedValue() + offset);
        shape.setRadiusY(getStyledNonNull(RADIUS_Y).getConvertedValue() + offset);
        return FXShapes.fxShapeToAwtShape(shape).getPathIterator(tx);
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        CssSize rx = CssSize.max(width.multiply(0.5), CssSize.ZERO);
        CssSize ry = CssSize.max(height.multiply(0.5), CssSize.ZERO);
        set(CENTER_X, x.add(rx));
        set(CENTER_Y, y.add(ry));
        set(RADIUS_X, rx);
        set(RADIUS_Y, ry);
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        Ellipse n = (Ellipse) node;
        applyHideableFigureProperties(ctx, n);
        applyTransformableFigureProperties(ctx, n);
        applyStrokableFigureProperties(ctx, n);
        applyFillableFigureProperties(ctx, n);
        applyCompositableFigureProperties(ctx, n);
        applyStyleableFigureProperties(ctx, node);
        n.setCenterX(getStyledNonNull(CENTER_X).getConvertedValue());
        n.setCenterY(getStyledNonNull(CENTER_Y).getConvertedValue());
        n.setRadiusX(getStyledNonNull(RADIUS_X).getConvertedValue());
        n.setRadiusY(getStyledNonNull(RADIUS_Y).getConvertedValue());
        n.applyCss();
    }

}
