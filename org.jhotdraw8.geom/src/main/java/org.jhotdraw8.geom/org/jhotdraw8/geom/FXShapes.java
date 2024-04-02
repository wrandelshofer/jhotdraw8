/*
 * @(#)FXShapes.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Text;
import javafx.scene.transform.MatrixType;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides conversion methods between AWT shapes and JavaFX shapes.
 *
 * @author Werner Randelshofer
 */
public class FXShapes {


    private static final Logger LOGGER = Logger.getLogger(FXShapes.class.getName());

    /**
     * Don't let anyone instantiate this class.
     */
    private FXShapes() {
    }

    public static int fxLineCapToAwtLineCap(@Nullable StrokeLineCap cap) {
        if (cap == null) {
            return BasicStroke.CAP_BUTT;
        }
        return switch (cap) {
            default -> BasicStroke.CAP_BUTT;
            case ROUND -> BasicStroke.CAP_ROUND;
            case SQUARE -> BasicStroke.CAP_SQUARE;
        };
    }

    public static int fxLineJoinToAwtLineJoin(@Nullable StrokeLineJoin join) {
        if (join == null) {
            return BasicStroke.JOIN_BEVEL;
        }
        return switch (join) {
            default -> BasicStroke.JOIN_BEVEL;
            case MITER -> BasicStroke.JOIN_MITER;
            case ROUND -> BasicStroke.JOIN_ROUND;
        };
    }

    public static PathIterator fxPathToAwtPathIterator(@NonNull Path path, @Nullable AffineTransform tx) {
        PathBuilder<PathIterator> b = new PathIteratorPathBuilder(path.getFillRule() == FillRule.NON_ZERO ? PathIterator.WIND_NON_ZERO : PathIterator.WIND_EVEN_ODD);
        if (tx != null && !tx.isIdentity()) {
            b = new AffineTransformPathBuilder<>(b, tx);
        }
        FXSvgPaths.buildFromPathElements(b, path.getElements());
        return b.build();
    }

    public static PathIterator fxPathElementsToAwtPathIterator(@NonNull Iterable<PathElement> pathElements, int windingRule, @Nullable AffineTransform tx) {
        final PathIteratorPathBuilder b = new PathIteratorPathBuilder(windingRule);
        FXSvgPaths.buildFromPathElements(b, pathElements);
        return b.build();
    }

