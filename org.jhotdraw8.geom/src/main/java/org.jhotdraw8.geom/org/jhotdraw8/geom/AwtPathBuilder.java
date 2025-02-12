/*
 * @(#)AwtPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;


import java.awt.geom.Path2D;

/**
 * Builds an AWT {@code Path2D}.
 *
 */
public class AwtPathBuilder extends AbstractPathBuilder<Path2D.Double> {

    private final Path2D.Double path;

    public AwtPathBuilder() {
        this(new Path2D.Double());
    }

    public AwtPathBuilder(Path2D.Double path) {
        this.path = path;
    }

    public boolean isEmpty() {
        return path.getCurrentPoint() == null;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        path.closePath();
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x, double y, double x0, double y0, double x1, double y1) {
        path.curveTo(x, y, x0, y0, x1, y1);
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        path.lineTo(x, y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        path.moveTo(x, y);
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x, double y, double x0, double y0) {
        path.quadTo(x, y, x0, y0);
    }

    @Override
    public Path2D.Double build() {
        path.trimToSize();
        return path;
    }
}
