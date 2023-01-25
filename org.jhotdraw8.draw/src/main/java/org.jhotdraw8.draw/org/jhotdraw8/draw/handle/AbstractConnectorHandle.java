/*
 * @(#)AbstractConnectorHandle.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.ConnectableFigure;
import org.jhotdraw8.draw.figure.ConnectingFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.Geom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handle for the start or end point of a connection figure.
 * <p>
 * Pressing the alt or the control key while dragging the handle prevents
 * connecting the point.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractConnectorHandle extends AbstractHandle {
    protected static class ConnectorAndConnectedFigure {
        final @NonNull Connector connector;
        final @NonNull Figure connectedFigure;

        public ConnectorAndConnectedFigure(@NonNull Connector connector, @NonNull Figure connectedFigure) {
            this.connector = connector;
            this.connectedFigure = connectedFigure;
        }

        public Connector getConnector() {
            return connector;
        }

        public Figure getConnectedFigure() {
            return connectedFigure;
        }
    }

    protected final @NonNull MapAccessor<Connector> connectorKey;
    protected final NonNullMapAccessor<CssPoint2D> pointKey;
    protected final @NonNull MapAccessor<Figure> targetKey;
    protected @Nullable Point2D connectorLocation;
    protected Point2D pickLocation;
    // private final Region connectorNode;
    // private final javafx.scene.Group groupNode;
    private boolean isConnected;
    private boolean isDragging;
    private boolean editable = true;
    private @Nullable Figure prevTarget;

    public AbstractConnectorHandle(@NonNull ConnectingFigure figure,
                                   NonNullMapAccessor<CssPoint2D> pointKey,
                                   @NonNull MapAccessor<Connector> connectorKey,
                                   @NonNull MapAccessor<Figure> targetKey) {
        super(figure);
        this.pointKey = pointKey;
        this.connectorKey = connectorKey;
        this.targetKey = targetKey;

        isConnected = figure.get(connectorKey) != null && figure.get(targetKey) != null;
    }

    @Override
    public boolean contains(DrawingView dv, double x, double y, double tolerance) {
        boolean b = false;
        if (connectorLocation != null) {
            b = Geom.squaredDistance(x, y, connectorLocation.getX(), connectorLocation.getY()) <= tolerance * tolerance;
        }
        if (!b && pickLocation != null) {
            b = Geom.squaredDistance(x, y, pickLocation.getX(), pickLocation.getY()) <= tolerance * tolerance;
        }
        return b;
    }

    @Override
    public Cursor getCursor() {
        return isDragging ? Cursor.CLOSED_HAND : Cursor.HAND;
    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public @NonNull ConnectingFigure getOwner() {
        return (ConnectingFigure) super.getOwner();
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (!editable) {
            return;
        }
        isDragging = true;
        Point2D pointInView = new Point2D(event.getX(), event.getY());
        Point2D unconstrainedPointInWorld = view.viewToWorld(pointInView);
        double tolerance = view.getViewToWorld().deltaTransform(view.getEditor().getTolerance(), 0).getX();

        CssPoint2D constrainedPointInWorld;
        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control turns the constrainer off
            constrainedPointInWorld = view.getConstrainer().constrainPoint(owner, new CssPoint2D(unconstrainedPointInWorld));
        } else {
            constrainedPointInWorld = new CssPoint2D(unconstrainedPointInWorld);
        }

        ConnectingFigure o = getOwner();
        Connector newConnector = null;
        Figure newConnectedFigure = null;
        isConnected = false;
        // must clear end target, otherwise findConnector won't work as expected
        DrawingModel model = view.getModel();
        model.set(o, targetKey, null);
        // Meta prevents connection
        if (!event.isMetaDown()) {
            // Shift prevents search for another target figure
            if (event.isShiftDown() && prevTarget != null) {
                newConnectedFigure = prevTarget;
                ConnectableFigure cff = (ConnectableFigure) prevTarget;
                final ConnectorAndConnectedFigure connectorAndConnectedFigure = find(constrainedPointInWorld, o, cff, event, tolerance);
                newConnector = connectorAndConnectedFigure == null ? null : connectorAndConnectedFigure.getConnector();
                if (newConnector != null && o.canConnect(cff, newConnector)) {
                    newConnectedFigure = connectorAndConnectedFigure.getConnectedFigure();
                    constrainedPointInWorld = new CssPoint2D(newConnector.getPointAndTangentInLocal(o, cff).getPoint(Point2D::new));
                    isConnected = true;
                }
            } else {
                List<Figure> list = view.findFigures(pointInView, true)
                        .stream().map(Map.Entry::getKey).collect(Collectors.toList());//front to back

                double closestDistanceSq = Double.POSITIVE_INFINITY;
                SearchLoop:
                for (int i = list.size() - 1; i >= 0; i--) {
                    Figure f1 = list.get(i);
                    for (Figure ff : f1.breadthFirstIterable()) {//back to front NOOO
                        if (this.owner != ff && (ff instanceof ConnectableFigure)) {
                            ConnectableFigure cff = (ConnectableFigure) ff;
                            Point2D pointInLocal = cff.worldToLocal(unconstrainedPointInWorld);
                            if (ff.getBoundsInLocal().contains(pointInLocal)) {
                                final ConnectorAndConnectedFigure candidate = find(constrainedPointInWorld, o, cff, event, tolerance);
                                final Connector candidateConnector = candidate == null ? null : candidate.getConnector();
                                if (candidateConnector != null && o.canConnect(ff, newConnector)) {
                                    Point2D p = candidate.getConnector().getPointAndTangentInWorld(owner, candidate.getConnectedFigure()).getPoint(Point2D::new);
                                    double distanceSq = FXGeom.distanceSq(p, unconstrainedPointInWorld);
                                    if (distanceSq <= closestDistanceSq) {
                                        // we compare <= because we go back to front, and the
                                        // front-most figure wins
                                        closestDistanceSq = distanceSq;
                                        newConnectedFigure = candidate.getConnectedFigure();
                                        newConnector = candidateConnector;
                                        isConnected = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        model.set(o, pointKey, owner.worldToLocal(constrainedPointInWorld));
        model.set(o, connectorKey, newConnector);
        model.set(o, targetKey, newConnectedFigure);
    }

    protected ConnectorAndConnectedFigure find(CssPoint2D pointInWorld, ConnectingFigure o, ConnectableFigure cff, MouseEvent mouseEvent,
                                               double tolerance) {
        final Connector connector = cff.findConnector(cff.worldToLocal(pointInWorld.getConvertedValue()), o, tolerance);
        return connector == null ? null : new ConnectorAndConnectedFigure(connector, cff);
    }

    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
        prevTarget = owner.get(targetKey);
    }

    /**
     * This method is called when the popup menu is triggered.
     * This implementation does nothing.
     *
     * @param event the mouse event
     * @param view  the drawing view
     */
    protected void onPopupTriggered(MouseEvent event, DrawingView view) {
    }

    @Override
    public void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
        isDragging = false;
    }

}
