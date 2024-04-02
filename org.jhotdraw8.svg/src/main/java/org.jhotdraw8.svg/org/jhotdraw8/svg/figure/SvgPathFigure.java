/*
 * @(#)SvgPathFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.NumberConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.figure.AbstractLeafFigure;
import org.jhotdraw8.draw.figure.HideableFigure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.PathIterableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.key.StringStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.AwtPathBuilder;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXSvgPaths;
import org.jhotdraw8.geom.FXTransformPathBuilder;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.SvgPaths;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.text.ParseException;
import java.util.List;


/**
 * Represents an SVG 'path' element.
 *
 * @author Werner Randelshofer
 */
public class SvgPathFigure extends AbstractLeafFigure
        implements StyleableFigure, LockableFigure, SvgTransformableFigure, PathIterableFigure, HideableFigure, SvgPathLengthFigure, SvgDefaultableFigure,
        SvgElementFigure {
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final @NonNull String TYPE_SELECTOR = "path";
    public static final @NonNull StringStyleableKey D = new StringStyleableKey("d");

    @Override
    public @NonNull Node createNode(@NonNull RenderContext ctx) {
        Group g = new Group();
        Path n0 = new Path();
        Path n1 = new Path();
        n0.setManaged(false);
        n1.setManaged(false);
        g.getChildren().addAll(n0, n1);
        return g;
    }

    @Override
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, @Nullable AffineTransform tx) {
        AwtPathBuilder b = new AwtPathBuilder();
        String d = get(D);
        if (d != null) {
            try {
                SvgPaths.svgStringToBuilder(d, b);
            } catch (ParseException e) {
                // bail
            }
        }
        return b.build().getPathIterator(tx);
    }


    @Override
    public @NonNull Bounds getBoundsInLocal() {
        AwtPathBuilder b = new AwtPathBuilder();
        String d = get(D);
        if (d != null) {
            try {
                SvgPaths.svgStringToBuilder(d, b);
            } catch (ParseException e) {
                // bail
            }
        }
        Rectangle bounds = b.build().getBounds();
        return new BoundingBox(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        Bounds b = getBoundsInLocal();
        return new CssRectangle2D(b);
    }


    @Override
    public void reshapeInLocal(@NonNull Transform transform) {
        FXPathElementsBuilder bb = new FXPathElementsBuilder();
        FXTransformPathBuilder<List<PathElement>> b = new FXTransformPathBuilder<>(bb);
        b.setTransform(transform);
        String d = get(D);
        if (d != null) {
            try {
                SvgPaths.svgStringToBuilder(d, b);
            } catch (ParseException e) {
                // bail
            }
        }

        List<PathElement> build = b.build();
        set(D, build == null ? null : FXSvgPaths.pathElementsToSvgString(build, new NumberConverter()));
    }

    @Override
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        reshapeInLocal(x.getConvertedValue(), y.getConvertedValue(), width.getConvertedValue(), height.getConvertedValue());
    }

    @Override
    public void reshapeInLocal(double x, double y, double width, double height) {
        reshapeInLocal(FXTransforms.createReshapeTransform(getLayoutBounds(), x, y, width, height));
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        Group g = (Group) node;
        Path n0 = (Path) g.getChildren().get(0);
        Path n1 = (Path) g.getChildren().get(1);

        applyHideableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
        applyTransformableFigureProperties(ctx, node);
        applySvgDefaultableCompositingProperties(ctx, node);
        applySvgShapeProperties(ctx, n0, n1);

        FXPathElementsBuilder bb = new FXPathElementsBuilder();
        String d = get(D);
        if (d != null) {
            try {
                SvgPaths.svgStringToBuilder(d, bb);
            } catch (ParseException e) {
                // bail
            }
        }
        List<PathElement> build = bb.build();
        n0.getElements().setAll(build);
        n1.getElements().setAll(build);

        FillRule fillRule = getDefaultableStyled(FILL_RULE_KEY);
        n0.setFillRule(fillRule);
        n1.setFillRule(fillRule);
    }

    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
