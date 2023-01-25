/*
 * @(#)GridConstrainer.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.constrain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.ViewBoxableDrawing;
import org.jhotdraw8.geom.Geom;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

/**
 * GridConstrainer.
 *
 * @author Werner Randelshofer
 */
public class GridConstrainer extends AbstractConstrainer {

    /**
     * Up-Vector.
     */
    private final Point2D UP = new Point2D(0, 1);
    /**
     * The angle for constrained rotations on the grid (in degrees). The value 0
     * turns the constrainer off for rotations.
     */
    private final DoubleProperty angle = new SimpleDoubleProperty(this, "angle") {

        @Override
        public void invalidated() {
            fireInvalidated(this);
        }
    };
    /**
     * Whether to draw the grid.
     */
    private final BooleanProperty drawGrid = new SimpleBooleanProperty(this, "drawGrid") {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };
    /**
     * Height of a grid cell. The value 0 turns the constrainer off for the
     * vertical axis.
     */
    private final ObjectProperty<CssSize> height = new SimpleObjectProperty<CssSize>(this, "height") {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };

    private final @NonNull ObjectProperty<CssColor> gridColorProperty = new SimpleObjectProperty<CssColor>(this, "majorGridColor", new CssColor("hsba(226,100%,75%,40%)", Color.hsb(226, 1.0, 0.75, 0.4))) {
        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };

    private final Path majorNode = new Path();
    /**
     * The x-factor for the major grid of the grid.
     */
    private final IntegerProperty majorX = new SimpleIntegerProperty(this, "major-x", 5) {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };
    /**
     * The x-factor for the major grid of the grid.
     */
    private final IntegerProperty majorY = new SimpleIntegerProperty(this, "major-y", 5) {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };

    private final Path minorNode = new Path();
    private final Group node = new Group();
    /**
     * Whether to snap to the grid.
     */
    private final BooleanProperty snapToGrid = new SimpleBooleanProperty(this, "snapToGrid", true) {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };
    /**
     * Width of a grid cell. The value 0 turns the constrainer off for the
     * horizontal axis.
     */
    private final ObjectProperty<CssSize> width = new SimpleObjectProperty<CssSize>(this, "width") {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };
    /**
     * The x-origin of the grid.
     */
    private final ObjectProperty<CssSize> x = new SimpleObjectProperty<CssSize>(this, "x") {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };
    /**
     * The y-origin of the grid.
     */
    private final ObjectProperty<CssSize> y = new SimpleObjectProperty<CssSize>(this, "y") {

        @Override
        public void invalidated() {
            fireInvalidated();
        }
    };

    /**
     * Creates a grid of 10x10 pixels at origin 0,0 and 22.5 degree rotations.
     */
    public GridConstrainer() {
        this(0, 0, 10, 10, 22.5, 5, 5);
    }

    /**
     * Creates a grid of width x height pixels at origin 0,0 and 22.5 degree
     * rotations.
     *
     * @param width  The width of the grid. 0 turns the grid of for the x-axis.
     * @param height The width of the grid. 0 turns the grid of for the y-axis.
     */
    public GridConstrainer(double width, double height) {
        this(0, 0, width, height, 22.5, 5, 5);
    }

    /**
     * Creates a grid with the specified constraints.
     *
     * @param x      The x-origin of the grid
     * @param y      The y-origin of the grid
     * @param width  The width of the grid. 0 turns the grid of for the x-axis.
     * @param height The width of the grid. 0 turns the grid of for the y-axis.
     * @param angle  The angular grid (in degrees). 0 turns the grid off for
     *               rotations.
     * @param majorx the interval for major grid lines on the x-axis
     * @param majory the interval for major grid lines on the y-axis
     */
    public GridConstrainer(double x, double y, double width, double height, double angle, int majorx, int majory) {
        this(CssSize.from(x), CssSize.from(y), CssSize.from(width), CssSize.from(height), angle, majorx, majory);
    }

