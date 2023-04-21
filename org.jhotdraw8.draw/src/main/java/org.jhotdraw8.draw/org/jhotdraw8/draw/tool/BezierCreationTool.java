/*
 * @(#)BezierCreationTool.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.vector.VectorList;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.figure.LayerFigure;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.key.BezierNodeListStyleableKey;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.BezierFit;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierNodePathBuilder;

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

    private final BezierNodeListStyleableKey key;
    /**
     * The bezier nodes being created.
     */
    private @Nullable ArrayList<BezierNode> points;
    /**
     * The rubber band shows where the next point will be added.
     */
    private final @NonNull Line rubberBand = new Line();

    public BezierCreationTool(String name, Resources rsrc, BezierNodeListStyleableKey key, Supplier<Figure> factory) {
        this(name, rsrc, key, factory, LayerFigure::new);
    }

    public BezierCreationTool(String name, Resources rsrc, BezierNodeListStyleableKey key, Supplier<Figure> figureFactory, Supplier<Layer> layerFactory) {
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
    public void activate(@NonNull DrawingEditor editor) {
        requestFocus();
        rubberBand.setVisible(false);
        createdFigure = null;
        super.activate(editor);
    }

    @Override
    protected void onMouseClicked(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (event.getClickCount() > 1) {
            if (createdFigure != null) {
                for (int i = points.size() - 1; i > 0; i--) {
                    if (Objects.equals(points.get(i), points.get(i - 1))) {
                        points.remove(i);
                    }
                }
                DrawingModel dm = dv.getModel();
                if (points.size() < 2) {
                    dm.removeFromParent(createdFigure);
                } else {
                    dm.set(createdFigure, key, VectorList.copyOf(points));
                    dv.getSelectedFigures().clear();
                    dv.getEditor().setHandleType(HandleType.POINT);
                    dv.getSelectedFigures().add(createdFigure);
                }
                createdFigure = null;
                points = null;
                fireToolDone();
            }
        }
    }

    @Override
    protected void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (createdFigure != null && points != null) {
            double x2 = event.getX();
            double y2 = event.getY();
            Point2D c2 = createdFigure.worldToParent(dv.viewToWorld(x2, y2));
            DrawingModel dm = dv.getModel();
            if (dragStartIndex < 0) {
                points.add(new BezierNode(c2));
                dragStartIndex = points.size() - 1;
            } else {
                points.add(new BezierNode(c2));
            }
            dm.set(createdFigure, key, VectorList.copyOf(points));
        }
        event.consume();
    }

    @Override
    protected void onMouseMoved(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (createdFigure != null && points != null) {
            /*
            dragStartIndex = -1;
            double x2 = event.getX();
            double y2 = event.getY();
            Point2D c2 = dv.getConstrainer().constrainPoint(createdFigure, dv.viewToWorld(x2, y2));
            DrawingModel dm = dv.getModel();
            points.set(points.size() - 1, new BezierNode(c2));
            dm.set(createdFigure, key, ImmutableList.ofCollection(points));
             */
            if (!points.isEmpty()) {
                BezierNode lastNode = points.get(points.size() - 1);
                Point2D start = FXTransforms.transform(FXTransforms.concat(dv.getWorldToView(), createdFigure.getLocalToWorld()), lastNode.getX0(), lastNode.getY0());
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
    protected void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
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
                points = null;
                return;
            }
            points = new ArrayList<>();
            view.setActiveParent(parent);

            dm.addChildTo(createdFigure, parent);

        }
        assert points != null;
        CssPoint2D c = view.getConstrainer().constrainPoint(createdFigure, new CssPoint2D(
                createdFigure.worldToParent(view.viewToWorld(new Point2D(x1, y1)))));
        points.add(new BezierNode(c.getConvertedValue()));
        dm.set(createdFigure, key, VectorList.copyOf(points));

        rubberBand.setVisible(false);
        dragStartIndex = -1;
        event.consume();
    }

    @Override
    protected void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (createdFigure == null || points == null) {
            return;
        }
        if (dragStartIndex != -1) {
            List<Point2D> digitized = new ArrayList<>(points.size() - dragStartIndex);
            for (int i = dragStartIndex, n = points.size(); i < n; i++) {
                digitized.add(points.get(i).getC0());
            }
            BezierNodePathBuilder builder = new BezierNodePathBuilder();
            double error = 5 / dv.getZoomFactor();
            BezierFit.fitBezierPath(builder, digitized, error);
            final ImmutableList<BezierNode> built = builder.build();
            ArrayList<BezierNode> newList = new ArrayList<>(dragStartIndex + built.size());
            for (int i = 0; i < dragStartIndex; i++) {
                newList.add(points.get(i));
            }

            for (int i = 0, n = built.size(); i < n; i++) {
                if (i == 0) {
                    newList.add(built.get(i).setMask(built.get(i).getMask() & (~BezierNode.MOVE_MASK)));
                } else {
                    newList.add(built.get(i));
                }
            }
            newList.add(points.get(points.size() - 1));
            points = newList;

            DrawingModel dm = dv.getModel();
            dm.set(createdFigure, key, VectorList.copyOf(points));
            dragStartIndex = -1;
        }
    }

    @Override
    protected void stopEditing() {
        if (createdFigure != null) {
            rubberBand.setVisible(false);

            createdFigure = null;
            points = null;
        }
    }

    @Override
    public @NonNull String getHelpText() {
        return "BezierCreationTool"
                + "\n  Click on the drawing view. The tool will create a new bezier curve with a point at that location."
                + "\n  Continue clicking on the drawing view. The tool will add each clicked point to the created bezier curve."
                + "\n  Press enter or escape, when you are done."
                + "\nOr"
                + "\n  Press and drag the mouse over the drawing view to draw a curve. The tool will create a new bezier curve with a curve fitted to your drawing."
                + "\n  Continue pressing and dragging on the drawing view. The tool will add additional fitted curves to the bezier curve."
                + "\n  Press enter or escape when you are done.";
    }

}
