/*
 * @(#)RotateHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.css.value.Paintable;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.TransformableFigure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.Angles;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_PIVOT;
import static org.jhotdraw8.draw.figure.TransformableFigure.SCALE_X;
import static org.jhotdraw8.draw.figure.TransformableFigure.SCALE_Y;
import static org.jhotdraw8.draw.figure.TransformableFigure.TRANSFORMS;
import static org.jhotdraw8.draw.figure.TransformableFigure.TRANSLATE_X;
import static org.jhotdraw8.draw.figure.TransformableFigure.TRANSLATE_Y;

/**
 * A Handle to rotate a TransformableFigure around the center of its bounds in
 * local.
 * <p>
 * This handle consists of a pick node, a pivot node and a line.
 * <p>
 * The pick node is displayed as a circle above the top of the figure.
 * <p>
 * The pivot node is displayed as a cross located at the center of the figure.
 * The pivot node is only visible when the mouse is pressed.
 * <p>
 * The line extends from the pick node to the top of the figure.
 * <p>
 *
 * <pre>
 *              ○             pick node
 *              |             line
 *     +-----------------+
 *     |                 |
 *     |        +        |    pivot node
 *     |                 |
 *     +-----------------+
 * </pre>
 */
public class RotateHandle extends AbstractHandle {
    public static final @Nullable BorderStrokeStyle INSIDE_STROKE = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 1.0, 0, null);

    private static final @Nullable Background HANDLE_REGION_BACKGROUND = new Background(new BackgroundFill(Color.WHITE, null, null));
    private static final @Nullable Border HANDLE_REGION_BORDER = new Border(new BorderStroke(Color.PURPLE, BorderStrokeStyle.SOLID, null, null));
    private static final Circle PICK_NODE_SHAPE = new Circle(3);
    private static final SVGPath PIVOT_NODE_SHAPE = new SVGPath();

    private static final @Nullable Background PIVOT_REGION_BACKGROUND = new Background(new BackgroundFill(Color.PURPLE, null, null));
    private static final @Nullable Border PIVOT_REGION_BORDER = null;

    static {
        PIVOT_NODE_SHAPE.setContent("M-5,-1 L -1,-1 -1,-5 1,-5 1,-1 5,-1 5 1 1,1 1,5 -1,5 -1,1 -5,1 Z");
    }

    private final Group group;

    private Set<Figure> groupReshapeableFigures;
    private final Line line;
    private double lineLength = 10.0;
    private Point2D pickLocation;
    private Point2D pivotLocation;
    private final Region pickNode;
    private final Region pivotNode;

    public RotateHandle(TransformableFigure figure) {
        super(figure);
        group = new Group();
        pickNode = new Region();
        pickNode.setShape(PICK_NODE_SHAPE);
        pickNode.setManaged(false);
        pickNode.setScaleShape(true);
        pickNode.setCenterShape(true);
        pickNode.resize(11, 11); // size must be odd
        pickNode.getStyleClass().clear();
        pickNode.setBorder(HANDLE_REGION_BORDER);
        pickNode.setBackground(HANDLE_REGION_BACKGROUND);

        pivotNode = new Region();
        pivotNode.setShape(PIVOT_NODE_SHAPE);
        pivotNode.setManaged(false);
        pivotNode.setScaleShape(true);
        pivotNode.setCenterShape(true);
        pivotNode.resize(11, 11); // size must be odd
        pivotNode.getStyleClass().clear();
        pivotNode.setBorder(PIVOT_REGION_BORDER);
        pivotNode.setBackground(PIVOT_REGION_BACKGROUND);
        pivotNode.setVisible(false);

        line = new Line();
        line.getStyleClass().clear();
        group.getChildren().addAll(line, pickNode, pivotNode);
    }

    @Override
    public boolean contains(DrawingView dv, double x, double y, double tolerance) {
        Point2D p = getLocationInView();
        if (p == null) return false;
        return Points.squaredDistance(x, y, p.getX(), p.getY()) <= tolerance * tolerance;
    }

    @Override
    public Cursor getCursor() {
        return Cursor.CROSSHAIR;
    }

    public @Nullable Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public Group getNode(DrawingView view) {
        double size = view.getEditor().getHandleSize();
        lineLength = size * 1.5;
        if (pickNode.getWidth() != size) {
            pickNode.resize(size, size);
        }
        Paint color = Paintable.getPaint(view.getEditor().getHandleColor());
        line.setStroke(color);
        BorderStroke borderStroke = pickNode.getBorder().getStrokes().getFirst();
        if (borderStroke == null || !borderStroke.getTopStroke().equals(color)) {
            Border border = new Border(
                    new BorderStroke(color, INSIDE_STROKE, null, null)
            );
            pickNode.setBorder(border);
            pivotNode.setBorder(border);
        }
        return group;
    }

    @Override
    public TransformableFigure getOwner() {
        return (TransformableFigure) super.getOwner();
    }

    private Transform computeParentToLocal(boolean styled) {
        Transform p2l = null;
        final Bounds layoutBounds = owner.getLayoutBounds();
        Point2D center = FXRectangles.center(layoutBounds);

        double sx = styled ? owner.getStyledNonNull(SCALE_X) : owner.getNonNull(SCALE_X);
        double sy = styled ? owner.getStyledNonNull(SCALE_Y) : owner.getNonNull(SCALE_Y);
        double r = styled ? owner.getStyledNonNull(ROTATE) : owner.getNonNull(ROTATE);
        double tx = styled ? owner.getStyledNonNull(TRANSLATE_X) : owner.getNonNull(TRANSLATE_X);
        double ty = styled ? owner.getStyledNonNull(TRANSLATE_Y) : owner.getNonNull(TRANSLATE_Y);

        Transform transform = getOwner().getTransform(styled);
        if (tx != 0.0 || ty != 0.0) {
            Translate tt = new Translate(-tx, -ty);
            p2l = tt;
        }
        if (r != 0) {
            CssPoint2D cssPivot = owner.getStyledNonNull(ROTATION_PIVOT);
            Point2D pivot = CssPoint2D.getPointInBounds(cssPivot, layoutBounds);
            Point2D transformedPivot = transform.transform(pivot);
            Rotate tr = new FXPreciseRotate(-r, transformedPivot.getX(), transformedPivot.getY());
            //  p2l = FXTransforms.concat(tr, p2l);
        }
        if ((sx != 1.0 || sy != 1.0) && sx != 0.0 && sy != 0.0) {// check for 0.0 avoids creating a non-invertible transform
            Scale ts = new Scale(1 / sx, 1 / sy, center.getX(), center.getY());
            p2l = FXTransforms.concat(ts, p2l);
        }
        if (!transform.isIdentity()) {
            try {
                p2l = FXTransforms.concat(transform.createInverse(), p2l);
            } catch (NonInvertibleTransformException e) {
                // bail
            }
        }
        if (p2l == null) {
            p2l = FXTransforms.IDENTITY;
        }
        return p2l;
    }

    private @Nullable Transform getWorldToRotate() {
        TransformableFigure o = getOwner();
        if (true) {
            Transform t = null;
            t = computeParentToLocal(false);
            final Figure parent = o.getParent();
            t = parent == null ? t : FXTransforms.concat(t, parent.getWorldToLocal());
            return t;
        }

        Bounds boundsInLocal = o.getBoundsInLocal();
        double cx = boundsInLocal.getCenterX();
        double cy = boundsInLocal.getCenterX();
        Transform invTranslate = Transform.translate(-o.getNonNull(TRANSLATE_X), -o.getNonNull(TRANSLATE_Y));
        Transform invScale = Transform.scale(1.0 / o.getNonNull(SCALE_X), 1.0 / o.getNonNull(SCALE_Y), cx, cy);
        Transform t = null;
        t = FXTransforms.concat(t, invTranslate);
        t = FXTransforms.concat(t, invScale);
        for (Transform tt : o.getNonNull(TRANSFORMS).readableReversed()) {
            try {
                t = FXTransforms.concat(t, tt.createInverse());
            } catch (NonInvertibleTransformException e) {
                //bail
            }
        }
        t = FXTransforms.concat(t, o.getWorldToParent());
        return t;
    }

    @Override
    public void onMouseDragged(MouseEvent event, DrawingView view) {
        TransformableFigure o = getOwner();

        Transform worldToRotate = FXTransforms.concat(getWorldToRotate(), view.getViewToWorld());
        Point2D localDragLocation = worldToRotate.transform(event.getX(), event.getY());
        Point2D localPivotLocation = worldToRotate.transform(pivotLocation);
        double newRotate = 90 + Math.toDegrees(Angles.angle(localPivotLocation.getX(), localPivotLocation.getY(), localDragLocation.getX(), localDragLocation.getY()));
        newRotate = newRotate % 360;
        if (newRotate < 0) {
            newRotate += 360;
        }

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control turns the constrainer off
            newRotate = view.getConstrainer().constrainAngle(getOwner(), newRotate);
        }
        if (event.isMetaDown()) {
            // meta snaps the location of the handle to the grid
        }

        DrawingModel model = view.getModel();
        if (event.isShiftDown()) {
            // shift transforms all selected figures
            for (Figure f : groupReshapeableFigures) {
                if (f instanceof TransformableFigure) {
                    model.set(f, TransformableFigure.ROTATE, newRotate);
                }
            }
        } else {
            model.set(getOwner(), TransformableFigure.ROTATE, newRotate);
        }
    }

    @Override
    public void onMousePressed(MouseEvent event, DrawingView view) {
        pivotNode.setVisible(true);
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
    public void onMouseReleased(MouseEvent event, DrawingView dv) {
        pivotNode.setVisible(false);
        // FIXME fireDrawingModelEvent undoable edit event
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void updateNode(DrawingView view) {
        TransformableFigure o = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), o.getLocalToWorld());
        Bounds b = o.getLayoutBounds();

        double size = pickNode.getWidth();
        Point2D pivot = o.get(ROTATION_PIVOT).getConvertedValue();
        pickLocation = t.transform(b.getCenterX(), b.getMinY());
        Point2D pickDelta = t.deltaTransform(0, 1).normalize().multiply(size * -2);
        pickLocation = pickLocation.add(pickDelta);
        pivotLocation = t.transform(b.getMinX() + b.getWidth() * pivot.getX(), b.getMinY() + b.getHeight() * pivot.getY());

        pickNode.relocate(pickLocation.getX() - size * 0.5, pickLocation.getY() - size * 0.5);
        pivotNode.relocate(pivotLocation.getX() - size * 0.5, pivotLocation.getY() - size * 0.5);
        pivotNode.setRotate(o.get(ROTATE));

        Point2D northLocation = t.transform(b.getCenterX(), b.getMinY());
        line.setStartX(pickLocation.getX());
        line.setStartY(pickLocation.getY());
        line.setEndX(northLocation.getX());
        line.setEndY(northLocation.getY());
    }

}
