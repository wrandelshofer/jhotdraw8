/*
 * @(#)BezierNodeTangentHandle.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.shape.BezierNode;

import java.util.List;

/**
 * Handle for the point ofCollection a figure.
 *
 * @author Werner Randelshofer
 */
public class BezierNodeTangentHandle extends AbstractHandle {

    private static final @Nullable Background REGION_BACKGROUND = new Background(new BackgroundFill(Color.BLUE, null, null));
    private static final @Nullable Border REGION_BORDER = new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.DASHED, null, null));
    private final @NonNull Polyline node;

    private Point2D pickLocation;
    private final int pointIndex;
    private final MapAccessor<ImmutableList<BezierNode>> pointKey;

    public BezierNodeTangentHandle(Figure figure, MapAccessor<ImmutableList<BezierNode>> pointKey, int pointIndex) {
        super(figure);
        this.pointKey = pointKey;
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
        ImmutableList<BezierNode> list = owner.get(pointKey);
        return list.get(pointIndex);

    }

    @Override
    public @Nullable Cursor getCursor() {
        return null;
    }

    private @NonNull Point2D getLocation() {
        return getBezierNode().getC0();

    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public @NonNull Polyline getNode(@NonNull DrawingView view) {
        CssColor color = view.getEditor().getHandleColor();
        node.setStroke(color.getColor());
        return node;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void updateNode(@NonNull DrawingView view) {
        Figure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        ImmutableList<BezierNode> list = f.get(pointKey);
        if (list == null || pointIndex >= list.size()) {
            return;
        }
        BezierNode bn = getBezierNode();
        BezierNode prev = list.get((pointIndex + list.size() - 1) % list.size());
        BezierNode next = list.get((pointIndex + 1) % list.size());
        Point2D c0 = FXTransforms.transform(t, bn.getC0());
        Point2D c1 = FXTransforms.transform(t, bn.getC1());
        Point2D c2 = FXTransforms.transform(t, bn.getC2());

        Polyline node = getNode(view);
        List<Double> points = node.getPoints();
        points.clear();
        {
            if (bn.isC1()) {
                points.add(c1.getX());
                points.add(c1.getY());
                points.add(c0.getX());
                points.add(c0.getY());
            } else if (prev.isC2()) {
                /*
                c1 = Transforms.transform(t, prev.getC2());

                points.add(c1.getX());
                points.add(c1.getY());
                points.add(c0.getX());
                points.add(c0.getY());*/
            }
            if (bn.isC2()) {
                if (points.isEmpty()) {
                    points.add(c0.getX());
                    points.add(c0.getY());
                }
                points.add(c2.getX());
                points.add(c2.getY());
            } else if (next.isC1()) {
                /*
                c2 = Transforms.transform(t, next.getC1());

                if (points.isEmpty()) {
                    points.add(c0.getX());
                    points.add(c0.getY());
                }
                points.add(c2.getX());
                points.add(c2.getY());
*/

            }
        }

    }

}
