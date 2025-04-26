/*
 * @(#)AbstractResizeTransformHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.css.value.CssRectangle2D;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.locator.Locator;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;

/**
 * AbstractResizeTransformHandle.
 */
abstract class AbstractResizeTransformHandle extends LocatorHandle {
    public static final @Nullable BorderStrokeStyle INSIDE_STROKE = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 1.0, 0, null);

    private final Region node;
    private Point2D oldPoint;
    private Point2D pickLocation;
    /**
     * The height divided by the width.
     */
    protected double preferredAspectRatio;
    protected CssRectangle2D startBounds;
    private @Nullable Transform startWorldToLocal;
    private final Function<Color, Border> borderFactory;

    public AbstractResizeTransformHandle(Figure owner, Locator locator, Shape shape, @Nullable Background bg, Function<Color, Border> borderFactory) {
        super(owner, locator);
        node = new Region();
        node.setShape(shape);
        node.setManaged(false);
        node.setScaleShape(true);
        node.setCenterShape(true);
        node.resize(11, 11);
        this.borderFactory = borderFactory;
        node.setBackground(bg);
    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public Region getNode(DrawingView view) {
        double size = view.getEditor().getHandleSize();
        if (node.getWidth() != size) {
            node.resize(size, size);
        }
        CssColor color = view.getEditor().getHandleColor();
        node.setBorder(borderFactory.apply(color.getColor()));
        return node;
    }

    @Override
    public void onMouseDragged(MouseEvent event, DrawingView view) {
        CssPoint2D pointInWorld = new CssPoint2D(view.viewToWorld(new Point2D(event.getX(), event.getY())));

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control turns the constrainer off
            pointInWorld = view.getConstrainer().constrainPoint(owner, pointInWorld);
        }
        if (event.isMetaDown()) {
            // meta snaps the location of the handle to the grid
            Point2D loc = getLocation();
            oldPoint = loc;
        }
        // shift keeps the aspect ratio
        boolean keepAspect = event.isShiftDown();

        Transform t = startWorldToLocal;

        if (t == null || t.isIdentity()) {
            resize(pointInWorld, owner, startBounds, view.getModel(), keepAspect);
        } else {
            CssPoint2D pointInLocal = convertPoint2D(
                    new CssPoint2D(FXTransforms.transform(t, pointInWorld.getConvertedValue())),
                    pointInWorld.getX().getUnits(), DefaultUnitConverter.getInstance());
            resize(
                    pointInLocal,
                    owner, startBounds, view.getModel(), keepAspect);
        }
    }

    @Override
    public void onMousePressed(MouseEvent event, DrawingView view) {
        oldPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(view.viewToWorld(new Point2D(event.getX(), event.getY())))).getConvertedValue();
        startBounds = owner.getCssLayoutBounds();
        startWorldToLocal = owner.getWorldToLocal();
        preferredAspectRatio = owner.getPreferredAspectRatio();
    }

    @Override
    public void onMouseReleased(MouseEvent event, DrawingView dv) {
        // FIXME fire undoable edit
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     * Resizes the figure.
     *
     * @param newPoint   new point in local coordinates
     * @param owner      the figure
     * @param bounds     the bounds of the figure on mouse pressed
     * @param model      the drawing model
     * @param keepAspect whether the aspect should be preserved. The bounds of
     *                   the figure on mouse pressed can be used as a reference.
     */

    protected abstract void resize(CssPoint2D newPoint, Figure owner, CssRectangle2D bounds, DrawingModel model, boolean keepAspect);

    @Override
    public void updateNode(DrawingView view) {
        Figure f = owner;
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        Bounds b = f.getLayoutBounds();
        Point2D p = getLocation();
        pickLocation = p = FXTransforms.transform(t, p);

        // Place the center of the node at the location.
        double size = node.getWidth();
        node.relocate(p.getX() - size * 0.5, p.getY() - size * 0.5);

        // Rotate the node.
        node.setRotate(f.getStyledNonNull(ROTATE));
        node.setRotationAxis(f.getStyled(ROTATION_AXIS));
    }

    private CssPoint2D convertPoint2D(CssPoint2D cssPoint2D, String units, UnitConverter c) {
        return new CssPoint2D(c.convertSize(cssPoint2D.getX(), units),
                c.convertSize(cssPoint2D.getY(), units));
    }
}
