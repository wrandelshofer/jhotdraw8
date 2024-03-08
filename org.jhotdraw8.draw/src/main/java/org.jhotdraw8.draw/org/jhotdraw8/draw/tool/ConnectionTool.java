/*
 * @(#)ConnectionTool.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.ConnectableFigure;
import org.jhotdraw8.draw.figure.ConnectingFigure;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.figure.LayerFigure;
import org.jhotdraw8.draw.figure.LineConnectionFigure;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.model.DrawingModel;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ConnectionTool.
 *
 * @author Werner Randelshofer
 */
public class ConnectionTool extends AbstractTool {

    /**
     * The created figure.
     */
    private @Nullable ConnectingFigure figure;

    private @NonNull Supplier<ConnectingFigure> figureFactory;
    private final @NonNull Supplier<Layer> layerFactory;

    private @Nullable HandleType handleType = null;

    public ConnectionTool(@NonNull String name, @Nullable Resources rsrc, @NonNull Supplier<ConnectingFigure> figureFactory) {
        this(name, rsrc, figureFactory, LayerFigure::new);
    }

    public ConnectionTool(@NonNull String name, @Nullable Resources rsrc,
                          @NonNull Supplier<ConnectingFigure> figureFactory,
                          @NonNull Supplier<Layer> layerFactory) {
        this(name, rsrc, null, figureFactory, layerFactory);

    }

    public ConnectionTool(@NonNull String name, @Nullable Resources rsrc, @Nullable HandleType handleType,
                          @NonNull Supplier<ConnectingFigure> figureFactory,
                          @NonNull Supplier<Layer> layerFactory) {
        super(name, rsrc);
        this.handleType = handleType;
        this.figureFactory = figureFactory;
        this.layerFactory = layerFactory;
    }

    public void setFactory(Supplier<ConnectingFigure> factory) {
        this.figureFactory = factory;
    }

    /**
     * Finds a layer for the specified figure. Creates a new layer if no
     * suitable layer can be found.
     *
     * @param dv        the drawing view
     * @param newFigure the figure
     * @return a suitable layer for the figure
     */
    protected @Nullable Figure getOrCreateParent(@NonNull DrawingView dv, @NonNull Figure newFigure) {
        // try to use the active layer
        Drawing drawing = dv.getDrawing();
        if (drawing == null) {
            return null;
        }
        Figure activeParent = dv.getActiveParent();
        if (activeParent != null && activeParent.isEditable() && activeParent.isAllowsChildren()) {
            return activeParent;
        }
        // search for a suitable layer front to back
        Layer layer = null;
        var layers = dv.getDrawing().getChildren();
        for (int i = layers.size() - 1; i >= 0; i--) {
            Figure candidate = layers.get(i);
            if (candidate.isEditable() && candidate.isAllowsChildren()
                    && candidate.isSuitableChild(newFigure)
                    && newFigure.isSuitableParent(candidate)
            ) {
                layer = (Layer) candidate;
                break;
            }
        }
        // create a new layer if necessary
        if (layer == null) {
            layer = layerFactory.get();
            dv.getModel().addChildTo(layer, drawing);
        }
        return layer;
    }

    @Override
    protected void onMouseClicked(@NonNull MouseEvent event, @NonNull DrawingView dv) {
    }

