/*
 * @(#)BezierPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.shape;

import org.jhotdraw8.geom.AbstractPathBuilder;
import org.jhotdraw8.geom.Points;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * BezierPathBuilder.
 *
 */
public class BezierPathBuilder extends AbstractPathBuilder<BezierPath> {

    private final List<BezierNode> nodes = new ArrayList<>();
    private int moveIndex;

    public BezierPathBuilder() {
    }

    private void add(BezierNode newValue) {
        nodes.add(newValue);
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        BezierNode last = getLast();
        if (last != null) {
            if (moveIndex != -1 && moveIndex != nodes.size() - 1) {
                BezierNode moveNode = nodes.get(moveIndex);
                if (Points.almostEqual(last.pointX(), last.pointY(), moveNode.pointX(), moveNode.pointY())) {
                    moveNode = moveNode.withMaskBitsSet(last.getMask() & BezierNode.IN_MASK)
                            .withIx(last.inX()).withIy(last.inY());
                    BezierNode moveNodeAsLineNode = moveNode.withMaskBitsClears(BezierNode.MOVE_MASK);
                    moveNode = moveNode
                            .withCollinear(moveNodeAsLineNode.computeIsCollinear())
                            .withEquidistant(moveNodeAsLineNode.computeIsEquidistant());
                    nodes.set(moveIndex, moveNode);
                    nodes.removeLast();
                    last = getLast();
                }
            }
            last = new BezierNode(last.getMask() | BezierNode.CLOSE_MASK, last.isEquidistant(), last.isCollinear(), last.pointX(), last.pointY(), last.inX(), last.inY(),
                    last.outX(), last.outY());
            setLast(last);
        }
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        BezierNode last = getLast();

        if (last != null) {
            // Set the outgoing tangent on the last bezier node
            last = new BezierNode(last.getMask() | BezierNode.OUT_MASK, last.isEquidistant(), last.isCollinear(), last.pointX(), last.pointY(), last.inX(), last.inY(), x1, y1);
            if (last.computeIsCollinear()) {
                last = last.withCollinear(true);
            }
            setLast(last);
        }

        // Set the ingoing tangent on the new bezier node
        add(new BezierNode(BezierNode.POINT_OUT_MASK, false, false, x, y, x2, y2, x - x2 + x, y - y2 + y));
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        add(new BezierNode(BezierNode.POINT_MASK, false, false, x, y, x, y, x, y));
    }

    @Override
    protected void doMoveTo(double x, double y) {
        moveIndex = nodes.size();
        add(new BezierNode(BezierNode.POINT_MASK | BezierNode.MOVE_MASK, false, false, x, y, x, y, x, y));
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        BezierNode last = getLast();
        if (last != null && last.hasIn()) {
            // Set the outgoing tangent on the last bezier node if it already has an ingoing tangent
            last = new BezierNode(last.getMask() | BezierNode.OUT_MASK, false, false, last.pointX(), last.pointY(), last.inX(), last.inY(), x1, y1);
            last = last.withCollinear(last.computeIsCollinear());
            last = last.withEquidistant(last.computeIsEquidistant());
            setLast(last);
            add(new BezierNode(BezierNode.POINT_MASK, false, false, x, y, x1, y1, x1, y1));
            return;
        }
        add(new BezierNode(BezierNode.POINT_OUT_MASK, false, false, x, y, x1, y1, x1, y1));
    }

    @Override
    protected void doSmoothCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        BezierNode last = getLast();
        setLast(new BezierNode(last.getMask() | BezierNode.OUT_MASK, true, true, last.pointX(), last.pointY(), last.inX(), last.inY(), x1, y1));
        add(new BezierNode(BezierNode.POINT_OUT_MASK, false, false, x, y, x2, y2, x2, y2));
    }

    @Override
    protected void doSmoothQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        BezierNode last = getLast();
        setLast(new BezierNode(last.getMask() | BezierNode.OUT_MASK, true, true, last.pointX(), last.pointY(), last.inX(), last.inY(), x1, y1));
        add(new BezierNode(BezierNode.POINT_MASK, false, false, x, y, x1, y1, x1, y1));
    }

    private @Nullable BezierNode getLast() {
        return nodes.isEmpty() ? null : nodes.getLast();
    }

    private void setLast(BezierNode newValue) {
        nodes.set(nodes.size() - 1, newValue);
    }

    @Override
    public BezierPath build() {
        return new BezierPath(nodes);
    }
}
