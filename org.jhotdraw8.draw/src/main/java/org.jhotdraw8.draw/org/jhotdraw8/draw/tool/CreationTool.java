/*
 * @(#)CreationTool.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.constrain.Constrainer;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.AnchorableFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.figure.LayerFigure;
import org.jhotdraw8.draw.model.DrawingModel;

import java.util.function.Supplier;

import static java.lang.Math.abs;

/**
 * CreationTool.
 *
 * @author Werner Randelshofer
 */
public class CreationTool extends AbstractCreationTool<Figure> {


    private double defaultWidth = 100;
    private double defaultHeight = 100;
    /**
     * The rubber band.
     */
    protected double x1, y1, x2, y2;

    /**
     * The minimum size of a created figure (in view coordinates.
     */
    private final double minSize = 2;

    public CreationTool(String name, Resources rsrc, Supplier<Figure> factory) {
        this(name, rsrc, factory, LayerFigure::new);
    }

    public CreationTool(String name, Resources rsrc, Supplier<? extends Figure> figureFactory, Supplier<Layer> layerFactory) {
        super(name, rsrc, figureFactory, layerFactory);
        node.setCursor(Cursor.CROSSHAIR);
    }

    public double getDefaultHeight() {
        return defaultHeight;
    }

    public void setDefaultHeight(double defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    public double getDefaultWidth() {
        return defaultWidth;
    }

    public void setDefaultWidth(double defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    @Override
    protected void stopEditing() {
        createdFigure = null;
    }

    @Override
    protected void onMousePressed(MouseEvent event, DrawingView view) {
       undoHelper.stopCompositeEdit();

        createdFigure = createFigure();
        Figure parent = getOrCreateParent(view, createdFigure);
        if (parent == null) {
            event.consume();
            createdFigure = null;
            return;
        }

        undoHelper.startCompositeEdit(null);

        x1 = event.getX();
        y1 = event.getY();
        x2 = x1;
        y2 = y1;

        double anchorX = MathUtil.clamp(createdFigure.getNonNull(AnchorableFigure.ANCHOR_X), 0, 1);
        double anchorY = MathUtil.clamp(createdFigure.getNonNull(AnchorableFigure.ANCHOR_Y), 0, 1);


        CssPoint2D c =
                view.getConstrainer().constrainPoint(createdFigure, new CssPoint2D(
                        parent.worldToLocal(
                                view.viewToWorld(new Point2D(x1, y1)))));
        createdFigure.reshapeInLocal(
                anchorX == 0 ? c.getX() : c.getX().subtract(CssSize.of(1).multiply(anchorX)),
                anchorY == 0 ? c.getY() : c.getY().subtract(CssSize.of(1).multiply(anchorY)),
                CssSize.of(1), CssSize.of(1));
        DrawingModel dm = view.getModel();

        view.setActiveParent(parent);

        dm.addChildTo(createdFigure, parent);
        event.consume();
    }

    @Override
    protected void onMouseReleased(MouseEvent event, DrawingView dv) {
        undoHelper.stopCompositeEdit();
        if (createdFigure != null) {
            if (abs(x2 - x1) < minSize && abs(y2 - y1) < minSize) {
                double width = getDefaultWidth();
                double height = getDefaultHeight();

                CssPoint2D c1 = dv.getConstrainer().constrainPoint(createdFigure, new CssPoint2D(
                        createdFigure.worldToParent(dv.viewToWorld(x1, y1))));
                CssPoint2D c2 = dv.getConstrainer().translatePoint(createdFigure, new CssPoint2D(
                        createdFigure.worldToParent(dv.viewToWorld(x1
                                + width, y1 + height))), Constrainer.DIRECTION_NEAREST);
                if (c2.equals(c1)) {
                    c2 = dv.getConstrainer().constrainPoint(createdFigure, new CssPoint2D(c1.getX().getConvertedValue() + defaultWidth, c1.getY().getConvertedValue() + defaultHeight));
                }
                DrawingModel dm = dv.getModel();
                reshapeInLocal(createdFigure, c1, c2, dm);
            }
            dv.selectedFiguresProperty().clear();
            dv.selectedFiguresProperty().add(createdFigure);
            createdFigure = null;
        }
        event.consume();
        fireToolDone();
    }

    protected void reshapeInLocal(Figure figure, CssPoint2D c1, CssPoint2D c2, DrawingModel dm) {
        dm.reshapeInLocal(figure, c1.getX(), c1.getY(),
                c2.getX().subtract(c1.getX()),
                c2.getY().subtract(c1.getY()));
    }

    @Override
    protected void onMouseDragged(MouseEvent event, DrawingView dv) {
        undoHelper.startCompositeEdit(null);
        if (createdFigure != null) {
            x2 = event.getX();
            y2 = event.getY();
            CssPoint2D c1 = dv.getConstrainer().constrainPoint(createdFigure, new CssPoint2D(createdFigure.worldToParent(dv.viewToWorld(x1, y1))));
            CssPoint2D c2 = dv.getConstrainer().constrainPoint(createdFigure, new CssPoint2D(createdFigure.worldToParent(dv.viewToWorld(x2, y2))));
            CssSize newWidth = c2.getX().subtract(c1.getX());
            CssSize newHeight = c2.getY().subtract(c1.getY());
            // shift keeps the aspect ratio
            boolean keepAspect = event.isShiftDown();
            if (keepAspect) {
                double preferredAspectRatio = createdFigure.getPreferredAspectRatio();
                double newRatio = newHeight.getConvertedValue() / newWidth.getConvertedValue();
                if (newRatio > preferredAspectRatio) {
                    newHeight = CssSize.of(newWidth.getConvertedValue() * preferredAspectRatio);
                } else {
                    newWidth = CssSize.of(newHeight.getConvertedValue() / preferredAspectRatio);
                }
            }

            DrawingModel dm = dv.getModel();
            dm.reshapeInLocal(createdFigure, c1.getX(), c1.getY(), newWidth, newHeight);
        }
        event.consume();
    }


    /**
     * This implementation is empty.
     */
    @Override
    public void activate(DrawingEditor editor) {
        requestFocus();
        super.activate(editor);
    }

    @Override
    public String getHelpText() {
        return """
               CreationTool
                 Click on the drawing view. The tool will create a new figure with default size at the clicked location.
               Or:
                 Press and drag the mouse over the drawing view to define the diagonal of a rectangle. The tool will create a new figure that fits into the rectangle.""";
    }

}
