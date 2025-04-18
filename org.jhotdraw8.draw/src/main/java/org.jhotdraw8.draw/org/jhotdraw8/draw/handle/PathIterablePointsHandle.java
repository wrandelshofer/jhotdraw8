/*
 * @(#)PathIterablePointsHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.Paintable;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.PathIterableFigure;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.FXPathPointsBuilder;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws points of the path of a {@link PathIterableFigure}.
 * <p>
 * Does not provide interactions. Just increases the hit area of the figure.
 *
 */
public class PathIterablePointsHandle extends AbstractHandle {

    private final Path node;
    private final boolean selectable;

    public PathIterablePointsHandle(PathIterableFigure figure, boolean selectable) {
        super(figure);
        node = new Path();
        initNode(node);
        this.selectable = selectable;
    }

    @Override
    public boolean contains(DrawingView dv, double x, double y, double tolerance) {
        return node.contains(x, y);
    }

    @Override
    public @Nullable Cursor getCursor() {
        return null;
    }

    @Override
    public Node getNode(DrawingView view) {
        CssColor color = view.getEditor().getHandleColor();
        node.setStroke(Paintable.getPaint(color));
        return node;
    }

    protected void initNode(Path r) {
        r.setFill(null);
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
        FXPathPointsBuilder builder = new FXPathPointsBuilder(view.getEditor().getHandleSize(), elements);
        AwtShapes.buildPathIterator(builder, f.getPathIterator(view, FXShapes.fxTransformToAwtTransform(t)));

        node.getElements().setAll(builder.getElements());
    }

}
