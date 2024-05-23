/*
 * @(#)StartAndEndPointPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jspecify.annotations.Nullable;

/**
 * StartAndEndPointPathBuilder gets the first start point and last end point of a path,
 * and the derivatives of these points.
 *
 * @author Werner Randelshofer
 */
public class StartAndEndPointPathBuilder extends AbstractPathBuilder<Void> {
    private double startX;
    private double startY;
    private double startTangentX;
    private double startTangentY;
    private double endX;
    private double endY;
    private double endTangentX;
    private double endTangentY;
    private boolean startDone;

    public StartAndEndPointPathBuilder() {
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        //empty
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x3, double y3) {
        if (!startDone) {
            startX = lastX;
            startY = lastY;
            startTangentX = startX - x1;
            startTangentY = startY - y1;
            startDone = true;
        }
        endX = x3;
        endY = y3;
        endTangentX = x3 - x2;
        endTangentY = y3 - y2;
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        if (!startDone) {
            startX = lastX;
            startY = lastY;
            startTangentX = startX - x;
            startTangentY = startY - y;
            startDone = true;
        }
        endX = x;
        endY = y;
        endTangentX = x - lastX;
        endTangentY = y - lastY;
    }

    @Override
    protected void doMoveTo(double x, double y) {
// empty
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x2, double y2) {
        if (!startDone) {
            startX = lastX;
            startY = lastY;
            startTangentX = startX - x1;
            startTangentY = startY - y1;
            startDone = true;
        }
        endX = x2;
        endY = y2;
        endTangentX = x2 - x1;
        endTangentY = y2 - y1;
    }

    public double getEndTangentX() {
        return endTangentX;
    }

    public double getEndTangentY() {
        return endTangentY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getStartTangentX() {
        return startTangentX;
    }

    public double getStartTangentY() {
        return startTangentY;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public boolean isIsStartDone() {
        return startDone;
    }

    @Override
    public @Nullable Void build() {
        return null;
    }
}
