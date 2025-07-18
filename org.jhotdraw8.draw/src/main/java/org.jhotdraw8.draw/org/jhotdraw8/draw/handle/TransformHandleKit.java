/*
 * @(#)TransformHandleKit.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.color.util.MathUtil;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.css.value.CssRectangle2D;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.TransformableFigure;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.locator.Locator;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_PIVOT;
import static org.jhotdraw8.draw.figure.TransformableFigure.TRANSFORMS;

/**
 * A set of utility methods to create handles which transform a Figure by using
 * its {@code transform} method, if the Figure is transformable.
 * <p>
 * FIXME implement me
 */
public class TransformHandleKit {

    protected static final SVGPath NORTH_SHAPE = new SVGPath();
    protected static final SVGPath EAST_SHAPE = new SVGPath();
    protected static final SVGPath WEST_SHAPE = new SVGPath();
    protected static final SVGPath SOUTH_SHAPE = new SVGPath();//new Rectangle(9, 5);
    protected static final SVGPath NORTH_EAST_SHAPE = new SVGPath();
    protected static final SVGPath NORTH_WEST_SHAPE = new SVGPath();
    protected static final SVGPath SOUTH_EAST_SHAPE = new SVGPath();
    protected static final SVGPath SOUTH_WEST_SHAPE = new SVGPath();

    static {
        final String circle = "M 9,4.5 A 4.5,4.5 0 1 0 0,4.5 A 4.5,4.5 0 1 0 9,4.5 Z ";
        NORTH_EAST_SHAPE.setContent(circle + "M 4.5,9 4.5,4.5 0,4.5 ");
        NORTH_WEST_SHAPE.setContent(circle + "M 9,4.5 4.5,4.5 4.5,9");
        SOUTH_EAST_SHAPE.setContent(circle + "M 0,4.5 4.5,4.5 4.5,0");
        SOUTH_WEST_SHAPE.setContent(circle + "M 4.5,0 4.5,4.5 9,4.5 ");
        SOUTH_SHAPE.setContent(circle + "M 0,4.5 9,4.5");
        NORTH_SHAPE.setContent(circle + "M 0,4.5 9,4.5");
        EAST_SHAPE.setContent(circle + "M 4.5,0 4.5,9");
        WEST_SHAPE.setContent(circle + "M 4.5,0 4.5,9");
    }

    private static final Background REGION_BACKGROUND = new Background(new BackgroundFill(Color.WHITE, null, null));
    private static final Function<Color, Border> REGION_BORDER = color -> new Border(
            new BorderStroke(color, BorderStrokeStyle.SOLID, null, null)
    );

    /**
     * Prevent instance creation.
     */
    private TransformHandleKit() {

    }

    /**
     * Creates handles for each corner of a figure and adds them to the provided
     * collection.
     *
     * @param f       the figure which will own the handles
     * @param handles the list to which the handles should be added
     */
    public static void addCornerTransformHandles(TransformableFigure f, Collection<Handle> handles) {
        handles.add(southEast(f));
        handles.add(southWest(f));
        handles.add(northEast(f));
        handles.add(northWest(f));
    }

    /**
     * Fills the given collection with handles at each the north, south, east,
     * and west of the figure.
     *
     * @param f       the figure which will own the handles
     * @param handles the list to which the handles should be added
     */
    public static void addEdgeTransformHandles(TransformableFigure f, Collection<Handle> handles) {
        handles.add(south(f));
        handles.add(north(f));
        handles.add(east(f));
        handles.add(west(f));
    }

