/*
 * @(#)BoundingBoxBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import javafx.geometry.BoundingBox;
import javafx.scene.shape.Rectangle;
import org.jhotdraw8.annotation.NonNull;

import java.awt.geom.Rectangle2D;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Builds a bounding box path.
 *
 * @author Werner Randelshofer
 */
public class BoundingBoxBuilder extends AbstractPathBuilder<BoundingBox> {

    private double minx = Double.POSITIVE_INFINITY,
            miny = Double.POSITIVE_INFINITY,
            maxx = Double.NEGATIVE_INFINITY,
            maxy = Double.NEGATIVE_INFINITY;

    public BoundingBoxBuilder() {
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        // nothing to do
    }

    public void addToBounds(double x, double y) {
        minx = min(minx, x);
        miny = min(miny, y);
        maxx = max(maxx, x);
        maxy = max(maxy, y);
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x3, double y3) {
        for (double t : CubicCurveCharacteristics.extremePoints(lastX, lastY, x1, y1, x2, y2, x3, y3)) {
            PointAndDerivative eval = CubicCurves.eval(lastX, lastY, x1, y1, x2, y2, x3, y3, t);
            addToBounds(eval.x(), eval.y());
        }
        addToBounds(x3, y3);
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        addToBounds(x, y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        addToBounds(x, y);
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x2, double y2) {
        for (double t : QuadCurveCharacteristics.extremePoints(lastX, lastY, x1, y1, x2, y2)) {
            PointAndDerivative eval = QuadCurves.eval(lastX, lastY, x1, y1, x2, y2, t);
            addToBounds(eval.x(), eval.y());
        }
        addToBounds(x2, y2);
    }

    public @NonNull Rectangle buildRectangle() {
        if (Double.isNaN(minx)) {
            return new Rectangle(0, 0, 0, 0);
        }
        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    public Rectangle2D.@NonNull Double buildRectangle2D() {
        if (Double.isNaN(minx)) {
            return new Rectangle2D.Double(0, 0, 0, 0);
        }
        return new Rectangle2D.Double(minx, miny, maxx - minx, maxy - miny);
    }

    @Override
    public @NonNull BoundingBox build() {
        if (Double.isNaN(minx)) {
            return new BoundingBox(0, 0, 0, 0);
        }
        return new BoundingBox(minx, miny, maxx - minx, maxy - miny);
    }

    public boolean isFinite() {
        return Double.isFinite(minx) && Double.isFinite(miny)
                && Double.isFinite(maxx) && Double.isFinite(maxy);
    }
}
