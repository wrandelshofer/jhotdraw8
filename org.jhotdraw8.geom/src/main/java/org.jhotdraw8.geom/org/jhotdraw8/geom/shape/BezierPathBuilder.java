/*
 * @(#)BezierPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.shape;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.geom.AbstractPathBuilder;
import org.jhotdraw8.geom.Points;

import java.util.ArrayList;
import java.util.List;

/**
 * BezierPathBuilder.
 *
 * @author Werner Randelshofer
 */
public class BezierPathBuilder extends AbstractPathBuilder<BezierPath> {

    private final @NonNull List<BezierNode> nodes = new ArrayList<>();
    private int moveIndex;

    public BezierPathBuilder() {
    }

    private void add(BezierNode newValue) {
        nodes.add(newValue);
    }

    @Override
    protected void doClosePath() {
        BezierNode last = getLast();
        if (last != null) {
            if (moveIndex != -1 && moveIndex != nodes.size() - 1) {
                BezierNode moveNode = nodes.get(moveIndex);
                if (Points.almostEqual(last.getX0(), last.getY0(), moveNode.getX0(), moveNode.getY0())) {
                    moveNode = moveNode.withMaskBits(last.getMask() & BezierNode.C1_MASK)
                            .setX1(last.getX1()).setY1(last.getY1());
                    nodes.set(moveIndex, moveNode);
                    nodes.removeLast();
                    last = getLast();
                }
            }
            last = new BezierNode(last.getMask() | BezierNode.CLOSE_MASK, last.isEquidistant(), last.isCollinear(), last.getX0(), last.getY0(), last.getX1(), last.getY1(),
                    last.getX2(), last.getY2());
            setLast(last);
        }
    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        BezierNode last = getLast();

        if (last != null) {
            // Set the outgoing tangent on the last bezier node
            last = new BezierNode(last.getMask() | BezierNode.C2_MASK, last.isEquidistant(), last.isCollinear(), last.getX0(), last.getY0(), last.getX1(), last.getY1(), x1, y1);
            if (last.computeIsCollinear()) {
                last = last.withCollinear(true);
            }
            setLast(last);
        }

        // Set the ingoing tangent on the new bezier node
        add(new BezierNode(BezierNode.C0C1_MASK, false, false, x, y, x2, y2, x - x2 + x, y - y2 + y));
    }

    @Override
    protected void doLineTo(double x, double y) {
        add(new BezierNode(BezierNode.C0_MASK, false, false, x, y, x, y, x, y));
    }

    @Override
    protected void doMoveTo(double x, double y) {
        moveIndex = nodes.size();
        add(new BezierNode(BezierNode.C0_MASK | BezierNode.MOVE_MASK, false, false, x, y, x, y, x, y));
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x, double y) {
        BezierNode last = getLast();
        if (last != null && last.isC1()) {
            // Set the outgoing tangent on the last bezier node if it already has an ingoing tangent
            last = new BezierNode(last.getMask() | BezierNode.C2_MASK, false, false, last.getX0(), last.getY0(), last.getX1(), last.getY1(), x1, y1);
            last = last.withCollinear(last.computeIsCollinear());
            last = last.withEquidistant(last.computeIsEquidistant());
            setLast(last);
            add(new BezierNode(BezierNode.C0_MASK, false, false, x, y, x1, y1, x1, y1));
            return;
        }
        add(new BezierNode(BezierNode.C0C1_MASK, false, false, x, y, x1, y1, x1, y1));
    }

    @Override
    protected void doSmoothCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        BezierNode last = getLast();
        setLast(new BezierNode(last.getMask() | BezierNode.C2_MASK, true, true, last.getX0(), last.getY0(), last.getX1(), last.getY1(), x1, y1));
        add(new BezierNode(BezierNode.C0C1_MASK, false, false, x, y, x2, y2, x2, y2));
    }

    @Override
    protected void doSmoothQuadTo(double x1, double y1, double x, double y) {
        BezierNode last = getLast();
        setLast(new BezierNode(last.getMask() | BezierNode.C2_MASK, true, true, last.getX0(), last.getY0(), last.getX1(), last.getY1(), x1, y1));
        add(new BezierNode(BezierNode.C0_MASK, false, false, x, y, x1, y1, x1, y1));
    }

    private @Nullable BezierNode getLast() {
        return nodes.isEmpty() ? null : nodes.getLast();
    }

    private void setLast(@NonNull BezierNode newValue) {
        nodes.set(nodes.size() - 1, newValue);
    }

    @Override
    public @NonNull BezierPath build() {
        return new BezierPath(nodes);
    }

    @Override
    protected void doPathDone() {
        //
    }


}