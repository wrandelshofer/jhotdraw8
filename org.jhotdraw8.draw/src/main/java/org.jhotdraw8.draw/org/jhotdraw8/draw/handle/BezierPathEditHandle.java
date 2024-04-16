/*
 * @(#)BezierPathEditHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.undo.RecreateHandlesEdit;
import org.jhotdraw8.fxbase.undo.CompositeEdit;
import org.jhotdraw8.fxbase.undo.FXUndoManager;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.CubicCurves;
import org.jhotdraw8.geom.QuadCurves;
import org.jhotdraw8.geom.intersect.IntersectionPoint;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;

public class BezierPathEditHandle extends BezierPathOutlineHandle {
    private final @NonNull MapAccessor<BezierPath> pointKey;

    public BezierPathEditHandle(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> pointKey) {
        super(figure, pointKey, true);
        this.pointKey = pointKey;
    }

    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    private void onPopupTriggered(@NonNull MouseEvent event, @NonNull DrawingView view) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addPoint = new MenuItem(DrawLabels.getResources().getString("handle.addPoint.text"));

        addPoint.setOnAction(actionEvent -> addPoint(event, view));
        contextMenu.getItems().add(addPoint);
        contextMenu.show(getNode(view), event.getScreenX(), event.getScreenY());
        event.consume();
    }

    private void addPoint(@NonNull MouseEvent event, @NonNull DrawingView view) {
        BezierPath path = owner.get(pointKey);
        if (path == null) {
            path = BezierPath.of();
        }
        Point2D pointInLocal = owner.worldToLocal(view.viewToWorld(event.getX(), event.getY()));
        IntersectionResult intersectionResultEx = path.pathIntersection(pointInLocal.getX(), pointInLocal.getY(), view.getEditor().getTolerance());// / view.getZoomFactor());// FIXME tolerance not
        if (!intersectionResultEx.intersections().isEmpty()) {
            IntersectionPoint intersectionPointEx = intersectionResultEx.intersections().get(0);
            int segment = intersectionPointEx.segmentA();
            BezierNode newNode = new BezierNode(intersectionPointEx.getX(), intersectionPointEx.getY());
            if (segment > 0 && path.get(segment - 1).isClosePath()) {
                path = path.set(segment - 1, path.get(segment - 1).withMaskBitsClears(BezierNode.CLOSE_MASK));
                newNode = newNode.withMaskBitsSet(BezierNode.CLOSE_MASK);
            }
            int inNodeIndex = (path.size() + segment - 1) % path.size();
            BezierNode inNode = path.get(inNodeIndex);
            int outNodeIndex = (path.size() + segment) % path.size();
            BezierNode outNode = path.get(outNodeIndex);
            if (inNode.hasOut() && outNode.hasIn()) {
                // split cubic curve
                double[] split = new double[8 + 6];
                CubicCurves.split(new double[]{inNode.pointX(), inNode.pointY(), inNode.outX(), inNode.outY(), outNode.inX(), outNode.inY(), outNode.pointX(), outNode.pointY()},
                        0,
                        intersectionPointEx.argumentA(), split, 0, split, 6);
                inNode = inNode.withOut(split[2], split[3]);
                newNode = newNode.withIn(split[4], split[5]).withOut(split[8], split[9]).withMaskBitsSet(BezierNode.IN_OUT_MASK);
                outNode = outNode.withIn(split[10], split[11]);

            } else if (inNode.hasOut()) {
                // split quadratic curve
                double[] split = new double[6 + 4];
                QuadCurves.split(new double[]{inNode.pointX(), inNode.pointY(), inNode.outX(), inNode.outY(), outNode.pointX(), outNode.pointY()},
                        0,
                        intersectionPointEx.argumentA(), split, 0, split, 4);
                inNode = inNode.withOut(split[2], split[3]);
                newNode = newNode.withOut(split[6], split[7]).withMaskBitsSet(BezierNode.OUT_MASK);
            } else if (outNode.hasIn()) {
                // split quadratic curve
                double[] split = new double[6 + 4];
                QuadCurves.split(new double[]{inNode.pointX(), inNode.pointY(), outNode.inX(), outNode.inY(), outNode.pointX(), outNode.pointY()},
                        0,
                        intersectionPointEx.argumentA(), split, 0, split, 4);
                newNode = newNode.withIn(split[2], split[3]).withMaskBitsSet(BezierNode.IN_MASK);
                outNode = outNode.withIn(split[6], split[7]);
            }
            path = path.set(inNodeIndex, inNode).set(outNodeIndex, outNode)
                    .add(segment, newNode);

            CompoundEdit compoundEdit = new CompositeEdit(DrawLabels.getResources().getString("handle.addPoint.text"));
            FXUndoManager undoManager = view.getEditor().getUndoManager();
            undoManager.undoableEditHappened(new UndoableEditEvent(this, compoundEdit));
            undoManager.undoableEditHappened(new UndoableEditEvent(this, new RecreateHandlesEdit(view)));
            view.getModel().set(owner, pointKey, path);
            view.recreateHandles();
            undoManager.undoableEditHappened(new UndoableEditEvent(this, new RecreateHandlesEdit(view)));
            undoManager.undoableEditHappened(new UndoableEditEvent(this, compoundEdit));
        }
    }


    @Override
    public void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isEditable() {
        return true;
    }
}
