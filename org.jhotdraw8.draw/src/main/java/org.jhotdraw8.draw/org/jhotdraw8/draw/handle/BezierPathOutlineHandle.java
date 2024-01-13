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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

public class BezierPathOutlineHandle extends AbstractHandle {
    private final @NonNull Group node;
    private final @NonNull Path path2;
    private final @NonNull Path path1;
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
    public Node getNode(@NonNull DrawingView view) {
        CssColor color = view.getEditor().getHandleColor();
        path1.setStroke(Color.WHITE);
        path2.setStroke(Paintable.getPaint(color));
        strokeWidth = view.getEditor().getHandleStrokeWidth();
        path1.setStrokeWidth(strokeWidth + 2);
        path2.setStrokeWidth(strokeWidth);
        return node;
    }

    @Override
    public void updateNode(@NonNull DrawingView view) {
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
        AwtShapes.buildFromPathIterator(builder, path.getPathIterator(FXShapes.awtTransformFromFX(t)));

        // draw a small arrow at the center of each segment, to visualize the direction of the path
        double arrowSize = 3;//Math.max(3,strokeWidth);
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
                    elements.add(new MoveTo(p.getX() - perp.getX() * arrowSize - dir.getX() * arrowSize, p.getY() - perp.getY() * arrowSize - dir.getY() * arrowSize));
                    elements.add(new LineTo(p.getX(), p.getY()));
                    elements.add(new LineTo(p.getX() + perp.getX() * arrowSize - dir.getX() * arrowSize, p.getY() + perp.getY() * arrowSize - dir.getY() * arrowSize));
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
