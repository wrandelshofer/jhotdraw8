/*
 * @(#)FXPolygonBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a list of {@link Double}s for a {@link Polygon}.
 */
public class FXPolygonBuilder extends AbstractPathBuilder<List<Double>> {
    private final List<Double> poly = new ArrayList<>();

    public FXPolygonBuilder() {
    }

    @Override
    public List<Double> build() {
        return poly;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        poly.add(x);
        poly.add(y);
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        poly.add(x);
        poly.add(y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        poly.add(x);
        poly.add(y);
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        poly.add(x);
        poly.add(y);
    }

}
