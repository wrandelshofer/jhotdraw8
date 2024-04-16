/*
 * @(#)StartAndEndPointsPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * StartAndEndPointPathBuilder gets all start and end points of a path,
 * and the derivatives of these points.
 *
 * @author Werner Randelshofer
 */
public class StartAndEndPointsPathBuilder extends AbstractPathBuilder<Void> {
    private final @NonNull List<PointAndDerivative> startPoints = new ArrayList<>();
    private final @NonNull List<PointAndDerivative> endPoints = new ArrayList<>();

    private double startX;
    private double startY;
    private double startTangentX;
    private double startTangentY;
    private double endX;
    private double endY;
    private double endTangentX;
    private double endTangentY;
    private boolean startDone;

    public StartAndEndPointsPathBuilder() {
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        startDone = false;
    }

    @Override
    protected void doPathDone() {
        if (startDone) {
            startPoints.add(new PointAndDerivative(startX, startY, startTangentX, startTangentY));
            endPoints.add(new PointAndDerivative(endX, endY, endTangentX, endTangentY));
        }
        startDone = false;
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
        if (startDone) {
            startPoints.add(new PointAndDerivative(startX, startY, startTangentX, startTangentY));
            endPoints.add(new PointAndDerivative(endX, endY, endTangentX, endTangentY));
            startDone = false;
        }
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

    public @NonNull List<PointAndDerivative> getStartPoints() {
        return startPoints;
    }

    public @NonNull List<PointAndDerivative> getEndPoints() {
        return endPoints;
    }

    @Override
    public @Nullable Void build() {
        return null;
    }
}
