/*
 * @(#)PolyArcPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

import org.jhotdraw8.geom.AbstractPathBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Path builder for {@link PolyArcPath}.
 */
public class PolyArcPathBuilder extends AbstractPathBuilder<List<PolyArcPath>> {
    private final List<PolyArcPath> paths = new ArrayList<>();
    private PolyArcPath current;

    public PolyArcPathBuilder() {
    }

    @Override
    public List<PolyArcPath> build() {
        return paths;
    }

    @Override
    protected void doClosePath() {
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
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doLineTo(double x, double y) {
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
    protected void doQuadTo(double x1, double y1, double x, double y) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doArcTo(double radiusX, double radiusY, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag) {
        if (radiusX == radiusY) {

        } else {
            super.doArcTo(radiusX, radiusY, xAxisRotation, x, y, largeArcFlag, sweepFlag);
        }
    }
}