    public GridConstrainer(CssSize x, CssSize y, CssSize width, CssSize height, double angle, int majorx, int majory) {
        this.x.set(x);
        this.y.set(y);
        this.width.set(width);
        this.height.set(height);
        this.angle.set(angle);
        this.minorNode.getStyleClass().setAll(STYLECLASS_CONSTRAINER_MINOR_GRID);
        this.majorNode.getStyleClass().setAll(STYLECLASS_CONSTRAINER_MAJOR_GRID);
        this.majorX.set(majorx);
        this.majorY.set(majory);

        node.getChildren().addAll(minorNode, majorNode);
    }

    public @NonNull DoubleProperty angleProperty() {
        return angle;
    }

    private boolean canSnapToGrid() {
        return snapToGrid.get() && getWidth().getValue() > 0 && getHeight().getValue() > 0;
    }

    public @NonNull BooleanProperty drawGridProperty() {
        return drawGrid;
    }

    public CssColor getGridColor() {
        return gridColorProperty.getValue();
    }

    public void setGridColor(CssColor newValue) {
        gridColorProperty.setValue(newValue);
    }

    public CssSize getHeight() {
        return height.get();
    }

    public int getMajorX() {
        return majorX.get();
    }

    public int getMajorY() {
        return majorY.get();
    }

    @Override
    public @NonNull Node getNode() {
        return node;
    }

    public CssSize getWidth() {
        return width.get();
    }

    public CssSize getX() {
        return x.get();
    }

    public CssSize getY() {
        return y.get();
    }

    public @NonNull Property<CssColor> gridColorProperty() {
        return gridColorProperty;
    }

    public @NonNull ObjectProperty<CssSize> heightProperty() {
        return height;
    }

    public @NonNull IntegerProperty majorXProperty() {
        return majorX;
    }

    public @NonNull IntegerProperty majorYProperty() {
        return majorY;
    }

    public @NonNull BooleanProperty snapToGridProperty() {
        return snapToGrid;
    }

    @Override
    public double translateAngle(Figure f, double angle, double dir) {
        if (!snapToGrid.get()) {
            return angle;
        }

        double cAngle = this.angle.get();

        if (cAngle == 0) {
            return angle;
        }

        double ta = angle / cAngle;

        if (Double.isNaN(dir) || dir == 0) {
            ta = round(ta);
        } else if (dir < 0) {
            ta = floor(ta + 1);
        } else {
            ta = ceil(ta - 1);
        }

        double result = (ta * cAngle) % 360;
        return result < 0 ? 360 + result : result;
    }

    @Override
    public @NonNull CssPoint2D translatePoint(Figure f, @NonNull CssPoint2D cssp, @NonNull CssPoint2D dir) {
        if (!canSnapToGrid()) {
            Point2D p = cssp.getConvertedValue();
            Point2D covertedDir = dir.getConvertedValue();
            return new CssPoint2D(p.add(covertedDir));
        }

        DefaultUnitConverter c = DefaultUnitConverter.getInstance();
        String wunits = this.width.get().getUnits();
        String hunits = this.height.get().getUnits();
        double px = c.convert(cssp.getX(), wunits);
        double py = c.convert(cssp.getY(), hunits);

        double cx = c.convert(this.x.get(), wunits);
        double cy = c.convert(this.y.get(), hunits);
        double cwidth = this.width.get().getValue();
        double cheight = this.height.get().getValue();

        double tx = (cwidth == 0) ? px : (px - cx) / cwidth;
        double ty = (cheight == 0) ? py : (py - cy) / cheight;

        if (dir.getX().getValue() > 0) {
            tx = floor(tx + 1);
        } else if (dir.getX().getValue() < 0) {
            tx = ceil(tx - 1);
        } else {
            tx = round(tx);
        }
        if (dir.getY().getValue() > 0) {
            ty = ceil(ty);
        } else if (dir.getY().getValue() < 0) {
            ty = floor(ty);
        } else {
            ty = round(ty);
        }


        double x = Geom.fma(tx, cwidth, cx);
        double y = Geom.fma(ty, cheight, cy);
        return new CssPoint2D(CssSize.from(x, wunits), CssSize.from(y, hunits));
    }