    /**
     * Fills the given collection with handles at each the north, south, east,
     * and west of the figure.
     *
     * @param f       the figure which will own the handles
     * @param handles the list to which the handles should be added
     */
    public static void addTransformHandles(TransformableFigure f, Collection<Handle> handles) {
        addCornerTransformHandles(f, handles);
        addEdgeTransformHandles(f, handles);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle south(TransformableFigure owner) {
        return new SouthHandle(owner);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle southEast(TransformableFigure owner) {
        return new SouthEastHandle(owner);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle southWest(TransformableFigure owner) {
        return new SouthWestHandle(owner);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle north(TransformableFigure owner) {
        return new NorthHandle(owner);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle northEast(TransformableFigure owner) {
        return new NorthEastHandle(owner);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle northWest(TransformableFigure owner) {
        return new NorthWestHandle(owner);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle east(TransformableFigure owner) {
        return new EastHandle(owner);
    }

    /**
     * Creates a handle for the specified figure.
     *
     * @param owner the figure which will own the handle
     * @return the handle
     */
    public static Handle west(TransformableFigure owner) {
        return new WestHandle(owner);
    }

    private abstract static class AbstractTransformHandle extends AbstractResizeTransformHandle {

        @Nullable
        PersistentList<Transform> startTransforms;

        public AbstractTransformHandle(Figure owner, Locator locator, Shape shape, Background bg, Function<Color, Border> border) {
            super(owner, locator, shape, bg, border);
        }

        @Override
        public void onMousePressed(MouseEvent event, DrawingView view) {
            super.onMousePressed(event, view);
            startTransforms = owner.get(TRANSFORMS);
        }

        protected void transform(DrawingModel model, Figure o, double x, double y, double width, double height) {
            if (width == 0 || height == 0) {
                return;
            }
            TransformableFigure owner = (TransformableFigure) o;
            Bounds bounds = startBounds.getConvertedBoundsValue();

            // We are about to change the location of the center of the figure.
            // If there is a rotation around the center of the figure,
            // replace it by a rotation around a fixed pivot point.
            double rotate = owner.get(ROTATE);
            if (rotate != 0.0) {
                model.set(owner, ROTATE, 0.0);
                Point2D relativePivot = owner.get(ROTATION_PIVOT).getConvertedValue();
                Point2D pivot = new Point2D(bounds.getMinX() + bounds.getWidth() * relativePivot.getX(),
                        bounds.getMinY() + bounds.getHeight() * relativePivot.getY());
                pivot = FXTransforms.transform(((TransformableFigure) o).getLocalToParent(false), pivot);
                startTransforms = startTransforms.addFirst(
                        new FXPreciseRotate(rotate, pivot.getX(), pivot.getY())
                );
            }
            PersistentList<Transform> oldTransforms = startTransforms;

            double sx = MathUtil.approximate(width / bounds.getWidth(), 1, 1e-6);
            double sy = MathUtil.approximate(height / bounds.getHeight(), 1, 1e-6);
            double tx = MathUtil.approximate(x - bounds.getMinX(), 0, 1e-6);
            double ty = MathUtil.approximate(y - bounds.getMinY(), 0, 1e-6);

            Transform transform = new Translate(tx, ty);
            if (!Double.isNaN(sx) && !Double.isNaN(sy)
                    && !Double.isInfinite(sx) && !Double.isInfinite(sy)
                    && (sx != 1d || sy != 1d)) {
                transform = FXTransforms.concat(transform, new Scale(sx, sy, bounds.getMinX(), bounds.getMinY()));
            }
            PersistentList<Transform> newTransforms;
            if (oldTransforms.isEmpty() || (oldTransforms.getLast() instanceof Rotate)) {
                newTransforms = oldTransforms.add(transform);
            } else {
                int last = oldTransforms.size() - 1;
                Transform concat = FXTransforms.concat(oldTransforms.get(last), transform);
                newTransforms = concat.isIdentity() ? oldTransforms.removeLast() : oldTransforms.set(last, concat);
            }
            model.set(owner, TRANSFORMS, newTransforms);
        }

        @Override
        protected void resize(CssPoint2D newPoint, Figure owner, CssRectangle2D bounds, DrawingModel model, boolean keepAspect) {
            resize(newPoint.getConvertedValue(), owner, bounds.getConvertedBoundsValue(), model, keepAspect);
        }

        // FIXME remove this method, because it resizes always in pixel coordinates, but we want to support coordinates in other units
        protected abstract void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect);
    }

    private static class NorthEastHandle extends AbstractTransformHandle {

        NorthEastHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.NORTH_EAST, NORTH_EAST_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.CROSSHAIR;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newX = max(bounds.getMinX(), newPoint.getX());
            double newY = min(bounds.getMaxY(), newPoint.getY());
            double newWidth = newX - bounds.getMinX();
            double newHeight = bounds.getMaxY() - newY;
            if (keepAspect) {
                double newRatio = newHeight / newWidth;
                if (newRatio > preferredAspectRatio) {
                    newHeight = newWidth * preferredAspectRatio;
                } else {
                    newWidth = newHeight / preferredAspectRatio;
                }
            }

            transform(model, owner, bounds.getMinX(), bounds.getMaxY() - newHeight, newWidth, newHeight);
        }

    }

    private static class EastHandle extends AbstractTransformHandle {

        EastHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.EAST, EAST_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.CROSSHAIR;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newWidth = max(newPoint.getX(), bounds.getMinX()) - bounds.getMinX();
            double newHeight = bounds.getMaxY() - bounds.getMinY();
            if (keepAspect) {
                newHeight = newWidth * preferredAspectRatio;
            }
            transform(model, owner, bounds.getMinX(), (bounds.getMinY() + bounds.getMaxY() - newHeight) * 0.5, newWidth, newHeight);
        }

    }

    private static class NorthHandle extends AbstractTransformHandle {

        NorthHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.NORTH, NORTH_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.CROSSHAIR;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newY = min(bounds.getMaxY(), newPoint.getY());
            double newWidth = bounds.getMaxX() - bounds.getMinX();
            double newHeight = bounds.getMaxY() - newY;
            if (keepAspect) {
                double newRatio = newHeight / newWidth;
                newWidth = newHeight / preferredAspectRatio;
            }
            transform(model, owner, (bounds.getMinX() + bounds.getMaxX() - newWidth) * 0.5, newY, newWidth, newHeight);
        }
    }

    private static class NorthWestHandle extends AbstractTransformHandle {

        NorthWestHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.NORTH_WEST, NORTH_WEST_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.CROSSHAIR;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newX = min(bounds.getMaxX(), newPoint.getX());
            double newY = min(bounds.getMaxY(), newPoint.getY());
            double newWidth = bounds.getMaxX() - newX;
            double newHeight = bounds.getMaxY() - newY;
            if (keepAspect) {
                double newRatio = newHeight / newWidth;
                if (newRatio > preferredAspectRatio) {
                    newHeight = newWidth * preferredAspectRatio;
                } else {
                    newWidth = newHeight / preferredAspectRatio;
                }
            }

            transform(model, owner, bounds.getMaxX() - newWidth, bounds.getMaxY() - newHeight, newWidth, newHeight);
        }
    }

    private static class SouthEastHandle extends AbstractTransformHandle {

        SouthEastHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.SOUTH_EAST, SOUTH_EAST_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.SE_RESIZE;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newX = max(bounds.getMinX(), newPoint.getX());
            double newY = max(bounds.getMinY(), newPoint.getY());
            double newWidth = newX - bounds.getMinX();
            double newHeight = newY - bounds.getMinY();
            if (keepAspect) {
                double newRatio = newHeight / newWidth;
                if (newRatio > preferredAspectRatio) {
                    newHeight = newWidth * preferredAspectRatio;
                } else {
                    newWidth = newHeight / preferredAspectRatio;
                }
            }
            transform(model, owner, bounds.getMinX(), bounds.getMinY(), newWidth, newHeight);
        }
    }

    private static class SouthHandle extends AbstractTransformHandle {

        SouthHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.SOUTH, SOUTH_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.S_RESIZE;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newY = max(bounds.getMinY(), newPoint.getY());
            double newWidth = bounds.getMaxX() - bounds.getMinX();
            double newHeight = newY - bounds.getMinY();
            if (keepAspect) {
                newWidth = newHeight / preferredAspectRatio;
            }
            transform(model, owner, (bounds.getMinX() + bounds.getMaxX() - newWidth) * 0.5, bounds.getMinY(), newWidth, newHeight);
        }
    }

