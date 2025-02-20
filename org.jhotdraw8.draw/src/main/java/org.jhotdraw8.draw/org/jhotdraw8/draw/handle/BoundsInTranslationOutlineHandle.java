/*
 * @(#)BoundsInTranslationOutlineHandle.java
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
import javafx.scene.transform.Translate;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.Paintable;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.TransformableFigure;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

import static org.jhotdraw8.draw.figure.TransformableFigure.TRANSLATE_X;
import static org.jhotdraw8.draw.figure.TransformableFigure.TRANSLATE_Y;

/**
 * Draws the {@code boundsInLocal} with applied translation of a {@code Figure},
 * but does not provide any interactions.
 *
 */
public class BoundsInTranslationOutlineHandle extends AbstractHandle {
    private final Group node;
    private final Polygon poly1;
    private final Polygon poly2;
    private final double[] points;

    public BoundsInTranslationOutlineHandle(Figure figure) {
        super(figure);

        node = new Group();
        points = new double[8];
        poly1 = new Polygon(points);
        poly2 = new Polygon(points);
        poly1.setFill(null);
        poly2.setFill(null);
        poly1.setStrokeLineCap(StrokeLineCap.BUTT);
        poly2.setStrokeLineCap(StrokeLineCap.BUTT);
        poly1.setStrokeWidth(3);
        poly1.setStroke(Color.WHITE);
        node.getChildren().addAll(poly1, poly2);
        poly2.getStrokeDashArray().setAll(3.0);
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
        poly2.setStroke(Paintable.getPaint(color));
        double handleStrokeWidth = view.getEditor().getHandleStrokeWidth();
        poly2.setStrokeWidth(handleStrokeWidth);
        return node;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void updateNode(DrawingView view) {
        Figure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getParentToWorld());
        if (f instanceof TransformableFigure tf) {
            t = FXTransforms.concat(t, new Translate(tf.getNonNull(TRANSLATE_X), tf.getNonNull(TRANSLATE_Y)));
        }
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
        if (t.isType2D()) {
            FXTransforms.transform2DPoints(t, points, 0, points, 0, 4);
        }

        ObservableList<Double> pp1 = poly1.getPoints();
        ObservableList<Double> pp2 = poly2.getPoints();
        for (int i = 0; i < points.length; i++) {
            pp1.set(i, points[i]);
            pp2.set(i, points[i]);
        }

    }

}
