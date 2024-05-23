/*
 * @(#)PathIterableOutlineHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Transform;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.PathIterableFigure;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws an outline of the path of a {@link PathIterableFigure}.
 *
 * @author Werner Randelshofer
 */
public class PathIterableOutlineHandle extends AbstractHandle {

    private final Group node;
    private final Path path2;
    private final Path path1;
    private final boolean selectable;

    public PathIterableOutlineHandle(PathIterableFigure figure, boolean selectable) {
        super(figure);
        node = new Group();
        path2 = new Path();
        path1 = new Path();
        node.getChildren().addAll(path1, path2);
        this.selectable = selectable;
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
        path1.setStroke(Color.WHITE);
        path2.setStroke(Paintable.getPaint(color));
        double strokeWidth = view.getEditor().getHandleStrokeWidth();
        path1.setStrokeWidth(strokeWidth + 2);
        path2.setStrokeWidth(strokeWidth);
        return node;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isSelectable() {
        return selectable;
    }

    @Override
    public PathIterableFigure getOwner() {
        return (PathIterableFigure) super.getOwner();
    }

    @Override
    public void updateNode(DrawingView view) {
        PathIterableFigure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        List<PathElement> elements = new ArrayList<>();
        FXPathElementsBuilder builder = new FXPathElementsBuilder(elements);
        AwtShapes.buildPathIterator(builder, f.getPathIterator(view, FXShapes.fxTransformToAwtTransform(t)));
        path1.getElements().setAll(elements);
        path2.getElements().setAll(elements);
    }

}
