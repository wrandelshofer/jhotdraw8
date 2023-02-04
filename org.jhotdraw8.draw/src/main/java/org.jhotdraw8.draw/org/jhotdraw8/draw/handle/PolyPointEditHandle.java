/*
 * @(#)PolyPointEditHandle.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;

import java.util.function.Function;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;

/**
 * Handle for the point ofCollection a figure.
 *
 * @author Werner Randelshofer
 */
public class PolyPointEditHandle extends AbstractHandle {
    public static final @Nullable BorderStrokeStyle INSIDE_STROKE = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 1.0, 0, null);

    private static final @Nullable Background REGION_BACKGROUND = new Background(new BackgroundFill(Color.WHITE, null, null));
    private static final @Nullable Function<Color, Border> REGION_BORDER = color -> new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, null, null));
    private static final Rectangle REGION_SHAPE = new Rectangle(7, 7);
    private final @NonNull Region node;

    private Point2D pickLocation;
    private final int pointIndex;
    private final NonNullMapAccessor<ImmutableList<Point2D>> pointKey;

    public PolyPointEditHandle(Figure figure, NonNullMapAccessor<ImmutableList<Point2D>> pointKey, int pointIndex) {
        super(figure);
        this.pointKey = pointKey;
        this.pointIndex = pointIndex;
        node = new Region();
        node.setShape(REGION_SHAPE);
        node.setManaged(false);
        node.setScaleShape(true);
        node.setCenterShape(true);
        node.resize(11, 11);
        node.setBackground(REGION_BACKGROUND);
    }

    @Override
    public boolean contains(DrawingView dv, double x, double y, double tolerance) {
        Point2D p = getLocationInView();
        return Points.squaredDistance(x, y, p.getX(), p.getY()) <= tolerance * tolerance;
    }

    @Override
    public Cursor getCursor() {
        return Cursor.CROSSHAIR;
    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public @NonNull Region getNode(@NonNull DrawingView view) {
        double size = view.getEditor().getHandleSize();
        if (node.getWidth() != size) {
            node.resize(size, size);
        }
        CssColor color = view.getEditor().getHandleColor();
        node.setBorder(REGION_BORDER.apply(color.getColor()));
        return node;
    }


    @Override
    public void onMouseClicked(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (pointKey != null && event.getClickCount() == 2) {
            if (owner.get(pointKey).size() > 2) {
                removePoint(dv);
            }
        }
    }

    private void removePoint(@NonNull DrawingView dv) {
        dv.getModel().set(owner, pointKey, owner.getNonNull(pointKey).removeAt(pointIndex));
        dv.recreateHandles();
    }

    @Override
    public void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        Point2D newPoint = view.viewToWorld(new Point2D(event.getX(), event.getY()));

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control switches the constrainer off
            newPoint = view.getConstrainer().constrainPoint(getOwner(), new CssPoint2D(newPoint)).getConvertedValue();
        }

        ImmutableList<Point2D> list = owner.getNonNull(pointKey);
        view.getModel().set(getOwner(), pointKey, list.set(pointIndex, getOwner().worldToLocal(newPoint)));
    }

    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, dv);
        }
    }

    protected void onPopupTriggered(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addPoint = new MenuItem(DrawLabels.getResources().getString("handle.removePoint.text"));
        addPoint.setOnAction(actionEvent -> removePoint(dv));
        contextMenu.getItems().add(addPoint);
        contextMenu.show(node, event.getScreenX(), event.getScreenY());
        event.consume();
    }

    @Override
    public void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, dv);
        }
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void updateNode(@NonNull DrawingView view) {
        Figure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        ImmutableList<Point2D> list = f.get(pointKey);
        if (list == null || pointIndex > list.size()) {
            node.setVisible(false);
            return;
        }
        node.setVisible(true);
        Point2D p = list.get(pointIndex);
        pickLocation = p = FXTransforms.transform(t, p);
        double size = node.getWidth();
        node.relocate(p.getX() - size * 0.5, p.getY() - size * 0.5);
        // rotates the node:
        node.setRotate(f.getStyled(ROTATE));
        node.setRotationAxis(f.getStyled(ROTATION_AXIS));
    }

}