    @Override
    public @NonNull CssRectangle2D translateRectangle(Figure f, @NonNull CssRectangle2D cssr, @NonNull CssPoint2D cssdir) {
        if (!canSnapToGrid()) {
            Rectangle2D r = cssr.getConvertedValue();
            Point2D dir = cssdir.getConvertedValue();
            return new CssRectangle2D(r.getMinX() + dir.getX(), r.getMinY() + dir.getY(), r.getWidth(), r.getHeight());
        }

        Rectangle2D r = cssr.getConvertedValue();
        Point2D dir = cssdir.getConvertedValue();

        double cx = this.x.get().getConvertedValue();
        double cy = this.y.get().getConvertedValue();
        double cwidth = this.width.get().getConvertedValue();
        double cheight = this.height.get().getConvertedValue();

        double tx = (cwidth == 0) ? r.getMinX() : (r.getMinX() - cx) / cwidth;
        double ty = (cheight == 0) ? r.getMinY() : (r.getMinY() - cy) / cheight;
        double tmaxx = (cwidth == 0) ? r.getMaxX() : (r.getMaxX() - cx) / cwidth;
        double tmaxy = (cheight == 0) ? r.getMaxY() : (r.getMaxY() - cy) / cheight;

        if (dir.getX() > 0) {
            tx += floor(tmaxx + 1) - tmaxx;
        } else if (dir.getX() < 0) {
            tx = ceil(tx - 1);
        } else {
            tx = round(tx);
        }
        if (dir.getY() > 0) {
            ty += floor(tmaxy + 1) - tmaxy;
        } else if (dir.getY() < 0) {
            ty = ceil(ty - 1);
        } else {
            ty = round(ty);
        }

        return new CssRectangle2D(
                Geom.fma(tx, cwidth, cx),
                Geom.fma(ty, cheight, cy), r.getWidth(), r.getHeight());
    }

