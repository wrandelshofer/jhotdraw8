/*
 * @(#)FXTransformPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * TransformPathBuilder.
 *
 * @param <T> the product type
 * @author Werner Randelshofer
 */
public class AffineTransformPathBuilder<T> extends AbstractPathBuilder<T> {

    private final PathBuilder<T> target;
    private AffineTransform transform;

    public AffineTransformPathBuilder(PathBuilder<T> target) {
        this(target, new AffineTransform());
    }

    public AffineTransformPathBuilder(PathBuilder<T> target, AffineTransform transform) {
        this.target = target;
        this.transform = transform;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        target.closePath();
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x3, double y3) {
        Point2D p1 = transform.transform(new Point2D.Double(x1, y1), new Point2D.Double());
        Point2D p2 = transform.transform(new Point2D.Double(x2, y2), new Point2D.Double());
        Point2D p3 = transform.transform(new Point2D.Double(x3, y3), new Point2D.Double());
        target.curveTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        Point2D p = transform.transform(new Point2D.Double(x, y), new Point2D.Double());
        target.lineTo(p.getX(), p.getY());
    }

    @Override
    protected void doMoveTo(double x, double y) {
        Point2D p = transform.transform(new Point2D.Double(x, y), new Point2D.Double());
        target.moveTo(p.getX(), p.getY());
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x2, double y2) {
        Point2D p1 = transform.transform(new Point2D.Double(x1, y1), new Point2D.Double());
        Point2D p2 = transform.transform(new Point2D.Double(x2, y2), new Point2D.Double());
        target.quadTo(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

    @Override
    public @Nullable T build() {
        return target.build();
    }
}
