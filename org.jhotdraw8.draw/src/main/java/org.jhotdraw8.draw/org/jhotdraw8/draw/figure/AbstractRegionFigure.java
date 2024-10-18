/*
 * @(#)AbstractRegionFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.key.CssRectangle2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NonNullBooleanStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.BoundingBoxBuilder;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXSvgPaths;
import org.jhotdraw8.geom.FXTransformPathBuilder;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 * Renders a Shape (either a Rectangle or an SVGPath) inside a rectangular region.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractRegionFigure extends AbstractLeafFigure
        implements PathIterableFigure {
    public static final CssRectangle2DStyleableMapAccessor BOUNDS = RectangleFigure.BOUNDS;
    public static final CssSizeStyleableKey HEIGHT = RectangleFigure.HEIGHT;
    public static final NullableFXPathElementsStyleableKey SHAPE = new NullableFXPathElementsStyleableKey("shape",
            VectorList.of(new MoveTo(0, 0), new HLineTo(1), new VLineTo(1), new HLineTo(0), new ClosePath()));
    public static final CssSizeStyleableKey WIDTH = RectangleFigure.WIDTH;
    public static final CssSizeStyleableKey X = RectangleFigure.X;
    public static final CssSizeStyleableKey Y = RectangleFigure.Y;

    public static final NonNullBooleanStyleableKey SHAPE_PRESERVE_RATIO_KEY = new NonNullBooleanStyleableKey("ShapePreserveRatio", false);

    private transient PersistentList<PathElement> pathElements;

    public AbstractRegionFigure() {
        this(0, 0, 1, 1);
    }

    public AbstractRegionFigure(double x, double y, double width, double height) {
        // Performance: Only set properties if the differ from the default value.
        if (x != 0 || y != 0 || width != 0 || height != 0) {
            reshapeInLocal(x, y, width, height);
        }
    }

    public AbstractRegionFigure(Rectangle2D rect) {
        this(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public Node createNode(RenderContext drawingView) {
        Path n = new Path();
        n.setManaged(false);
        return n;
    }

    @Override
    public CssRectangle2D getCssLayoutBounds() {
        return getNonNull(BOUNDS);
    }

    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        if (pathElements == null) {
            pathElements = VectorList.of();
        }
        return FXShapes.fxPathElementsToAwtPathIterator(pathElements, PathIterator.WIND_EVEN_ODD, tx);
    }

    @Override
    public void layout(RenderContext ctx) {
        layoutPath();
    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        set(X, width.getValue() < 0 ? x.add(width) : x);
        set(Y, height.getValue() < 0 ? y.add(height) : y);
        set(WIDTH, width.abs());
        set(HEIGHT, height.abs());
    }


    protected void updatePathNode(RenderContext ctx, Path path) {
        path.getElements().setAll(this.pathElements.asCollection());
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        Path path = (Path) node;
        updatePathNode(ctx, path);
    }

    protected void layoutPath() {
        if (pathElements == null) {
            pathElements = VectorList.of();
        }

        PersistentList<PathElement> shape = getStyled(SHAPE);
        if (shape == null || shape.isEmpty()) {
            return;
        }

        double width = getStyledNonNull(WIDTH).getConvertedValue();
        double height = getStyledNonNull(HEIGHT).getConvertedValue();
        double x = getStyledNonNull(X).getConvertedValue();
        double y = getStyledNonNull(Y).getConvertedValue();
        Bounds shapeBounds = FXSvgPaths.buildPathElements(new BoundingBoxBuilder(), shape).build();
        final Bounds b;
        if (getStyledNonNull(SHAPE_PRESERVE_RATIO_KEY)) {
            double pathRatio = shapeBounds.getHeight() / shapeBounds.getWidth();
            double regionRatio = height / width;
            if (pathRatio < regionRatio) {
                b = new BoundingBox(
                        x,
                        y,
                        width,
                        pathRatio * width);
            } else {
                b = new BoundingBox(
                        x,
                        y,
                        height / pathRatio,
                        height);
            }
        } else {
            b = new BoundingBox(
                    x,
                    y,
                    width,
                    height);
        }
        Transform tx = FXTransforms.createReshapeTransform(
                shapeBounds.getMinX(), shapeBounds.getMinY(), shapeBounds.getWidth(), shapeBounds.getHeight(),
                b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight()
        );
        if (tx.isIdentity()) {
            this.pathElements = shape;
        } else {
            final var builder = new FXTransformPathBuilder<>(new FXPathElementsBuilder(), tx);
            FXShapes.buildPathElements(builder, shape);
            this.pathElements = VectorList.copyOf(builder.build());
        }
    }
}
