/*
 * @(#)ShapeableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.key.CssInsetsStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NullableSvgPathStyleableKey;
import org.jhotdraw8.draw.key.Rectangle2DStyleableMapAccessor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.AwtPathBuilder;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.NineRegionsScalingBuilder;
import org.jhotdraw8.geom.SvgPaths;

import java.awt.geom.Path2D;
import java.text.ParseException;
import java.util.List;

public interface ShapeableFigure extends Figure {
    @NonNull CssSizeStyleableKey SHAPE_SLICE_BOTTOM = new CssSizeStyleableKey("shapeSliceBottom", CssSize.ZERO);
    @NonNull CssSizeStyleableKey SHAPE_SLICE_LEFT = new CssSizeStyleableKey("shapeSliceLeft", CssSize.ZERO);
    @NonNull CssSizeStyleableKey SHAPE_SLICE_RIGHT = new CssSizeStyleableKey("shapeSliceRight", CssSize.ZERO);
    @NonNull CssSizeStyleableKey SHAPE_SLICE_TOP = new CssSizeStyleableKey("shapeSliceTop", CssSize.ZERO);
    /**
     * This property specifies inward offsets from the top, right, bottom, and
     * left edges of the border image defined by the {@link #SHAPE_BOUNDS}
     * property, dividing it into nine regions. Percentages are relative to the
     * shape bounds: the width for the horizontal offsets, the height for the
     * vertical offsets. Numbers represent pixel units in the image.
     * <p>
     * See
     * <a href="https://www.w3.org/TR/css3-background/#border-image-slice">CSS3
     * Background: border-image-slice</a>.
     */
    @NonNull CssInsetsStyleableMapAccessor SHAPE_SLICE = new CssInsetsStyleableMapAccessor("shapeSlice", SHAPE_SLICE_TOP, SHAPE_SLICE_RIGHT, SHAPE_SLICE_BOTTOM, SHAPE_SLICE_LEFT);
    /**
     * This property specifies the bounds of a {@link #SHAPE} property. If the
     * bounds are null or empty, then the bounds of the shape are used.
     */
    @NonNull DoubleStyleableKey SHAPE_BOUNDS_X = new DoubleStyleableKey("shapeBoundsX", 0.0);
    @NonNull DoubleStyleableKey SHAPE_BOUNDS_Y = new DoubleStyleableKey("shapeBoundsY", 0.0);
    @NonNull DoubleStyleableKey SHAPE_BOUNDS_WIDTH = new DoubleStyleableKey("shapeBoundsWidth", 0.0);
    @NonNull DoubleStyleableKey SHAPE_BOUNDS_HEIGHT = new DoubleStyleableKey("shapeBoundsHeight", 0.0);
    @NonNull Rectangle2DStyleableMapAccessor SHAPE_BOUNDS = new Rectangle2DStyleableMapAccessor("shapeBounds", SHAPE_BOUNDS_X, SHAPE_BOUNDS_Y, SHAPE_BOUNDS_WIDTH, SHAPE_BOUNDS_HEIGHT);
    /**
     * Defines the border image as an SVG path.
     * <p>
     * Performance: it would be nice if Shape was an already parsed representation. For example a JavaFX Path object.
     */
    @NonNull NullableSvgPathStyleableKey SHAPE = new NullableSvgPathStyleableKey("shape", null);
    @NonNull String SVG_SQUARE = "M 0,0 1,0 1,1 0,1 Z";

    default void applyShapeableProperties(RenderContext ctx, @NonNull Path node) {
        applyShapeableProperties(ctx, node, getLayoutBounds());
    }

    default void applyShapeableProperties(RenderContext ctx, @NonNull Path node, @NonNull Bounds b) {
        String content = getStyled(SHAPE);
        if (content == null || content.trim().isEmpty()) {
            content = SVG_SQUARE;
        }

        try {
            AwtPathBuilder builder = new AwtPathBuilder(new Path2D.Double());
            SvgPaths.svgStringToBuilder(content, builder);
            Path2D path = builder.build();


            Rectangle2D shapeBounds = getStyled(SHAPE_BOUNDS);

            final Bounds srcBounds = shapeBounds == null || FXRectangles.isEmpty(shapeBounds) ? FXRectangles.getBounds(path) : FXRectangles.getBounds(shapeBounds);
            Insets shapeSlice = getStyledNonNull(SHAPE_SLICE).getConvertedValue(srcBounds.getWidth(), srcBounds.getHeight());

            FXPathElementsBuilder builder2 = new FXPathElementsBuilder();
            final NineRegionsScalingBuilder<List<PathElement>> nineRegionsScalingBuilder = new NineRegionsScalingBuilder<>(builder2, srcBounds, shapeSlice, b);

            AwtShapes.buildFromPathIterator(nineRegionsScalingBuilder, path.getPathIterator(null));
            List<PathElement> elements = nineRegionsScalingBuilder.build();
            node.getElements().setAll(elements);
            node.setVisible(true);
        } catch (ParseException ex) {
            node.setVisible(false);
        }
    }
}
