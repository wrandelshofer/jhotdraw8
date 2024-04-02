/*
 * @(#)TransformFlattener.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * TransformFlattener.
 *
 * @author Werner Randelshofer
 */
public class TransformFlattener {

    public TransformFlattener() {
    }

    private boolean canFlattenTranslate(@NonNull Node node) {
        if (node.getRotate() != 0.0
                || node.getScaleX() != 1.0
                || node.getScaleY() != 1.0) {
            return false;
        }
        for (Transform t : node.getTransforms()) {
            if (!(t instanceof Translate)) {
                return false;
            }
        }
        return true;

    }

    /**
     * Removes the translate transforms of a node and clears the translateX,
     * translateY properties.
     * <p>
     * If the node has a clip - flattens the transforms of the clip.
     *
     * @param node
     * @return Returns the summed up translate of the node.
     */

    private @NonNull Translate flattenTranslateTransforms(@NonNull Node node) {
        Translate translate = new Translate(node.getTranslateX(), node.getTranslateY());
        for (Transform t : node.getTransforms()) {
            if ((t instanceof Translate tt)) {
                translate = (Translate) translate.createConcatenation(tt);
            } else {
                throw new IllegalArgumentException("node has non-translate transforms.");
            }
        }
        node.setTranslateX(0.0);
        node.setTranslateY(0.0);
        node.getTransforms().clear();

        if (node.getClip() instanceof Shape s) {
            node.setClip(FXShapes.awtShapeToFXShape(FXShapes.fxShapeToAwtShape(s)
                    .getPathIterator(FXShapes.fxTransformToAwtTransform(translate))));
        }

        return translate;
    }

    /**
     * Tries to get rid of Translation transforms on a Node, by applying it to
     * descendants of the node or by adjusting the coordinates of the node.
     *
     * @param node a node
     */
    public void flattenTranslates(Node node) {
        if (node instanceof Parent) {
            flattenTranslatesInParent((Parent) node);
        } else if (node instanceof Shape) {
            flattenTranslatesInShape((Shape) node);
        }
    }

    private void flattenTranslatesInParent(@NonNull Parent parent) {
        if (!canFlattenTranslate(parent)) {
            return;
        }

        Translate translate = flattenTranslateTransforms(parent);

        // apply translation to children
        if (!translate.isIdentity()) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                child.getTransforms().addFirst(translate);
            }
        }
        // try to flatten the children
        for (Node child : parent.getChildrenUnmodifiable()) {
            flattenTranslates(child);
        }

    }

    private void flattenTranslatesInPath(@NonNull Path path) {
        if (!canFlattenTranslate(path)) {
            return;
        }
        Translate t = flattenTranslateTransforms(path);
        double tx = t.getTx();
        double ty = t.getTy();
        boolean first = true;

        // We must clone the PathElements, because they might be shared with
        // other Path objects.
        List<PathElement> list = new ArrayList<>(path.getElements().size());
        for (PathElement element : path.getElements()) {
            if (element.isAbsolute() || first) {
                switch (element) {
                    case ClosePath closePath -> list.add(element);
                    case MoveTo e -> list.add(new MoveTo(e.getX() + tx, e.getY() + ty));
                    case LineTo e -> list.add(new LineTo(e.getX() + tx, e.getY() + ty));
                    case QuadCurveTo e ->
                            list.add(new QuadCurveTo(e.getControlX() + tx, e.getControlY() + ty, e.getX() + tx, e.getY() + ty));
                    case CubicCurveTo e ->
                            list.add(new CubicCurveTo(e.getControlX1() + tx, e.getControlY1() + ty, e.getControlX2() + tx, e.getControlY2() + ty, e.getX() + tx, e.getY() + ty));
                    case ArcTo e -> list.add(new ArcTo(e.getRadiusX(), e.getRadiusY(),
                            e.getXAxisRotation(),
                            e.getX() + tx, e.getY() + ty,
                            e.isLargeArcFlag(),
                            e.isSweepFlag()));
                    default -> throw new UnsupportedOperationException("unknown element type: " + element);
                }
            } else {
                list.add(element);
            }
            first = false;
        }
        path.getElements().setAll(list);
    }

    private void flattenTranslatesInPolygon(@NonNull Polygon path) {
        if (!canFlattenTranslate(path)) {
            return;
        }
        Translate t = flattenTranslateTransforms(path);
        ObservableList<Double> points = path.getPoints();
        for (int i = 0, n = points.size(); i < n; i += 2) {
            double x = points.get(i);
            double y = points.get(i + 1);
            Point2D p = t.transform(x, y);
            points.set(i, p.getX());
            points.set(i + 1, p.getY());
        }
    }

    private void flattenTranslatesInPolyline(@NonNull Polyline path) {
        if (!canFlattenTranslate(path)) {
            return;
        }
        Translate t = flattenTranslateTransforms(path);
        ObservableList<Double> points = path.getPoints();
        for (int i = 0, n = points.size(); i < n; i += 2) {
            double x = points.get(i);
            double y = points.get(i + 1);
            Point2D p = t.transform(x, y);
            points.set(i, p.getX());
            points.set(i + 1, p.getY());
        }
    }

    private void flattenTranslatesInLine(@NonNull Line shape) {
        if (!canFlattenTranslate(shape)) {
            return;
        }
        Translate t = flattenTranslateTransforms(shape);
        Point2D p = t.transform(shape.getStartX(), shape.getStartY());
        shape.setStartX(p.getX());
        shape.setStartY(p.getY());
        Point2D p2 = t.transform(shape.getEndX(), shape.getEndY());
        shape.setEndX(p2.getX());
        shape.setEndY(p2.getY());
    }

    private void flattenTranslatesInEllipse(@NonNull Ellipse shape) {
        if (!canFlattenTranslate(shape)) {
            return;
        }
        Translate t = flattenTranslateTransforms(shape);
        Point2D p = t.transform(shape.getCenterX(), shape.getCenterY());
        shape.setCenterX(p.getX());
        shape.setCenterY(p.getY());
    }

    private void flattenTranslatesInRectangle(@NonNull Rectangle shape) {
        if (!canFlattenTranslate(shape)) {
            return;
        }
        Translate t = flattenTranslateTransforms(shape);
        Point2D p = t.transform(shape.getX(), shape.getY());
        shape.setX(p.getX());
        shape.setY(p.getY());
    }

    private void flattenTranslatesInText(@NonNull Text shape) {
        if (!canFlattenTranslate(shape)) {
            return;
        }
        Translate t = flattenTranslateTransforms(shape);
        Point2D p = t.transform(shape.getX(), shape.getY());
        shape.setX(p.getX());
        shape.setY(p.getY());
    }

    private void flattenTranslatesInShape(Shape shape) {
        if (shape instanceof Path) {
            flattenTranslatesInPath((Path) shape);
        } else if (shape instanceof Polygon) {
            flattenTranslatesInPolygon((Polygon) shape);
        } else if (shape instanceof Polyline) {
            flattenTranslatesInPolyline((Polyline) shape);
        } else if (shape instanceof Line) {
            flattenTranslatesInLine((Line) shape);
        } else if (shape instanceof Ellipse) {
            flattenTranslatesInEllipse((Ellipse) shape);
        } else if (shape instanceof Rectangle) {
            flattenTranslatesInRectangle((Rectangle) shape);
        } else if (shape instanceof Text) {
            flattenTranslatesInText((Text) shape);
        }
        // FIXME implement more shapes
    }
}
