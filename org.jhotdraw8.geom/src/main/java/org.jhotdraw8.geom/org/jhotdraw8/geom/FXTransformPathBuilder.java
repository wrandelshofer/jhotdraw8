/*
 * @(#)FXTransformPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;

/**
 * TransformPathBuilder.
 *
 * @param <T> the product type
 */
public class FXTransformPathBuilder<T> extends AbstractPathBuilder<T> {

    private final PathBuilder<T> target;
    private Transform transform;

    public FXTransformPathBuilder(PathBuilder<T> target) {
        this(target, FXTransforms.IDENTITY);
    }

    public FXTransformPathBuilder(PathBuilder<T> target, Transform transform) {
        this.target = target;
        this.transform = transform;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        target.closePath();
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x3, double y3) {
        Point2D p1 = transform.transform(x1, y1);
        Point2D p2 = transform.transform(x2, y2);
        Point2D p3 = transform.transform(x3, y3);
        target.curveTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
    }


    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        Point2D p = transform.transform(x, y);
        target.lineTo(p.getX(), p.getY());
    }

    @Override
    protected void doMoveTo(double x, double y) {
        Point2D p = transform.transform(x, y);
        target.moveTo(p.getX(), p.getY());
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x2, double y2) {
        Point2D p1 = transform.transform(x1, y1);
        Point2D p2 = transform.transform(x2, y2);
        target.quadTo(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    @Override
    public T build() {
        return target.build();
    }
}
