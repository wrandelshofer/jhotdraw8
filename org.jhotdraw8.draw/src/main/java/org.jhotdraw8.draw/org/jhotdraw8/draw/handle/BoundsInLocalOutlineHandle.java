/*
 * @(#)BoundsInLocalOutlineHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Transform;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

/**
 * Draws the {@code boundsInLocal} of a {@code Figure}, but does not provide any
 * interactions.
 *
 */
public class BoundsInLocalOutlineHandle extends AbstractHandle {

    private final Group node;
    private final Polygon poly1;
    private final Polygon poly2;
    private final double[] points;

    public BoundsInLocalOutlineHandle(Figure figure) {
        super(figure);
        node = new Group();
        points = new double[8];
        poly1 = new Polygon(points);
        poly2 = new Polygon(points);
        poly1.setStrokeLineCap(StrokeLineCap.BUTT);
        poly2.setStrokeLineCap(StrokeLineCap.BUTT);
        poly1.setFill(null);
        poly2.setFill(null);
        node.getChildren().addAll(poly1, poly2);

    }

    @Override
    public boolean contains(DrawingView dv, double x, double y, double tolerance) {
        return false;
    }

    @Override
    public @Nullable Cursor getCursor() {
        return null;
    }

    @Override
    public Node getNode(DrawingView view) {
        CssColor color = view.getEditor().getHandleColor();
        poly1.setStroke(Color.WHITE);
        poly2.setStroke(Paintable.getPaint(color));
        double strokeWidth = view.getEditor().getHandleStrokeWidth();
        poly1.setStrokeWidth(strokeWidth + 2);
        poly2.setStrokeWidth(strokeWidth);
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
        t = FXTransforms.concat(Transform.translate(0.5, 0.5), t);
        Bounds b = f.getLayoutBounds();
        points[0] = b.getMinX();
        points[1] = b.getMinY();
        points[2] = b.getMaxX();
        points[3] = b.getMinY();
        points[4] = b.getMaxX();
        points[5] = b.getMaxY();
        points[6] = b.getMinX();
        points[7] = b.getMaxY();
        FXTransforms.transform2DPoints(t, points, 0, points, 0, 4);

        ObservableList<Double> pp1 = poly1.getPoints();
        ObservableList<Double> pp2 = poly2.getPoints();
        for (int i = 0; i < points.length; i++) {
            pp1.set(i, points[i]);
            pp2.set(i, points[i]);
        }
    }

}
