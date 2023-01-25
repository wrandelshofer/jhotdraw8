/*
 * @(#)AbstractResizeTransformHandle.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.locator.Locator;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.FXTransforms;

import java.util.function.Function;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;

/**
 * AbstractResizeTransformHandle.
 *
 * @author Werner Randelshofer
 */
abstract class AbstractResizeTransformHandle extends LocatorHandle {
    public static final @Nullable BorderStrokeStyle INSIDE_STROKE = new BorderStrokeStyle(StrokeType.INSIDE, StrokeLineJoin.MITER, StrokeLineCap.BUTT, 1.0, 0, null);

    private final @NonNull Region node;
    private Point2D oldPoint;
    private Point2D pickLocation;
    /**
     * The height divided by the width.
     */
    protected double preferredAspectRatio;
    protected CssRectangle2D startBounds;
    private @Nullable Transform startWorldToLocal;
    private final Function<Color, Border> borderFactory;

    public AbstractResizeTransformHandle(Figure owner, Locator locator, Shape shape, Background bg, Function<Color, Border> borderFactory) {
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
    public @NonNull Region getNode(@NonNull DrawingView view) {
        double size = view.getEditor().getHandleSize();
        if (node.getWidth() != size) {
            node.resize(size, size);
        }
        CssColor color = view.getEditor().getHandleColor();
        node.setBorder(borderFactory.apply(color.getColor()));
        return node;
    }

    @Override
    public void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        CssPoint2D newPoint = new CssPoint2D(view.viewToWorld(new Point2D(event.getX(), event.getY())));

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control turns the constrainer off
            newPoint = view.getConstrainer().constrainPoint(owner, newPoint);
        }
        if (event.isMetaDown()) {
            // meta snaps the location of the handle to the grid
            Point2D loc = getLocation();
            oldPoint = loc;
        }
        // shift keeps the aspect ratio
        boolean keepAspect = event.isShiftDown();

        Transform t = startWorldToLocal;//owner.getWorldToLocal();

        if (t == null || t.isIdentity()) {
            resize(newPoint, owner, startBounds, view.getModel(), keepAspect);
        } else {
            resize(
                    convertPoint2D(
                            new CssPoint2D(FXTransforms.transform(t, newPoint.getConvertedValue())),
                            newPoint.getX().getUnits(), DefaultUnitConverter.getInstance()),
                    owner, startBounds, view.getModel(), keepAspect);
        }
    }

    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        oldPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(view.viewToWorld(new Point2D(event.getX(), event.getY())))).getConvertedValue();
        startBounds = owner.getCssLayoutBounds();
        startWorldToLocal = owner.getWorldToLocal();
        preferredAspectRatio = owner.getPreferredAspectRatio();
    }

    @Override
    public void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView dv) {
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
    // FIXME remove this method - we only want the variant with CssPoint2D and CssRectangle2D
    protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
        throw new UnsupportedOperationException("don't want to implement this in class " + getClass());
    }

    protected void resize(@NonNull CssPoint2D newPoint, Figure owner, @NonNull CssRectangle2D bounds, @NonNull DrawingModel model, boolean keepAspect) {
        resize(newPoint.getConvertedValue(), owner, bounds.getConvertedBoundsValue(), model, keepAspect);
    }

    @Override
    public void updateNode(@NonNull DrawingView view) {
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

    private @NonNull CssPoint2D convertPoint2D(@NonNull CssPoint2D cssPoint2D, @NonNull String units, @NonNull UnitConverter c) {
        return new CssPoint2D(c.convertSize(cssPoint2D.getX(), units),
                c.convertSize(cssPoint2D.getY(), units));
    }
}
