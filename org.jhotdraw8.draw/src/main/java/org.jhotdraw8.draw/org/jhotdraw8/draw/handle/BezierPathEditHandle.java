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
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.intersect.IntersectionPoint;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierNodePath;

public class BezierPathEditHandle extends BezierPathOutlineHandle {
    private final MapAccessor<ImmutableList<BezierNode>> pointKey;

    public BezierPathEditHandle(Figure figure, MapAccessor<ImmutableList<BezierNode>> pointKey) {
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

        addPoint.setOnAction(actionEvent -> {
            final ImmutableList<BezierNode> nodes = owner.get(pointKey);
            BezierNodePath path = nodes == null ? new BezierNodePath() : new BezierNodePath(nodes);
            Point2D pointInLocal = owner.worldToLocal(view.viewToWorld(event.getX(), event.getY()));
            IntersectionResult intersectionResultEx = path.pathIntersection(pointInLocal.getX(), pointInLocal.getY(), view.getEditor().getTolerance());// / view.getZoomFactor());// FIXME tolerance not
            if (!intersectionResultEx.intersections().isEmpty()) {
                IntersectionPoint intersectionPointEx = intersectionResultEx.intersections().get(0);
                int segment = intersectionPointEx.getSegmentA();
                path.getNodes().add(segment, new BezierNode(
                        pointInLocal));
                view.getModel().set(owner, pointKey, VectorList.copyOf(path.getNodes()));
                view.recreateHandles();
            }
        });


        contextMenu.getItems().add(addPoint);
        contextMenu.show(getNode(view), event.getScreenX(), event.getScreenY());
        event.consume();
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
