/*
 * @(#)BezierPathOutlineHandle.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.MapAccessor;
import org.jhotdraw8.collection.PersistentList;
import org.jhotdraw8.css.CssColor;
import org.jhotdraw8.css.Paintable;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.geom.BezierNode;
import org.jhotdraw8.geom.BezierNodePath;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.SvgPaths;

import java.util.ArrayList;
import java.util.List;

public class BezierPathOutlineHandle extends AbstractHandle {
    private final @NonNull Group node;
    private final @NonNull Path path2;
    private final @NonNull Path path1;

    private final MapAccessor<PersistentList<BezierNode>> bezierNodeListKey;
    private final boolean selectable;

    public BezierPathOutlineHandle(Figure figure, MapAccessor<PersistentList<BezierNode>> pointKey, boolean selectable) {
        super(figure);
        this.bezierNodeListKey = pointKey;
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
        final PersistentList<BezierNode> bezierNodes = getOwner().get(bezierNodeListKey);
        if (bezierNodes != null) {
            final Point2D p = drawingView.viewToWorld(x, y);
            return new BezierNodePath(bezierNodes).contains(p.getX(), p.getY(), tolerance);
        }
        return false;

    }


    @Override
    public Node getNode(@NonNull DrawingView view) {
        CssColor color = view.getEditor().getHandleColor();
        path1.setStroke(Color.WHITE);
        path2.setStroke(Paintable.getPaint(color));
        double strokeWidth = view.getEditor().getHandleStrokeWidth();
        path1.setStrokeWidth(strokeWidth + 2);
        path2.setStrokeWidth(strokeWidth);
        return node;
    }

    @Override
    public void updateNode(@NonNull DrawingView view) {
        Figure f = getOwner();
        final PersistentList<BezierNode> bezierNodes = f.get(bezierNodeListKey);
        if (bezierNodes == null) {
            path1.getElements().clear();
            path2.getElements().clear();
            return;
        }
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        List<PathElement> elements = new ArrayList<>();
        FXPathElementsBuilder builder = new FXPathElementsBuilder(elements);
        final BezierNodePath bnp = new BezierNodePath(bezierNodes);
        SvgPaths.buildFromPathIterator(builder, bnp.getPathIterator(FXShapes.awtTransformFromFX(t)));
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