    @Override
    protected void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (figure != null) {
            Point2D pointInViewCoordinates = new Point2D(event.getX(), event.getY());
            Point2D unconstrainedPoint = view.viewToWorld(pointInViewCoordinates);
            Point2D constrainedPoint;
            if (!event.isAltDown() && !event.isControlDown()) {
                // alt or control turns the constrainer off
                constrainedPoint = view.getConstrainer().constrainPoint(figure, new CssPoint2D(unconstrainedPoint)).getConvertedValue();
            } else {
                constrainedPoint = unconstrainedPoint;
            }
            double tolerance = view.getViewToWorld().transform(view.getEditor().getTolerance(), 0).getX();
            Connector newConnector = null;
            Figure newConnectionTarget = null;
            DrawingModel model = view.getModel();
            // must clear end target, otherwise findConnector won't work as expected
            model.set(figure, LineConnectionFigure.END_TARGET, null);
            if (!event.isMetaDown()) {
                List<Figure> list = view.findFigures(pointInViewCoordinates, true)
                        .stream().map(Map.Entry::getKey).toList();
                SearchLoop:
                for (Figure f1 : list) {
                    for (Figure ff : f1.breadthFirstIterable()) {
                        if (figure != ff && (ff instanceof ConnectableFigure cff)) {
                            Point2D pointInLocal = cff.worldToLocal(unconstrainedPoint);
                            if (ff.getLayoutBounds().contains(pointInLocal)) {
                                newConnector = cff.findConnector(cff.worldToLocal(constrainedPoint), figure, tolerance);
                                if (newConnector != null && figure.canConnect(ff, newConnector)) {
                                    newConnectionTarget = ff;
                                    break SearchLoop;
                                }
                            }
                        }
                    }
                }
            }

            model.set(figure, LineConnectionFigure.END, new CssPoint2D(figure.worldToLocal(constrainedPoint)));
            model.set(figure, LineConnectionFigure.END_CONNECTOR, newConnector);
            model.set(figure, LineConnectionFigure.END_TARGET, newConnectionTarget);
        }
        event.consume();
    }

    @Override
    protected void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        requestFocus();
        figure = figureFactory.get();
        if (figure == null) {
            return;
        }
        Figure parent = getOrCreateParent(view, figure);
        if (parent == null) {
            figure = null;
            return;
        }
        if (handleType != null) {
            view.getEditor().setHandleType(handleType);
        }
        Point2D pointInViewCoordinates = new Point2D(event.getX(), event.getY());
        Point2D unconstrainedPoint = parent.worldToLocal(view.viewToWorld(pointInViewCoordinates));
        Point2D constrainedPoint = view.getConstrainer().constrainPoint(figure, new CssPoint2D(unconstrainedPoint)).getConvertedValue();
        figure.reshapeInLocal(constrainedPoint.getX(), constrainedPoint.getY(), 1, 1);
        DrawingModel dm = view.getModel();
        double tolerance = view.getViewToWorld().transform(view.getEditor().getTolerance(), 0).getX();

        view.setActiveParent(parent);

        Connector newConnector = null;
        Figure newConnectedFigure = null;
        if (!event.isMetaDown()) {
            List<Figure> list = view.findFigures(pointInViewCoordinates, true)
                    .stream().map(Map.Entry::getKey).toList();

            SearchLoop:
            for (Figure f1 : list) {
                for (Figure ff : f1.breadthFirstIterable()) {
                    if (figure != ff && (ff instanceof ConnectableFigure cff)) {
                        Point2D pointInLocal = cff.worldToLocal(unconstrainedPoint);
                        if (ff.getLayoutBounds().contains(pointInLocal)) {
                            newConnector = cff.findConnector(cff.worldToLocal(constrainedPoint), figure, tolerance);
                            if (newConnector != null && figure.canConnect(ff, newConnector)) {
                                newConnectedFigure = ff;
                                break SearchLoop;
                            }
                        }
                    }
                }
            }
        }
        figure.set(LineConnectionFigure.START_CONNECTOR, newConnector);
        figure.set(LineConnectionFigure.START_TARGET, newConnectedFigure);

        dm.addChildTo(figure, parent);
        event.consume();
    }

    @Override
    protected void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (figure != null) {
            onMouseDragged(event, view);
            view.getSelectedFigures().clear();
            view.getSelectedFigures().add(figure);
            figure = null;
        }
        fireToolDone();
    }

    @Override
    protected void stopEditing() {
        figure = null;
    }

    @Override
    public @NonNull String getHelpText() {
        return "ConnectionTool"
                + "\n  Press the mouse on a figure in the drawing view, and drag the mouse to another figure."
                + " The tool will create a new figure which connects the two figures."
                ;
    }

}