    private static class SouthWestHandle extends AbstractTransformHandle {

        SouthWestHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.SOUTH_WEST, SOUTH_WEST_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.CROSSHAIR;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newX = min(bounds.getMaxX(), newPoint.getX());
            double newY = max(bounds.getMinY(), newPoint.getY());
            double newWidth = bounds.getMaxX() - min(bounds.getMaxX(), newX);
            double newHeight = newY - bounds.getMinY();
            if (keepAspect) {
                double newRatio = newHeight / newWidth;
                if (newRatio > preferredAspectRatio) {
                    newHeight = newWidth * preferredAspectRatio;
                } else {
                    newWidth = newHeight / preferredAspectRatio;
                }
            }
            transform(model, owner, bounds.getMaxX() - newWidth, bounds.getMinY(), newWidth, newHeight);
        }
    }

    private static class WestHandle extends AbstractTransformHandle {

        WestHandle(TransformableFigure owner) {
            super(owner, BoundsLocator.WEST, WEST_SHAPE, REGION_BACKGROUND, REGION_BORDER);
        }

        @Override
        public Cursor getCursor() {
            return Cursor.CROSSHAIR;
        }

        @Override
        protected void resize(Point2D newPoint, Figure owner, Bounds bounds, DrawingModel model, boolean keepAspect) {
            double newX = min(bounds.getMaxX(), newPoint.getX());
            double newWidth = bounds.getMaxX() - newX;
            double newHeight = bounds.getHeight();
            if (keepAspect) {
                newHeight = newWidth * preferredAspectRatio;
            }
            transform(model, owner, newX, (bounds.getMinY() + bounds.getMaxY() - newHeight) * 0.5, newWidth, newHeight);
        }
    }
}
