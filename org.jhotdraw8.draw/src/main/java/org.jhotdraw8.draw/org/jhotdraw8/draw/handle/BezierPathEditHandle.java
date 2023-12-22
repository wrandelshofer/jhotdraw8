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
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.intersect.IntersectionPoint;
import org.jhotdraw8.geom.intersect.IntersectionResult;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierNodePath;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.util.List;

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

        addPoint.setOnAction(actionEvent -> addPoint(event, view));
        contextMenu.getItems().add(addPoint);
        contextMenu.show(getNode(view), event.getScreenX(), event.getScreenY());
        event.consume();
    }

    private void addPoint(@NonNull MouseEvent event, @NonNull DrawingView view) {
        final ImmutableList<BezierNode> nodes = owner.get(pointKey);
        BezierNodePath path = nodes == null ? new BezierNodePath() : new BezierNodePath(nodes);
        List<BezierNode> pathNodes = path.getNodes();

        Point2D pointInLocal = owner.worldToLocal(view.viewToWorld(event.getX(), event.getY()));
        IntersectionResult intersectionResultEx = path.pathIntersection(pointInLocal.getX(), pointInLocal.getY(), view.getEditor().getTolerance());// / view.getZoomFactor());// FIXME tolerance not
        if (!intersectionResultEx.intersections().isEmpty()) {
            IntersectionPoint intersectionPointEx = intersectionResultEx.intersections().get(0);
            int segment = intersectionPointEx.getSegmentA();
            BezierNode newNode = new BezierNode(intersectionPointEx.getX(), intersectionPointEx.getY());
            if (segment > 0 && nodes.get(segment - 1).isClosePath()) {
                pathNodes.set(segment - 1, pathNodes.get(segment - 1).withClearMaskBits(BezierNode.CLOSE_MASK));
                newNode = newNode.withMaskBits(BezierNode.CLOSE_MASK);
            }
            pathNodes.add(segment, newNode);
            view.getModel().set(owner, pointKey, VectorList.copyOf(pathNodes));
            view.recreateHandles();
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