    public static @NonNull PathIterator fxPointsToAwtPathIterator(@NonNull List<Point2D> points, boolean closed, int windingRule, @Nullable AffineTransform tx) {
        return new PathIterator() {
            final float @NonNull [] srcf = new float[2];
            final double @NonNull [] srcd = new double[2];
            private final int size = points.size();
            int index = 0;

            @Override
            public int currentSegment(float[] coords) {
                if (index < size) {
                    Point2D p = points.get(index);
                    if (tx == null) {
                        coords[0] = (float) p.getX();
                        coords[1] = (float) p.getY();
                    } else {
                        srcf[0] = (float) p.getX();
                        srcf[1] = (float) p.getY();
                        tx.transform(srcf, 0, coords, 0, 1);
                    }
                    return index == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
                } else if (index == size && closed) {
                    return PathIterator.SEG_CLOSE;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public int currentSegment(double[] coords) {
                if (index < size) {
                    Point2D p = points.get(index);
                    if (tx == null) {
                        coords[0] = p.getX();
                        coords[1] = p.getY();
                    } else {
                        srcd[0] = p.getX();
                        srcd[1] = p.getY();
                        tx.transform(srcd, 0, coords, 0, 1);
                    }
                    return index == 0 ? PathIterator.SEG_MOVETO : PathIterator.SEG_LINETO;
                } else if (index == size && closed) {
                    return PathIterator.SEG_CLOSE;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public int getWindingRule() {
                return windingRule;
            }

            @Override
            public boolean isDone() {
                return index >= size + (closed ? 1 : 0);
            }

            @Override
            public void next() {
                if (index < size + (closed ? 1 : 0)) {
                    index++;
                }
            }

        };
    }

    /**
     * Converts a JavaFX shape to a AWT shape.
     * <p>
     * If conversion fails, returns a Rectangle.Double with the layout bounds
     * of the shape.
     *
     * @param fx A JavaFX shape
     * @return AWT Shape or Rectangle
     */
    public static Shape fxShapeToAwtShape(javafx.scene.shape.Shape fx) {
        switch (fx) {
            case Arc arc -> {
                return fxArcToAwtShape(arc);
            }
            case Circle circle -> {
                return fxCircleToAwtShape(circle);
            }
            case CubicCurve cubicCurve -> {
                return fxCubicCurveToAwtShape(cubicCurve);
            }
            case Ellipse ellipse -> {
                return fxEllipseToAwtShape(ellipse);
            }
            case Line line -> {
                return fxLineToAwtShape(line);
            }
            case Path path -> {
                return fxPathToAwtShape(path);
            }
            case Polygon polygon -> {
                return fxPolygonToAwtShape(polygon);
            }
            case Polyline polyline -> {
                return fxPolylineToAwtShape(polyline);
            }
            case QuadCurve quadCurve -> {
                return fxQuadCurveToAwtShape(quadCurve);
            }
            case Rectangle rectangle -> {
                return fxRectangleToAwtShape(rectangle);
            }
            case SVGPath svgPath -> {
                try {
                    return fxSvgPathToAwtShape(svgPath);
                } catch (ParseException e) {
                    LOGGER.warning("Illegal path: " + e.getMessage());
                    Bounds b = fx.getLayoutBounds();
                    return new Rectangle2D.Double(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
                }
            }
            case Text text -> {
                return fxTextToAwtShape(text);
            }
            case null -> {
                LOGGER.warning("shape is null");
                return new Rectangle2D.Double(0, 0, 0, 0);
            }
            default -> {
                LOGGER.warning("Unsupported shape type: " + fx);
                Bounds b = fx.getLayoutBounds();
                return new Rectangle2D.Double(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
            }
        }
    }

    private static @NonNull Shape fxArcToAwtShape(@NonNull Arc node) {
        double centerX = node.getCenterX();
        double centerY = node.getCenterY();
        double radiusX = node.getRadiusX();
        double radiusY = node.getRadiusY();
        double startAngle = Math.toRadians(-node.getStartAngle());
        double endAngle = Math.toRadians(-node.getStartAngle() - node.getLength());
        double length = node.getLength();

        double startX = radiusX * Math.cos(startAngle);
        double startY = radiusY * Math.sin(startAngle);

        double endX = centerX + radiusX * Math.cos(endAngle);
        double endY = centerY + radiusY * Math.sin(endAngle);

        int xAxisRot = 0;
        boolean largeArc = (length > 180);
        boolean sweep = (length < 0);

        SvgPath2D p = new SvgPath2D();
        p.moveTo(centerX, centerY);

        if (ArcType.ROUND == node.getType()) {
            p.lineTo(startX, startY);
        }

        p.arcTo(radiusX, radiusY, xAxisRot, endX, endY, largeArc, sweep);

        if (ArcType.CHORD == node.getType()
                || ArcType.ROUND == node.getType()) {
            p.closePath();
        }
        return p;
    }

    public static @NonNull Shape fxBoundsToAwtShape(@NonNull Bounds node) {
        return new Rectangle2D.Double(
                node.getMinX(),
                node.getMinY(),
                node.getWidth(),
                node.getHeight()
        );
    }

    private static @NonNull Shape fxCircleToAwtShape(@NonNull Circle node) {
        double x = node.getCenterX();
        double y = node.getCenterY();
        double r = node.getRadius();
        return new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
    }

    private static @NonNull Shape fxCubicCurveToAwtShape(@NonNull CubicCurve e) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(e.getStartX(), e.getStartY());
        p.curveTo(e.getControlX1(), e.getControlY1(), e.getControlX2(), e.getControlY2(), e.getEndX(), e.getEndY());
        //XXX this method is only available since Java SE 11
        //p.trimToSize();
        return p;
    }

    private static @NonNull Shape fxEllipseToAwtShape(@NonNull Ellipse node) {
        double x = node.getCenterX();
        double y = node.getCenterY();
        double rx = node.getRadiusX();
        double ry = node.getRadiusY();
        return new Ellipse2D.Double(x - rx, y - ry, rx * 2, ry * 2);
    }

    private static @NonNull Shape fxLineToAwtShape(@NonNull Line node) {
        Line2D.Double p = new Line2D.Double(node.getStartX(), node.getStartY(), node.getEndX(), node.getEndY());
        return p;
    }

    private static @NonNull Shape fxPathToAwtShape(@NonNull Path node) {
        return fxPathELementsToAwtShape(node.getElements(), node.getFillRule());
    }

    public static @NonNull Shape fxPathELementsToAwtShape(@NonNull Iterable<PathElement> pathElements, FillRule fillRule) {
        Path2D.Double p = buildFromPathElements(new AwtPathBuilder(), pathElements).build();
        p.setWindingRule(fillRule == FillRule.NON_ZERO ? PathIterator.WIND_NON_ZERO : PathIterator.WIND_EVEN_ODD);
        return p;
    }

    public static @NonNull <T extends PathBuilder<?>> T buildFromPathElements(@NonNull T p, @NonNull Iterable<PathElement> pathElements) {
        double x = 0;
        double y = 0;
        for (PathElement pe : pathElements) {
            if (pe instanceof MoveTo e) {
                if (e.isAbsolute()) {
                    x = e.getX();
                    y = e.getY();
                } else {
                    x += e.getX();
                    y += e.getY();
                }
                p.moveTo(x, y);
            } else if (pe instanceof LineTo e) {
                if (e.isAbsolute()) {
                    x = e.getX();
                    y = e.getY();
                } else {
                    x += e.getX();
                    y += e.getY();
                }
                p.lineTo(x, y);
            } else if (pe instanceof CubicCurveTo e) {
                final double cx1, cy1, cx2, cy2;
                if (e.isAbsolute()) {
                    cx1 = e.getControlX1();
                    cy1 = e.getControlY1();
                    cx2 = e.getControlX2();
                    cy2 = e.getControlY2();
                    x = e.getX();
                    y = e.getY();
                } else {
                    cx1 = x + e.getControlX1();
                    cy1 = y + e.getControlY1();
                    cx2 = x + e.getControlX2();
                    cy2 = y + e.getControlY2();
                    x += e.getX();
                    y += e.getY();
                }
                p.curveTo(cx1, cy1, cx2, cy2, x, y);
            } else if (pe instanceof QuadCurveTo e) {
                final double cx, cy;
                if (e.isAbsolute()) {
                    cx = e.getControlX();
                    cy = e.getControlY();
                    x = e.getX();
                    y = e.getY();
                } else {
                    cx = x + e.getControlX();
                    cy = y + e.getControlY();
                    x += e.getX();
                    y += e.getY();
                }
                p.quadTo(cx, cy, x, y);
            } else if (pe instanceof ArcTo e) {
                if (e.isAbsolute()) {
                    x = e.getX();
                    y = e.getY();
                } else {
                    x += e.getX();
                    y += e.getY();
                }
                p.arcTo(e.getRadiusX(), e.getRadiusY(), e.getXAxisRotation(), x, y, e.isLargeArcFlag(), e.isSweepFlag());
            } else if (pe instanceof HLineTo e) {
                if (e.isAbsolute()) {
                    x = e.getX();
                } else {
                    x += e.getX();
                }
                p.lineTo(x, y);
            } else if (pe instanceof VLineTo e) {
                if (e.isAbsolute()) {
                    y = e.getY();
                } else {
                    y += e.getY();
                }
                p.lineTo(x, y);
            } else if (pe instanceof ClosePath) {
                p.closePath();
            }
        }
        return p;
    }

    private static @NonNull Shape fxPolygonToAwtShape(@NonNull Polygon node) {
        Path2D.Double p = new Path2D.Double();
        List<Double> ps = node.getPoints();
        for (int i = 0, n = ps.size(); i < n; i += 2) {
            if (i == 0) {
                p.moveTo(ps.get(i), ps.get(i + 1));
            } else {
                p.lineTo(ps.get(i), ps.get(i + 1));
            }
        }
        p.closePath();
        //XXX this method is only available since Java SE 11
        //p.trimToSize();
        return p;
    }

    private static @NonNull Shape fxPolylineToAwtShape(@NonNull Polyline node) {
        Path2D.Double p = new Path2D.Double();
        List<Double> ps = node.getPoints();
        for (int i = 0, n = ps.size(); i < n; i += 2) {
            if (i == 0) {
                p.moveTo(ps.get(i), ps.get(i + 1));
            } else {
                p.lineTo(ps.get(i), ps.get(i + 1));
            }
        }
        //XXX this method is only available since Java SE 11
        //p.trimToSize();
        return p;
    }

    private static @NonNull Shape fxQuadCurveToAwtShape(@NonNull QuadCurve node) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(node.getStartX(), node.getStartY());
        p.quadTo(node.getControlX(), node.getControlY(), node.getEndX(), node.getEndY());
        //XXX this method is only available since Java SE 11
        //p.trimToSize();
        return p;
    }

    public static @NonNull Shape fxRectangleToAwtShape(@NonNull Rectangle node) {
        if (node.getArcHeight() == 0 && node.getArcWidth() == 0) {
            return new Rectangle2D.Double(
                    node.getX(),
                    node.getY(),
                    node.getWidth(),
                    node.getHeight()
            );

        } else {
            return new RoundRectangle2D.Double(
                    node.getX(),
                    node.getY(),
                    node.getWidth(),
                    node.getHeight(),
                    node.getArcWidth(),
                    node.getArcHeight()
            );
        }
    }

    private static Shape fxSvgPathToAwtShape(@NonNull SVGPath node) throws ParseException {
        AwtPathBuilder b = new AwtPathBuilder();
        SvgPaths.svgStringToBuilder(node.getContent(), b);
        return b.build();
    }

    private static @NonNull Shape fxTextToAwtShape(@NonNull Text node) {
        Path path = (Path) javafx.scene.shape.Shape.subtract(node, new Rectangle());
        return fxPathToAwtShape(path);
    }

    /**
     * Converts a Java Path iterator to a JavaFX shape.
     *
     * @param fxT A JavaFX Transform.
     * @return An AWT Transform.
     */
    public static @Nullable AffineTransform fxTransformToAwtTransform(@Nullable Transform fxT) {
        if (fxT == null) {
            return null;
        }

        double[] m = fxT.toArray(MatrixType.MT_2D_2x3);
        return new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
    }

    /**
     * Converts a Java Path iterator to a JavaFX shape.
     *
     * @param iter AWT Path Iterator
     * @return JavaFX Shape
     */
    public static @NonNull List<PathElement> fxPathElementsFromAwt(@NonNull PathIterator iter) {
        List<PathElement> fxelem = new ArrayList<>();
        double[] coords = new double[6];
        for (; !iter.isDone(); iter.next()) {
            switch (iter.currentSegment(coords)) {
                case PathIterator.SEG_CLOSE:
                    fxelem.add(new ClosePath());
                    break;
                case PathIterator.SEG_CUBICTO:
                    fxelem.add(new CubicCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
                    break;
                case PathIterator.SEG_LINETO:
                    fxelem.add(new LineTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_MOVETO:
                    fxelem.add(new MoveTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    fxelem.add(new QuadCurveTo(coords[0], coords[1], coords[2], coords[3]));
                    break;
            }
        }
        return fxelem;
    }

    /**
     * Converts a Java AWT Shape iterator to a JavaFX Shape.
     *
     * @param shape AWT Shape
     * @param fxT   Optional transformation which is applied to the shape
     * @return JavaFX Shape
     */
    public static @NonNull Path fxShapeFromAwt(@NonNull Shape shape, Transform fxT) {
        return fxShapeFromAwt(shape.getPathIterator(fxTransformToAwtTransform(fxT)));
    }

    /**
     * Converts a Java AWT Shape iterator to a JavaFX Shape.
     *
     * @param shape AWT Shape
     * @param at    Optional transformation which is applied to the shape
     * @return JavaFX Shape
     */
    public static @NonNull Path fxShapeFromAwt(@NonNull Shape shape, AffineTransform at) {
        return fxShapeFromAwt(shape.getPathIterator(at));
    }

    /**
     * Converts a Java AWT Shape iterator to a JavaFX Shape.
     *
     * @param shape AWT Shape
     * @return JavaFX Shape
     */
    public static @NonNull Path fxShapeFromAwt(@NonNull Shape shape) {
        return fxShapeFromAwt(shape.getPathIterator(null));
    }

    /**
     * Converts a Java Path iterator to a JavaFX shape.
     *
     * @param iter AWT Path Iterator
     * @return JavaFX Shape
     */
    public static @NonNull Path fxShapeFromAwt(@NonNull PathIterator iter) {
        Path fxpath = new Path();

        switch (iter.getWindingRule()) {
            case PathIterator.WIND_EVEN_ODD:
                fxpath.setFillRule(FillRule.EVEN_ODD);
                break;
            case PathIterator.WIND_NON_ZERO:
                fxpath.setFillRule(FillRule.NON_ZERO);
                break;
            default:
                throw new IllegalArgumentException("illegal winding rule " + iter.getWindingRule());
        }

        fxpath.getElements().addAll(fxPathElementsFromAwt(iter));

        return fxpath;
    }
}