    @Override
    public void updateNode(@NonNull DrawingView drawingView) {
        ObservableList<PathElement> minor = minorNode.getElements();
        ObservableList<PathElement> major = majorNode.getElements();
        minor.clear();
        major.clear();
        CssColor gridColor = getGridColor();
        minorNode.setStroke(gridColor == null ? null : gridColor.getColor());
        majorNode.setStroke(gridColor == null ? null : gridColor.getColor());
        minorNode.setStrokeWidth(0.5);
        majorNode.setStrokeWidth(1.0);

        Drawing drawing = drawingView.getDrawing();
        final double dx, dy, dw, dh;
        if (drawing instanceof ViewBoxableDrawing) {
            dx = drawing.getNonNull(ViewBoxableDrawing.VIEW_BOX_X).getConvertedValue();
            dy = drawing.getNonNull(ViewBoxableDrawing.VIEW_BOX_Y).getConvertedValue();
            dw = drawing.getNonNull(ViewBoxableDrawing.WIDTH).getConvertedValue();
            dh = drawing.getNonNull(ViewBoxableDrawing.HEIGHT).getConvertedValue();
        } else {
            dx = 0;
            dy = 0;
            dw = drawing.getNonNull(Drawing.WIDTH).getConvertedValue();
            dh = drawing.getNonNull(Drawing.HEIGHT).getConvertedValue();
        }
        Bounds visibleRect = drawingView.viewToWorld(drawingView.getVisibleRect());
        //Bounds visibleRect = FXGeom.intersection(drawingView.viewToWorld(drawingView.getVisibleRect()), new BoundingBox(-dx,-dy,dw,dh));

        if (drawGrid.get()) {
            Transform t = drawingView.getWorldToView();

            t = t.createConcatenation(drawing.getLocalToParent());

            double gx0 = x.get().getConvertedValue();
            double gy0 = y.get().getConvertedValue();
            double gxdelta = Math.abs(width.get().getConvertedValue());
            double gydelta = Math.abs(height.get().getConvertedValue());
            if (gx0 < 0) {
                gx0 = gx0 % gxdelta + gxdelta;
            }
            if (gy0 < 0) {
                gy0 = gy0 % gydelta + gydelta;
            }

            int gmx = Math.max(0, Math.abs(majorX.get()));
            int gmy = Math.max(0, Math.abs(majorY.get()));

            // render minor
            Point2D scaled = t.deltaTransform(gxdelta, gydelta);
            if (scaled.getX() > 2 && gmx != 1) {
                final int start = (int) ceil((max(dx, visibleRect.getMinX()) - gx0) / gxdelta);
                final int end = (int) ceil((min(dw + dx, visibleRect.getMaxX()) - gx0) / gxdelta);
                for (int i = start; i < end; i++) {
                    if (gmx > 0 && i % gmx == 0) {
                        continue;
                    }
                    double x = gx0 + i * gxdelta;
                    double x1 = x;
                    double y1 = dy;
                    double x2 = x;
                    double y2 = dh + dy;

                    Point2D p1 = t.transform(x1, y1);
                    Point2D p2 = t.transform(x2, y2);
                    minor.add(new MoveTo(Math.round(p1.getX()) + 0.5, p1.getY()));
                    minor.add(new LineTo(Math.round(p2.getX()) + 0.5, p2.getY()));
                }
            }
            if (scaled.getY() > 2 && gmy != 1) {
                final int start = (int) ceil((max(dy, visibleRect.getMinY()) - gy0) / gydelta);
                final int end = (int) Math.ceil((min(dh + dy, visibleRect.getMaxY()) - gy0) / gydelta);
                for (int i = start; i < end; i++) {
                    if (gmy > 0 && i % gmy == 0) {
                        continue;
                    }
                    double y = gy0 + i * gydelta;
                    double x1 = dx;
                    double y1 = y;
                    double x2 = dw + dx;
                    double y2 = y;

                    Point2D p1 = t.transform(x1, y1);
                    Point2D p2 = t.transform(x2, y2);
                    minor.add(new MoveTo(p1.getX(), Math.round(p1.getY()) + 0.5));
                    minor.add(new LineTo(p2.getX(), Math.round(p2.getY()) + 0.5));
                }
            }

            // render major
            double gmydelta = gydelta * gmy;
            double gmxdelta = gxdelta * gmx;
            scaled = t.deltaTransform(gmxdelta, gmydelta);
            if (scaled.getX() > 2) {
                final int start = (int) ceil((max(dx, visibleRect.getMinX()) - gx0) / gmxdelta);
                final int end = (int) ceil((min(dw + dx, visibleRect.getMaxX()) - gx0) / gmxdelta);
                for (int i = start; i < end; i++) {
                    double x = gx0 + i * gmxdelta;
                    double x1 = x;
                    double y1 = dy;
                    double x2 = x;
                    double y2 = dh + dy;

                    Point2D p1 = t.transform(x1, y1);
                    Point2D p2 = t.transform(x2, y2);
                    major.add(new MoveTo(Math.round(p1.getX()) + 0.5, p1.getY()));
                    major.add(new LineTo(Math.round(p2.getX()) + 0.5, p2.getY()));
                }
            }
            if (scaled.getY() > 2) {
                final int start = (int) ceil((max(dy, visibleRect.getMinY()) - gy0) / gmydelta);
                final int end = (int) Math.ceil((min(dh + dy, visibleRect.getMaxY()) - gy0) / gmydelta);
                for (int i = start; i < end; i++) {
                    double y = gy0 + i * gmydelta;
                    double x1 = dx;
                    double y1 = y;
                    double x2 = dw + dx;
                    double y2 = y;

                    Point2D p1 = t.transform(x1, y1);
                    Point2D p2 = t.transform(x2, y2);
                    major.add(new MoveTo(p1.getX(), Math.round(p1.getY()) + 0.5));
                    major.add(new LineTo(p2.getX(), Math.round(p2.getY()) + 0.5));
                }
            }
        }
    }

    public @NonNull ObjectProperty<CssSize> widthProperty() {
        return width;
    }

    public @NonNull ObjectProperty<CssSize> xProperty() {
        return x;
    }

    public @NonNull ObjectProperty<CssSize> yProperty() {
        return y;
    }

}
