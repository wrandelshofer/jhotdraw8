/*
 * @(#)PolylineFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.PolyPointEditHandle;
import org.jhotdraw8.draw.handle.PolyPointMoveHandle;
import org.jhotdraw8.draw.handle.PolylineOutlineHandle;
import org.jhotdraw8.draw.key.Point2DListStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * A figure which draws a connected line segments.
 *
 * @author Werner Randelshofer
 */
public class PolylineFigure extends AbstractLeafFigure
        implements StrokableFigure, FillableFigure, HideableFigure, StyleableFigure,
        LockableFigure, CompositableFigure, TransformableFigure, ResizableFigure,
        PathIterableFigure {

    public static final Point2DListStyleableKey POINTS = new Point2DListStyleableKey("points", VectorList.of());
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Polyline";

    public PolylineFigure() {
        this(0, 0, 1, 1);
    }

    public PolylineFigure(double startX, double startY, double endX, double endY) {
        set(POINTS, VectorList.of(new Point2D(startX, startY), new Point2D(endX, endY)));
        set(FILL, null);
    }

    public PolylineFigure(Point2D... points) {
        set(POINTS, VectorList.of(points));
        set(FILL, null);
    }

    @Override
    public void createHandles(@NonNull HandleType handleType, @NonNull List<Handle> list) {
        if (handleType == HandleType.SELECT) {
            list.add(new PolylineOutlineHandle(this, POINTS, false));
        } else if (handleType == HandleType.MOVE) {
            list.add(new PolylineOutlineHandle(this, POINTS, false));
            for (int i = 0, n = getNonNull(POINTS).size(); i < n; i++) {
                list.add(new PolyPointMoveHandle(this, POINTS, i));
            }
        } else if (handleType == HandleType.POINT) {
            list.add(new PolylineOutlineHandle(this, POINTS, true));
            for (int i = 0, n = getNonNull(POINTS).size(); i < n; i++) {
                list.add(new PolyPointEditHandle(this, POINTS, i));
            }
        } else {
            super.createHandles(handleType, list);
        }
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        Polyline n = new Polyline();
        n.setManaged(false);
        return n;
    }

    @Override
    public @NonNull Bounds getLayoutBounds() {
        // XXX should be cached
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Point2D p : getNonNull(POINTS)) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        return new CssRectangle2D(getLayoutBounds());
    }

    @Override
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, AffineTransform tx) {
        return FXShapes.awtPathIteratorFromFxPoint2Ds(getNonNull(POINTS).asList(), false, PathIterator.WIND_NON_ZERO, tx);
    }

    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    public void reshapeInLocal(@NonNull Transform transform) {
        ArrayList<Point2D> newP = getNonNull(POINTS).toArrayList();
        for (int i = 0, n = newP.size(); i < n; i++) {
            newP.set(i, FXTransforms.transform(transform, newP.get(i)));
        }
        set(POINTS, VectorList.copyOf(newP));
    }

    @Override
    public void translateInLocal(@NonNull CssPoint2D t) {
        ArrayList<Point2D> newP = getNonNull(POINTS).toArrayList();
        for (int i = 0, n = newP.size(); i < n; i++) {
            newP.set(i, newP.get(i).add(t.getConvertedValue()));
        }
        set(POINTS, VectorList.copyOf(newP));
    }


    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        Polyline lineNode = (Polyline) node;
        applyHideableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
        applyStrokableFigureProperties(ctx, lineNode);
        applyFillableFigureProperties(ctx, lineNode);
        applyTransformableFigureProperties(ctx, node);
        applyCompositableFigureProperties(ctx, lineNode);
        final ImmutableList<Point2D> points = getStyledNonNull(POINTS);
        List<Double> list = new ArrayList<>(points.size() * 2);
        for (Point2D p : points) {
            list.add(p.getX());
            list.add(p.getY());
        }
        lineNode.getPoints().setAll(list);
        lineNode.applyCss();
    }

    public static double @NonNull [] toPointArray(@NonNull Figure f, @NonNull NonNullMapAccessor<? extends ImmutableList<Point2D>> key) {
        ImmutableList<Point2D> points = f.getNonNull(key);
        double[] a = new double[points.size() * 2];
        for (int i = 0, n = points.size(), j = 0; i < n; i++, j += 2) {
            Point2D p = points.get(i);
            a[j] = p.getX();
            a[j + 1] = p.getY();
        }
        return a;
    }

    @Override
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        reshapeInLocal(FXTransforms.createReshapeTransform(getLayoutBounds(), x.getConvertedValue(), y.getConvertedValue(), width.getConvertedValue(), height.getConvertedValue()));

    }
}
