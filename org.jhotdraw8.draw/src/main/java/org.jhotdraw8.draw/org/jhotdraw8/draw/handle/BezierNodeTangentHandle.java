/*
 * @(#)BezierNodeTangentHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Handle for the point ofCollection a figure.
 *
 */
public class BezierNodeTangentHandle extends AbstractHandle {

    private static final @Nullable Background REGION_BACKGROUND = new Background(new BackgroundFill(Color.BLUE, null, null));
    private static final @Nullable Border REGION_BORDER = new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, null, null));
    private final Polyline node;

    private Point2D pickLocation;
    private final int pointIndex;
    private final MapAccessor<BezierPath> pathKey;

    public BezierNodeTangentHandle(Figure figure, MapAccessor<BezierPath> pathKey, int pointIndex) {
        super(figure);
        this.pathKey = pathKey;
        this.pointIndex = pointIndex;
        node = new Polyline();
        node.setManaged(false);
    }

    @Override
    public boolean contains(DrawingView drawingView, double x, double y, double tolerance) {
        Point2D p = getLocationInView();
        return Points.squaredDistance(x, y, p.getX(), p.getY()) <= tolerance * tolerance;
    }

    private BezierNode getBezierNode() {
        BezierPath list = owner.get(pathKey);
        return list.get(pointIndex);

    }

    @Override
    public @Nullable Cursor getCursor() {
        return null;
    }

    private Point2D getLocation() {
        return getBezierNode().getPoint(Point2D::new);

    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public Polyline getNode(DrawingView view) {
        CssColor color = view.getEditor().getHandleColor();
        node.setStroke(color.getColor());
        return node;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void updateNode(DrawingView view) {
        Figure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        BezierPath list = f.get(pathKey);
        if (list == null || pointIndex >= list.size()) {
            node.setVisible(false);
            return;
        }
        node.setVisible(true);
        BezierNode bn = getBezierNode();
        Point2D c0 = FXTransforms.transform(t, bn.pointX(), bn.pointY());
        Point2D c1 = FXTransforms.transform(t, bn.inX(), bn.inY());
        Point2D c2 = FXTransforms.transform(t, bn.outX(), bn.outY());

        Polyline node = getNode(view);
        List<Double> points = node.getPoints();
        points.clear();
        {
            if (bn.hasIn()) {
                points.add(c1.getX());
                points.add(c1.getY());
                points.add(c0.getX());
                points.add(c0.getY());
            }
            if (bn.hasOut()) {
                if (points.isEmpty()) {
                    points.add(c0.getX());
                    points.add(c0.getY());
                }
                points.add(c2.getX());
                points.add(c2.getY());
            }
        }

    }

}
