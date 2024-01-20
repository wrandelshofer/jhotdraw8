/*
 * @(#)PolyArcPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

import org.jhotdraw8.geom.AbstractPathBuilder;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Path builder for {@link PolyArcPath}.
 */
public class PolyArcPathBuilder extends AbstractPathBuilder<List<PolyArcPath>> {
    private final List<PolyArcPath> paths = new ArrayList<>();
    private PolyArcPath current;
    private double flatness = 0.125;

    public PolyArcPathBuilder() {
    }

    public double getFlatness() {
        return flatness;
    }

    public void setFlatness(double flatness) {
        this.flatness = flatness;
    }

    @Override
    public List<PolyArcPath> build() {
        return paths;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        if (current != null) {
            current.isClosed(true);
        }
    }

    @Override
    protected void doPathDone() {
        if (current != null) {
            paths.add(current);
            current = null;
        }
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        if (current == null) {
            current = new PolyArcPath();
        }
        PathIterator it = new CubicCurve2D.Double(lastX, lastY, x1, y1, x2, y2, x, y).getPathIterator(null, flatness);
        it.next();
        double[] coords = new double[8];
        while (!it.isDone()) {
            it.currentSegment(coords);
            lineTo(coords[0], coords[1]);
            it.next();
        }
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        if (current == null) {
            current = new PolyArcPath();
        }
        current.addVertex(x, y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        if (current != null) {
            paths.add(current);
        }
        current = new PolyArcPath();
        current.addVertex(x, y);
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        if (current == null) {
            current = new PolyArcPath();
        }
        PathIterator it = new QuadCurve2D.Double(lastX, lastY, x1, y1, x, y).getPathIterator(null, flatness);
        it.next();
        double[] coords = new double[8];
        while (!it.isDone()) {
            it.currentSegment(coords);
            lineTo(coords[0], coords[1]);
            it.next();
        }
    }

    @Override
    protected void doArcTo(double lastX, double lastY, double radiusX, double radiusY, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag) {
        if (radiusX != radiusY) {
            super.doArcTo(lastX, lastY, radiusX, radiusY, xAxisRotation, x, y, largeArcFlag, sweepFlag);
        }
    }
}
