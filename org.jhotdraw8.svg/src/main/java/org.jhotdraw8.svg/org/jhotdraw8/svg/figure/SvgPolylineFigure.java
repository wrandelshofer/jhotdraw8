/*
 * @(#)SvgPolylineFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssRectangle2D;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.figure.AbstractLeafFigure;
import org.jhotdraw8.draw.figure.HideableFigure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.PathIterableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.key.DoubleListStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents an SVG 'polyline' element.
 *
 */
public class SvgPolylineFigure extends AbstractLeafFigure
        implements StyleableFigure, LockableFigure, SvgTransformableFigure, PathIterableFigure, HideableFigure, SvgPathLengthFigure, SvgDefaultableFigure,
        SvgElementFigure {
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "polyline";
    public static final DoubleListStyleableKey POINTS = new DoubleListStyleableKey("points");

    @Override
    public Node createNode(RenderContext ctx) {
        Polyline polyline = new Polyline();
        polyline.setManaged(false);
        return polyline;
    }

    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        Path2D.Double p = new Path2D.Double();
        PersistentList<Double> points = get(POINTS);
        if (points != null) {
            for (int i = 0, n = points.size(); i < n - 1; i += 2) {
                if (i == 0) {
                    p.moveTo(points.get(0), points.get(1));
                } else {
                    p.lineTo(points.get(i), points.get(i + 1));
                }
            }
            p.closePath();
        }
        return p.getPathIterator(tx);
    }


    @Override
    public Bounds getBoundsInLocal() {
        double minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY,
                maxx = Double.NEGATIVE_INFINITY, maxy = Double.NEGATIVE_INFINITY;
        PersistentList<Double> points = get(POINTS);
        if (points != null) {
            for (int i = 0, n = points.size(); i < n - 1; i += 2) {
                double x = points.get(i);
                double y = points.get(i + 1);
                minx = Math.min(minx, x);
                miny = Math.min(miny, y);
                maxx = Math.max(maxx, x);
                maxy = Math.max(maxy, y);
            }
        }
        return new BoundingBox(minx, miny, maxx - minx, maxy - miny);
    }

    @Override
    public CssRectangle2D getCssLayoutBounds() {
        Bounds b = getBoundsInLocal();
        return new CssRectangle2D(b);
    }


    @Override
    public void reshapeInLocal(Transform transform) {
        PersistentList<Double> points = get(POINTS);
        if (points != null) {
            List<Double> t = new ArrayList<>(points.size());
            for (int i = 0, n = points.size(); i < n - 1; i += 2) {
                Point2D transformed = transform.transform(points.get(i), points.get(i + 1));
                t.add(transformed.getX());
                t.add(transformed.getY());
            }
            set(POINTS, VectorList.copyOf(t));
        }
    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        reshapeInLocal(x.getConvertedValue(), y.getConvertedValue(), width.getConvertedValue(), height.getConvertedValue());
    }

    @Override
    public void reshapeInLocal(double x, double y, double width, double height) {
        reshapeInLocal(FXTransforms.createReshapeTransform(getLayoutBounds(), x, y, width, height));
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        Polyline polyline = (Polyline) node;
        UnitConverter unit = ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        PersistentList<Double> points = get(POINTS);
        if (points == null || points.isEmpty() || points.size() % 2 == 1) {
            polyline.setVisible(false);
            return;
        }

        applyHideableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
        applyTransformableFigureProperties(ctx, node);
        applySvgDefaultableCompositingProperties(ctx, node);
        applySvgShapeProperties(ctx, polyline);
        polyline.getPoints().setAll(points.asList());


    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
