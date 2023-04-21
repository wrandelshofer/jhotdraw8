/*
 * @(#)PolyPointMoveHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;

/**
 * Handle for moving (translating) a figure.
 *
 * @author Werner Randelshofer
 */
public class PolyPointMoveHandle extends AbstractHandle {

    private static final @Nullable Function<Color, Background> REGION_BACKGROUND = color -> new Background(new BackgroundFill(color, null, null));
    private static final @Nullable Function<Color, Border> REGION_BORDER = color -> new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, null, null));
    private static final Rectangle REGION_SHAPE = new Rectangle(5, 5);
    private Set<Figure> groupReshapeableFigures;
    private final @NonNull Region node;
    private Point2D oldPoint;
    private Point2D pickLocation;
    private final int pointIndex;
    private final MapAccessor<ImmutableList<Point2D>> pointKey;

    public PolyPointMoveHandle(Figure figure, MapAccessor<ImmutableList<Point2D>> pointKey, int pointIndex) {
        super(figure);
        this.pointKey = pointKey;
        this.pointIndex = pointIndex;
        node = new Region();
        node.setShape(REGION_SHAPE);
        node.setManaged(false);
        node.setScaleShape(true);
        node.setCenterShape(true);
        node.resize(11, 11);
    }

    @Override
    public boolean contains(DrawingView dv, double x, double y, double tolerance) {
        Point2D p = getLocationInView();
        return Points.squaredDistance(x, y, p.getX(), p.getY()) <= tolerance * tolerance;
    }

    @Override
    public Cursor getCursor() {
        return Cursor.OPEN_HAND;
    }

    private Point2D getLocation() {
        ImmutableList<Point2D> list = owner.get(pointKey);
        return list.get(pointIndex);

    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public @NonNull Region getNode(@NonNull DrawingView view) {
        double size = view.getEditor().getHandleSize();
        node.resize(size, size);
        CssColor color = view.getEditor().getHandleColor();
        node.setBorder(REGION_BORDER.apply(Color.WHITE));
        node.setBackground(REGION_BACKGROUND.apply(color.getColor()));
        return node;
    }

    @Override
    public void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        Point2D newPoint = view.viewToWorld(new Point2D(event.getX(), event.getY()));

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control turns the constrainer off
            newPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(newPoint)).getConvertedValue();
        }

        if (event.isMetaDown()) {
            // meta snaps the location ofCollection the handle to the grid
            Point2D loc = getLocation();
            oldPoint = FXTransforms.transform(owner.getLocalToWorld(), loc);
        }

        if (oldPoint.equals(newPoint)) {
            return;
        }

        //Transform tx = Transform.translate(newPoint.getX() - oldPoint.getX(), newPoint.getY() - oldPoint.getY());
        DrawingModel model = view.getModel();

        if (event.isShiftDown()) {
            // shift transforms all selected figures
            for (Figure f : groupReshapeableFigures) {
                translateFigure(f, oldPoint, newPoint, model);
            }
        } else {
            Figure f = owner;
            translateFigure(f, oldPoint, newPoint, model);
        }
        oldPoint = newPoint;
    }

    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        oldPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(view.viewToWorld(new Point2D(event.getX(), event.getY())))).getConvertedValue();

        // determine which figures can be reshaped together as a group
        Set<Figure> selectedFigures = view.getSelectedFigures();
        groupReshapeableFigures = new HashSet<>();
        for (Figure f : view.getSelectedFigures()) {
            if (f.isGroupReshapeableWith(selectedFigures)) {
                groupReshapeableFigures.add(f);
            }
        }
        groupReshapeableFigures = view.getFiguresWithCompatibleHandle(groupReshapeableFigures, this);
    }

    @Override
    public void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        // FIXME fireDrawingModelEvent undoable edit
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void updateNode(@NonNull DrawingView view) {
        Figure f = owner;
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        Bounds b = f.getLayoutBounds();
        Point2D p = getLocation();
        //Point2D p = unconstrainedPoint!=null?unconstrainedPoint:f.get(pointKey);
        pickLocation = p = FXTransforms.transform(t, p);

        // The node is centered around the location.
        double size = node.getWidth();
        node.relocate(p.getX() - size * 0.5, p.getY() - size * 0.5);

        // rotates the node:
        node.setRotate(f.getStyled(ROTATE));
        node.setRotationAxis(f.getStyled(ROTATION_AXIS));
    }

    /**
     * Translates the specified figure, given the old and new position ofCollection a
     * point.
     *
     * @param f        the figure to be translated
     * @param oldPoint oldPoint in world coordinates
     * @param newPoint newPoint in world coordinates
     * @param model    the drawing model
     */
    public static void translateFigure(@NonNull Figure f, @NonNull Point2D oldPoint, @NonNull Point2D newPoint, @Nullable DrawingModel model) {
        Point2D npl = f.worldToParent(newPoint);
        Point2D opl = f.worldToParent(oldPoint);
        Transform tx = Transform.translate(npl.getX() - opl.getX(), npl.getY() - opl.getY());
        if (model != null) {
            model.reshapeInParent(f, tx);
        } else {
            f.reshapeInParent(tx);
        }
    }
}
