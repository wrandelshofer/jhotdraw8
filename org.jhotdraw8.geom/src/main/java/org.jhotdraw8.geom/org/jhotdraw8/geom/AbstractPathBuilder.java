/*
 * @(#)AbstractPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;


import java.awt.geom.Point2D;

/**
 * AbstractPathBuilder.
 *
 * @param <T> the product type
 */
public abstract class AbstractPathBuilder<T> implements PathBuilder<T> {

    private double lastX, lastY;
    private double lastMoveToX, lastMoveToY;
    private double lastCX, lastCY;

    private int numCommands = 0;

    public AbstractPathBuilder() {
    }

    @Override
    public void arcTo(double radiusX, double radiusY, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag) {
        if (numCommands++ == 0) {
            throw new IllegalStateException("Missing initial moveto in path definition.");
        }
        doArcTo(lastX, lastY, radiusX, radiusY, xAxisRotation, x, y, largeArcFlag, sweepFlag);
        lastX = x;
        lastY = y;
        lastCX = x;
        lastCY = y;
    }

    @Override
    public final void closePath() {
        if (numCommands++ == 0) {
            throw new IllegalStateException("Missing initial moveto in path definition.");
        }
        doClosePath(lastX, lastY, lastMoveToX, lastMoveToY);
    }

    @Override
    public final void curveTo(double x1, double y1, double x2, double y2, double x, double y) {
        if (numCommands++ == 0) {
            throw new IllegalStateException("Missing initial moveto in path definition.");
        }
        doCurveTo(lastX, lastY, x1, y1, x2, y2, x, y);
        lastX = x;
        lastY = y;
        lastCX = x2;
        lastCY = y2;
    }

    protected void doArcTo(double lastX, double lastY, double radiusX, double radiusY, double xAxisRotation, double x, double y, boolean largeArcFlag, boolean sweepFlag) {
        PathBuilder.super.arcTo(radiusX, radiusY, xAxisRotation, x, y, largeArcFlag, sweepFlag);
    }

    protected abstract void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY);

    protected abstract void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y);

    protected abstract void doLineTo(double lastX, double lastY, double x, double y);

    protected abstract void doMoveTo(double x, double y);

    protected abstract void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y);

    protected void doSmoothCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        doCurveTo(lastX, lastY, x1, y1, x2, y2, x, y);
    }

    protected void doSmoothQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        doQuadTo(lastX, lastY, x1, y1, x, y);
    }

    @Override
    public Point2D.Double getLastPoint() {
        return new Point2D.Double(lastX, lastY);
    }

    @Override
    public double getLastX() {
        return lastX;
    }

    @Override
    public double getLastY() {
        return lastY;
    }

    public double getLastMoveToX() {
        return lastMoveToX;
    }


    public double getLastMoveToY() {
        return lastMoveToY;
    }

    @Override
    public double getLastCX() {
        return lastCX;
    }

    @Override
    public double getLastCY() {
        return lastCY;
    }

    @Override
    public final void lineTo(double x, double y) {
        if (numCommands++ == 0) {
            throw new IllegalStateException("Missing initial moveto in path definition.");
        }
        doLineTo(lastX, lastY, x, y);
        lastX = x;
        lastY = y;
        lastCX = x;
        lastCY = y;
    }


    @Override
    public final void moveTo(double x, double y) {
        numCommands++;
        doMoveTo(x, y);
        lastX = x;
        lastY = y;
        lastMoveToX = x;
        lastMoveToY = y;
        lastCX = x;
        lastCY = y;
    }

    @Override
    public final void quadTo(double x1, double y1, double x, double y) {
        if (numCommands++ == 0) {
            throw new IllegalStateException("Missing initial moveto in path definition.");
        }
        doQuadTo(lastX, lastY, x1, y1, x, y);
        lastX = x;
        lastY = y;
        lastCX = x1;
        lastCY = y1;
    }

    @Override
    public final void smoothCurveTo(double x2, double y2, double x, double y) {
        if (numCommands++ == 0) {
            throw new IllegalStateException("Missing initial moveto in path definition.");
        }
        doSmoothCurveTo(
                lastX, lastY, lastX - lastCX + lastX, lastY - lastCY + lastY, x2, y2, x, y);
        lastX = x;
        lastY = y;
        lastCX = x2;
        lastCY = y2;
    }

    @Override
    public final void smoothQuadTo(double x, double y) {
        if (numCommands++ == 0) {
            throw new IllegalStateException("Missing initial moveto in path definition.");
        }
        doSmoothQuadTo(
                lastX, lastY, lastX - lastCX + lastX, lastY - lastCY + lastY, x, y);
        lastCX = lastX - lastCX + lastX;
        lastCY = lastY - lastCY + lastY;
        lastX = x;
        lastY = y;
    }

    protected void setLastX(double lastX) {
        this.lastX = lastX;
    }

    protected void setLastY(double lastY) {
        this.lastY = lastY;
    }

    protected void setLastCX(double lastCX) {
        this.lastCX = lastCX;
    }

    protected void setLastCY(double lastCY) {
        this.lastCY = lastCY;
    }

    @Override
    public boolean needsMoveTo() {
        return numCommands == 0;
    }


}
