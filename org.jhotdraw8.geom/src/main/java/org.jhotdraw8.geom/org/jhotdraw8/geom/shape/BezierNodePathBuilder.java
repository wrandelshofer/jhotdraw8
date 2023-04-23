/*
 * @(#)BezierNodePathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.shape;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.geom.AbstractPathBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * BezierNodePathBuilder.
 *
 * @author Werner Randelshofer
 */
public class BezierNodePathBuilder extends AbstractPathBuilder<ImmutableList<BezierNode>> {

    private final @NonNull List<BezierNode> nodes = new ArrayList<>();

    public BezierNodePathBuilder() {
    }

    private void add(BezierNode newValue) {
        nodes.add(newValue);
    }

    @Override
    protected void doClosePath() {
        BezierNode last = getLastNode();
        if (last != null) {
            last = new BezierNode(last.getMask() | BezierNode.CLOSE_MASK, last.isEquidistant(), last.isCollinear(), last.getX0(), last.getY0(), last.getX1(), last.getY1(),
                    last.getX2(), last.getY2());
            setLast(last);
        }
    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        BezierNode last = getLastNode();

        last = new BezierNode(last.getMask() | BezierNode.C2_MASK, last.isEquidistant(), last.isCollinear(), last.getX0(), last.getY0(), last.getX1(), last.getY1(), x1, y1);
        if (last.computeIsCollinear()) {
            last = last.setCollinear(true);
        }
        setLast(last);
        add(new BezierNode(BezierNode.C0C1_MASK, false, false, x, y, x2, y2, x - x2 + x, y - y2 + y));
    }

    @Override
    protected void doLineTo(double x, double y) {
        add(new BezierNode(BezierNode.C0_MASK, false, false, x, y, x, y, x, y));
    }

    @Override
    protected void doMoveTo(double x, double y) {
        add(new BezierNode(BezierNode.C0_MASK | BezierNode.MOVE_MASK, false, false, x, y, x, y, x, y));
    }

    @Override
    protected void doQuadTo(double x1, double y1, double x, double y) {
        add(new BezierNode(BezierNode.C0C1_MASK, false, false, x, y, x1, y1, x1, y1));
    }

    @Override
    protected void doSmoothCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        BezierNode last = getLastNode();
        setLast(new BezierNode(last.getMask() | BezierNode.C2_MASK, true, true, last.getX0(), last.getY0(), last.getX1(), last.getY1(), x1, y1));
        add(new BezierNode(BezierNode.C0C1_MASK, false, false, x, y, x1, y1, x2, y2));
    }

    @Override
    protected void doSmoothQuadTo(double x1, double y1, double x, double y) {
        BezierNode last = getLastNode();
        setLast(new BezierNode(last.getMask(), true, true, last.getX0(), last.getY0(), last.getX1(), last.getY1(), last.getX2(), last.getY2()));
        add(new BezierNode(BezierNode.C0C1_MASK, false, false, x, y, x1, y1, x1, y1));
    }

    private BezierNode getLastNode() {
        return nodes.get(nodes.size() - 1);
    }

    private void setLast(BezierNode newValue) {
        nodes.set(nodes.size() - 1, newValue);
    }

    @Override
    public @NonNull ImmutableList<BezierNode> build() {
        return VectorList.copyOf(nodes);
    }

    @Override
    protected void doPathDone() {
        //
    }


}
