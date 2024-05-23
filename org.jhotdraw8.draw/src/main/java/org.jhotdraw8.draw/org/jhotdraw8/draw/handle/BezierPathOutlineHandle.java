/*
 * @(#)BezierPathOutlineHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.transform.Transform;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.FXLines;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.QuadCurves;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BezierPathOutlineHandle extends AbstractHandle {
    private final Group node;
    private final Path path2;
    private final Path path1;
    private double strokeWidth = 1;

    private final MapAccessor<BezierPath> bezierPathKey;
    private final boolean selectable;

    public BezierPathOutlineHandle(Figure figure, MapAccessor<BezierPath> pointKey, boolean selectable) {
        super(figure);
        this.bezierPathKey = pointKey;
        node = new Group();
        path2 = new Path();
        path1 = new Path();
        node.getChildren().addAll(path1, path2);
        this.selectable = selectable;
    }

    @Override
    public @Nullable Cursor getCursor() {
        return null;
    }

    @Override
    public boolean contains(DrawingView drawingView, double x, double y, double tolerance) {
        final BezierPath path = getOwner().get(bezierPathKey);
        if (path != null) {
            final Point2D p = drawingView.viewToWorld(x, y);
            return path.contains(p.getX(), p.getY(), tolerance);
        }
        return false;

    }


    @Override
    public Node getNode(DrawingView view) {
        CssColor color = view.getEditor().getHandleColor();
        path1.setStroke(Color.WHITE);
        path2.setStroke(Paintable.getPaint(color));
        strokeWidth = view.getEditor().getHandleStrokeWidth();
        path1.setStrokeWidth(strokeWidth + 2);
        path2.setStrokeWidth(strokeWidth);
        return node;
    }

    @Override
    public void updateNode(DrawingView view) {
        Figure f = getOwner();
        final BezierPath path = f.get(bezierPathKey);
        if (path == null) {
            path1.getElements().clear();
            path2.getElements().clear();
            return;
        }
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        List<PathElement> elements = new ArrayList<>();
        FXPathElementsBuilder builder = new FXPathElementsBuilder(elements);
        AwtShapes.buildPathIterator(builder, path.getPathIterator(FXShapes.fxTransformToAwtTransform(t)));

        // draw a small arrow at the center of each segment, to visualize the direction of the path
        double arrowLength = 4 * Math.max(2, strokeWidth);
        double arrowWidth = 1.5 * Math.max(2, strokeWidth);
        if (!elements.isEmpty()) {
            double x = 0, y = 0;
            double newx = 0, newy = 0;
            for (int i = 0, n = elements.size(); i < n; i++) {
                PathElement node = elements.get(i);
                Point2D p = null;
                Point2D dir = null;
                switch (node) {
                    case LineTo e -> {
                        newx = e.getX();
                        newy = e.getY();
                        p = FXLines.lerp(x, y, e.getX(), e.getY(), 0.5);
                        dir = new Point2D(e.getX() - x, e.getY() - y).normalize();
                    }
                    case QuadCurveTo e -> {
                        PointAndDerivative pAndD = QuadCurves.eval(x, y, e.getControlX(), e.getControlY(), e.getX(), e.getY(), 0.5);
                        p = pAndD.getPoint(Point2D::new);
                        dir = pAndD.getDerivative(Point2D::new).normalize();
                        newx = e.getX();
                        newy = e.getY();
                    }
                    case CubicCurveTo e -> {
                        PointAndDerivative pAndD = CubicCurves.eval(x, y,
                                e.getControlX1(), e.getControlY1(),
                                e.getControlX2(), e.getControlY2(),
                                e.getX(), e.getY(), 0.5);
                        p = pAndD.getPoint(Point2D::new);
                        dir = pAndD.getDerivative(Point2D::new).normalize();
                        newx = e.getX();
                        newy = e.getY();
                    }
                    case MoveTo e -> {
                        newx = e.getX();
                        newy = e.getY();
                    }
                    default -> {
                    }
                }
                if (p != null && dir != null) {
                    Point2D perp = FXGeom.perp(dir);
                    double vx = perp.getX() * arrowWidth;
                    double vy = perp.getY() * arrowWidth;
                    double dx = dir.getX() * arrowLength;
                    double dy = dir.getY() * arrowLength;
                    elements.add(new MoveTo(p.getX() - vx - dx, p.getY() - vy - dy));
                    elements.add(new LineTo(p.getX(), p.getY()));
                    elements.add(new LineTo(p.getX() + vx - dx, p.getY() + vy - dy));
                }
                x = newx;
                y = newy;
            }
        }

        path1.getElements().setAll(elements);
        path2.getElements().setAll(elements);
    }

    @Override
    public boolean isSelectable() {
        return selectable;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
