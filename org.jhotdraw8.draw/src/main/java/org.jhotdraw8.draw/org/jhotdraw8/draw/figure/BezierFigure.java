/*
 * @(#)BezierFigure.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.css.StyleOrigin;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.connector.PathConnector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.handle.BezierControlPointEditHandle;
import org.jhotdraw8.draw.handle.BezierNodeEditHandle;
import org.jhotdraw8.draw.handle.BezierNodeTangentHandle;
import org.jhotdraw8.draw.handle.BezierPathEditHandle;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.PathIterableOutlineHandle;
import org.jhotdraw8.draw.key.BezierNodeListStyleableKey;
import org.jhotdraw8.draw.key.BooleanStyleableKey;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.BezierNode;
import org.jhotdraw8.geom.BezierNodePath;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Figure} which draws a {@link BezierNodePath}.
 *
 * @author Werner Randelshofer
 */
public class BezierFigure extends AbstractLeafFigure
        implements StrokableFigure, FillableFigure, FillRulableFigure, TransformableFigure, HideableFigure,
        StyleableFigure, LockableFigure, CompositableFigure, ResizableFigure, ConnectableFigure,
        PathIterableFigure {

    public static final BooleanStyleableKey CLOSED = new BooleanStyleableKey("closed", false);

    public static final BezierNodeListStyleableKey PATH = new BezierNodeListStyleableKey("path", ImmutableArrayList.of());
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Bezier";

    public BezierFigure() {
        setStyled(StyleOrigin.USER_AGENT, FILL, null);
    }

    @Override
    public void createHandles(@NonNull HandleType handleType, @NonNull List<Handle> list) {
        if (handleType == HandleType.SELECT) {
            list.add(new PathIterableOutlineHandle(this, true));
        } else if (handleType == HandleType.POINT) {
            list.add(new BezierPathEditHandle(this, PATH));
            ImmutableList<BezierNode> nodes = get(PATH);
            for (int i = 0, n = nodes.size(); i < n; i++) {
                list.add(new BezierNodeTangentHandle(this, PATH, i));
                list.add(new BezierNodeEditHandle(this, PATH, i));
                if (nodes.get(i).isC1()) {
                    list.add(new BezierControlPointEditHandle(this, PATH, i, BezierNode.C1_MASK));
                }
                if (nodes.get(i).isC2()) {
                    list.add(new BezierControlPointEditHandle(this, PATH, i, BezierNode.C2_MASK));
                }
            }
        } else {
            super.createHandles(handleType, list);
        }
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext ctx) {
        Path n = new Path();
        n.setManaged(false);
        return n;
    }

    @Override
    public @NonNull Connector findConnector(@NonNull Point2D p, Figure prototype, double tolerance) {
        return new PathConnector(new BoundsLocator(getLayoutBounds(), p));
    }

    @Override
    public @NonNull Bounds getLayoutBounds() {
        // XXX should be cached
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (BezierNode p : getNonNull(PATH)) {
            minX = Math.min(minX, p.getMinX());
            minY = Math.min(minY, p.getMinY());
            maxX = Math.max(maxX, p.getMaxX());
            maxY = Math.max(maxY, p.getMaxY());
        }
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public @NonNull CssRectangle2D getCssLayoutBounds() {
        return new CssRectangle2D(getLayoutBounds());
    }

    public int getNodeCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body ofCollection generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, AffineTransform tx) {
        return new BezierNodePath(getStyledNonNull(PATH), getStyledNonNull(CLOSED), getStyled(FILL_RULE)).getPathIterator(tx);
    }

    public @NonNull Point2D getPoint(int index, int coord) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body ofCollection generated methods, choose Tools | Templates.
    }

    public @NonNull Point2D getPointOnPath(float f, int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body ofCollection generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
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
    public void reshapeInLocal(@NonNull Transform transform) {
        ArrayList<BezierNode> newP = getNonNull(PATH).toArrayList();
        for (int i = 0, n = newP.size(); i < n; i++) {
            newP.set(i, newP.get(i).transform(transform));
        }
        set(PATH, ImmutableArrayList.copyOf(newP));
    }

    @Override
    public void translateInLocal(@NonNull CssPoint2D t) {
        Transform transform = new Translate(t.getX().getConvertedValue(), t.getY().getConvertedValue());
        reshapeInLocal(transform);
    }


    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        Path pathNode = (Path) node;

        applyHideableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
        applyStrokableFigureProperties(ctx, pathNode);
        applyFillableFigureProperties(ctx, pathNode);
        applyFillRulableFigureProperties(ctx, pathNode);
        applyTransformableFigureProperties(ctx, node);
        applyCompositableFigureProperties(ctx, pathNode);
        pathNode.setFillRule(getStyled(FILL_RULE));
        final List<PathElement> elements =
                FXShapes.fxPathElementsFromAwt(
                        new BezierNodePath(getStyledNonNull(PATH),
                                getStyledNonNull(CLOSED),
                                getStyledNonNull(FILL_RULE)).getPathIterator(null));

        if (!pathNode.getElements().equals(elements)) {
            pathNode.getElements().setAll(elements);
        }

    }

}
