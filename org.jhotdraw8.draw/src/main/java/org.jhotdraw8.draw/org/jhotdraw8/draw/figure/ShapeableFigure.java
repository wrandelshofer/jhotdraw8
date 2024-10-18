/*
 * @(#)ShapeableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.key.CssInsetsStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.draw.key.Rectangle2DStyleableMapAccessor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.BoundingBoxBuilder;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.NineRegionsScalingBuilder;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.util.List;

public interface ShapeableFigure extends Figure {
    CssSizeStyleableKey SHAPE_SLICE_BOTTOM = new CssSizeStyleableKey("shapeSliceBottom", CssSize.ZERO);
    CssSizeStyleableKey SHAPE_SLICE_LEFT = new CssSizeStyleableKey("shapeSliceLeft", CssSize.ZERO);
    CssSizeStyleableKey SHAPE_SLICE_RIGHT = new CssSizeStyleableKey("shapeSliceRight", CssSize.ZERO);
    CssSizeStyleableKey SHAPE_SLICE_TOP = new CssSizeStyleableKey("shapeSliceTop", CssSize.ZERO);
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
    CssInsetsStyleableMapAccessor SHAPE_SLICE = new CssInsetsStyleableMapAccessor("shapeSlice", SHAPE_SLICE_TOP, SHAPE_SLICE_RIGHT, SHAPE_SLICE_BOTTOM, SHAPE_SLICE_LEFT);
    /**
     * This property specifies the bounds of a {@link #SHAPE} property. If the
     * bounds are null or empty, then the bounds of the shape are used.
     */
    DoubleStyleableKey SHAPE_BOUNDS_X = new DoubleStyleableKey("shapeBoundsX", 0.0);
    DoubleStyleableKey SHAPE_BOUNDS_Y = new DoubleStyleableKey("shapeBoundsY", 0.0);
    DoubleStyleableKey SHAPE_BOUNDS_WIDTH = new DoubleStyleableKey("shapeBoundsWidth", 0.0);
    DoubleStyleableKey SHAPE_BOUNDS_HEIGHT = new DoubleStyleableKey("shapeBoundsHeight", 0.0);
    Rectangle2DStyleableMapAccessor SHAPE_BOUNDS = new Rectangle2DStyleableMapAccessor("shapeBounds", SHAPE_BOUNDS_X, SHAPE_BOUNDS_Y, SHAPE_BOUNDS_WIDTH, SHAPE_BOUNDS_HEIGHT);
    /**
     * Defines the border image as an SVG path.
     * <p>
     * Performance: it would be nice if Shape was an already parsed representation. For example a JavaFX Path object.
     */
    NullableFXPathElementsStyleableKey SHAPE = new NullableFXPathElementsStyleableKey("shape", null);
    PersistentList<PathElement> SVG_SQUARE = VectorList.of(new MoveTo(0, 0), new LineTo(1, 0), new LineTo(1, 1), new LineTo(0, 1), new ClosePath());


    default void applyShapeableProperties(RenderContext ctx, Path node) {
        applyShapeableProperties(ctx, node, getLayoutBounds());
    }

    default void applyShapeableProperties(RenderContext ctx, Path node, Bounds b) {
        PersistentList<PathElement> content = getStyled(SHAPE);
        if (content == null || content.isEmpty()) {
            content = SVG_SQUARE;
        }
        Rectangle2D shapeBounds = getStyled(SHAPE_BOUNDS);
        final Bounds srcBounds = shapeBounds == null || FXRectangles.isEmpty(shapeBounds) ? FXShapes.buildPathElements(new BoundingBoxBuilder(), content).build() : FXRectangles.getBounds(shapeBounds);
        Insets shapeSlice = getStyledNonNull(SHAPE_SLICE).getConvertedValue(srcBounds.getWidth(), srcBounds.getHeight());
        FXPathElementsBuilder builder2 = new FXPathElementsBuilder();
        final NineRegionsScalingBuilder<List<PathElement>> nineRegionsScalingBuilder = new NineRegionsScalingBuilder<>(builder2, srcBounds, shapeSlice, b);
        FXShapes.buildPathElements(nineRegionsScalingBuilder, content);
        List<PathElement> elements = nineRegionsScalingBuilder.build();
        node.getElements().setAll(elements);
        node.setVisible(true);
    }
}
