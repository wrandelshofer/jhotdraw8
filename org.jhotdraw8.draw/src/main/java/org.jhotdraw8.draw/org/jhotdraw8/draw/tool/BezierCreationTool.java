/*
 * @(#)BezierCreationTool.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.figure.LayerFigure;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.key.NonNullObjectStyleableKey;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.PolylineToCubicCurve;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jhotdraw8.geom.shape.BezierPathBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * CreationTool for bezier figures.
 *
 * @author Werner Randelshofer
 */
public class BezierCreationTool extends AbstractCreationTool<Figure> {

    /**
     * Remembers the number ofCollection points that we had, when the user
     * started to drag the mouse.
     */
    private int dragStartIndex;

    private final NonNullObjectStyleableKey<BezierPath> key;
    /**
     * The bezier nodes being created.
     */
    private @Nullable BezierPath path;
    /**
     * The rubber band shows where the next point will be added.
     */
    private final Line rubberBand = new Line();

    public BezierCreationTool(String name, Resources rsrc, NonNullObjectStyleableKey<BezierPath> key, Supplier<Figure> factory) {
        this(name, rsrc, key, factory, LayerFigure::new);
    }

    public BezierCreationTool(String name, Resources rsrc, NonNullObjectStyleableKey<BezierPath> key, Supplier<Figure> figureFactory, Supplier<Layer> layerFactory) {
        super(name, rsrc, figureFactory, layerFactory);
        this.key = key;
        node.setCursor(Cursor.CROSSHAIR);
        rubberBand.setVisible(false);
        rubberBand.setMouseTransparent(true);
        rubberBand.getStrokeDashArray().setAll(2.0, 5.0);
        rubberBand.setManaged(false);
        node.getChildren().add(rubberBand);
    }

    /**
     * This implementation is empty.
     */
    @Override
    public void activate(DrawingEditor editor) {
        requestFocus();
        rubberBand.setVisible(false);
        createdFigure = null;
        super.activate(editor);
    }

    @Override
    protected void onMouseClicked(MouseEvent event, DrawingView dv) {
        if (event.getClickCount() > 1) {
            if (createdFigure != null) {
                for (int i = path.size() - 1; i > 0; i--) {
                    if (Objects.equals(path.get(i), path.get(i - 1))) {
                        path = path.removeAt(i);
                    }
                }
                DrawingModel dm = dv.getModel();
                if (path.size() < 2) {
                    dm.removeFromParent(createdFigure);
                } else {
                    dm.set(createdFigure, key, path);
                    dv.getSelectedFigures().clear();
                    dv.getEditor().setHandleType(HandleType.POINT);
                    dv.getSelectedFigures().add(createdFigure);
                }
                createdFigure = null;
                path = null;
                fireToolDone();
            }
        }
    }

    @Override
    protected void onMouseDragged(MouseEvent event, DrawingView dv) {
        if (createdFigure != null && path != null) {
            double x2 = event.getX();
            double y2 = event.getY();
            Point2D c2 = createdFigure.worldToParent(dv.viewToWorld(x2, y2));
            DrawingModel dm = dv.getModel();
            if (dragStartIndex < 0) {
                path = path.add(new BezierNode(c2.getX(), c2.getY()));
                dragStartIndex = path.size() - 1;
            } else {
                path = path.add(new BezierNode(c2.getX(), c2.getY()));
            }
            dm.set(createdFigure, key, path);
        }
        event.consume();
    }

    @Override
    protected void onMouseMoved(MouseEvent event, DrawingView dv) {
        if (createdFigure != null && path != null) {
            if (!path.isEmpty()) {
                BezierNode lastNode = path.get(path.size() - 1);
                Point2D start = FXTransforms.transform(FXTransforms.concat(dv.getWorldToView(), createdFigure.getLocalToWorld()), lastNode.pointX(), lastNode.pointY());
                rubberBand.setStartX(start.getX());
                rubberBand.setStartY(start.getY());
                rubberBand.setEndX(event.getX());
                rubberBand.setEndY(event.getY());
                rubberBand.setVisible(true);
            }
        }
        event.consume();
    }

    @Override
    protected void onMousePressed(MouseEvent event, DrawingView view) {
        if (event.getClickCount() != 1) {
            return;
        }
        double x1 = event.getX();
        double y1 = event.getY();

        DrawingModel dm = view.getModel();
        if (createdFigure == null) {
            createdFigure = createFigure();
            Figure parent = createdFigure == null ? null : getOrCreateParent(view, createdFigure);
            if (parent == null) {
                createdFigure = null;
                path = null;
                return;
            }
            path = BezierPath.of();
            view.setActiveParent(parent);

            dm.addChildTo(createdFigure, parent);

        }
        assert path != null;
        CssPoint2D c = view.getConstrainer().constrainPoint(createdFigure, new CssPoint2D(
                createdFigure.worldToParent(view.viewToWorld(new Point2D(x1, y1)))));
        path = path.add(new BezierNode(c.getConvertedValue()));
        dm.set(createdFigure, key, path);

        rubberBand.setVisible(false);
        dragStartIndex = -1;
        event.consume();
    }

    @Override
    protected void onMouseReleased(MouseEvent event, DrawingView dv) {
        if (createdFigure == null || path == null) {
            return;
        }
        if (dragStartIndex != -1) {
            List<Point2D> digitized = new ArrayList<>(path.size() - dragStartIndex);
            for (int i = dragStartIndex, n = path.size(); i < n; i++) {
                digitized.add(path.get(i).getPoint(Point2D::new));
            }
            BezierPathBuilder builder = new BezierPathBuilder();
            double error = 5 / dv.getZoomFactor();
            PolylineToCubicCurve.fitBezierPath(builder, digitized, error);
            BezierPath built = builder.build();

            BezierPath newList = BezierPath.of();
            for (int i = 0; i < dragStartIndex; i++) {
                newList = newList.add(path.get(i));
            }

            for (int i = 0, n = built.size(); i < n; i++) {
                if (i == 0) {
                    newList = newList.add(built.get(i).withMask(built.get(i).getMask() & (~BezierNode.MOVE_MASK)));
                } else {
                    newList = newList.add(built.get(i));
                }
            }
            newList = newList.add(path.getLast());
            path = newList;

            DrawingModel dm = dv.getModel();
            dm.set(createdFigure, key, path);
            dragStartIndex = -1;
        }
    }

    @Override
    protected void stopEditing() {
        if (createdFigure != null) {
            rubberBand.setVisible(false);

            createdFigure = null;
            path = null;
        }
    }

    @Override
    public String getHelpText() {
        return """
               BezierCreationTool
                 Click on the drawing view. The tool will create a new bezier curve with a point at that location.
                 Continue clicking on the drawing view. The tool will add each clicked point to the created bezier curve.
                 Press enter or escape, when you are done.
               Or
                 Press and drag the mouse over the drawing view to draw a curve. The tool will create a new bezier curve with a curve fitted to your drawing.
                 Continue pressing and dragging on the drawing view. The tool will add additional fitted curves to the bezier curve.
                 Press enter or escape when you are done.""";
    }

}
