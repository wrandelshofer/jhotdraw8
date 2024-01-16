package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.Nullable;

public class ReversePathBuilder<T> extends AbstractPathBuilder<T> {
    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {

    }

    @Override
    protected void doPathDone() {

    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {

    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {

    }

    @Override
    protected void doMoveTo(double x, double y) {

    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {

    }

    @Override
    public @Nullable T build() {
        return null;
    }
}
