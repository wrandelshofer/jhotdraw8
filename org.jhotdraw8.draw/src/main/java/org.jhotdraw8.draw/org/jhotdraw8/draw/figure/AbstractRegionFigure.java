/*
 * @(#)AbstractRegionFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.key.CssRectangle2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NonNullBooleanStyleableKey;
import org.jhotdraw8.draw.key.NullableSvgPathStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.AwtPathBuilder;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.SvgPaths;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.text.ParseException;
import java.util.logging.Logger;

/**
 * Renders a Shape (either a Rectangle or an SVGPath) inside a rectangular region.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractRegionFigure extends AbstractLeafFigure
        implements PathIterableFigure {
    public static final @NonNull CssRectangle2DStyleableMapAccessor BOUNDS = RectangleFigure.BOUNDS;
    public static final @NonNull CssSizeStyleableKey HEIGHT = RectangleFigure.HEIGHT;
    public static final @NonNull NullableSvgPathStyleableKey SHAPE = new NullableSvgPathStyleableKey("shape", "M 0,0 h 1 v -1 h -1 Z");
    public static final @NonNull CssSizeStyleableKey WIDTH = RectangleFigure.WIDTH;
    public static final @NonNull CssSizeStyleableKey X = RectangleFigure.X;
    public static final @NonNull CssSizeStyleableKey Y = RectangleFigure.Y;

    public static final NonNullBooleanStyleableKey SHAPE_PRESERVE_RATIO_KEY = new NonNullBooleanStyleableKey("ShapePreserveRatio", false);
    private static final Logger LOGGER = Logger.getLogger(AbstractRegionFigure.class.getName());

    private transient Path2D.Double path;

    public AbstractRegionFigure() {
        this(0, 0, 1, 1);
    }

    public AbstractRegionFigure(double x, double y, double width, double height) {
        // Performance: Only set properties if the differ from the default value.
        if (x != 0 || y != 0 || width != 0 || height != 0) {
            reshapeInLocal(x, y, width, height);
        }
    }

    public AbstractRegionFigure(@NonNull Rectangle2D rect) {
        this(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        Path n = new Path();
        n.setManaged(false);
        return n;
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        return getNonNull(BOUNDS);
    }

    @Override
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, AffineTransform tx) {
        if (path == null) {
            path = new Path2D.Double();
        }
        return path.getPathIterator(tx);
    }

    @Override
    public void layout(@NonNull RenderContext ctx) {
        layoutPath();
    }

    @Override
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        set(X, width.getValue() < 0 ? x.add(width) : x);
        set(Y, height.getValue() < 0 ? y.add(height) : y);
        set(WIDTH, width.abs());
        set(HEIGHT, height.abs());
    }


    protected void updatePathNode(RenderContext ctx, @NonNull Path path) {
        path.getElements().setAll(FXShapes.fxPathElementsFromAwt(this.path.getPathIterator(null)));
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        Path path = (Path) node;
        updatePathNode(ctx, path);
    }

    protected void layoutPath() {
        if (path == null) {
            path = new Path2D.Double();
        }
        path.reset();

        String pathstr = getStyled(SHAPE);
        if (pathstr == null || pathstr.isEmpty()) {
            return;
        }

        double width = getStyledNonNull(WIDTH).getConvertedValue();
        double height = getStyledNonNull(HEIGHT).getConvertedValue();
        double x = getStyledNonNull(X).getConvertedValue();
        double y = getStyledNonNull(Y).getConvertedValue();
        final Bounds b;
        if (getStyledNonNull(SHAPE_PRESERVE_RATIO_KEY)) {
            AwtPathBuilder awtPathBuilder = new AwtPathBuilder(path);
            try {
                SvgPaths.svgStringToBuilder(pathstr, awtPathBuilder);
                java.awt.geom.Rectangle2D bounds2D = awtPathBuilder.build().getBounds2D();
                double pathRatio = bounds2D.getHeight() / bounds2D.getWidth();
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
                path.reset();
            } catch (ParseException e) {
                LOGGER.warning("Illegal SVG path: " + pathstr);
                return;
            }
        } else {
            b = new BoundingBox(
                    x,
                    y,
                    width,
                    height);
        }
        //XXX this method is only available since Java SE 11
        //path.trimToSize();
        SvgPaths.svgStringReshapeToBuilder(pathstr, b, new AwtPathBuilder(path));
    }
}
