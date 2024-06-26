/*
 * @(#)PathMetricsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.collection.primitive.ByteArrayList;
import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.collection.primitive.IntArrayList;

import java.awt.geom.PathIterator;

public abstract class AbstractPathDataBuilder<B> extends AbstractPathBuilder<B> {
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = (int) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = (int) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = (int) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (int) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;
    protected final ByteArrayList commands = new ByteArrayList();
    protected final IntArrayList offsets = new IntArrayList();
    protected final DoubleArrayList coords = new DoubleArrayList();
    protected
    final double[] temp = new double[8];
    protected int windingRule = PathIterator.WIND_EVEN_ODD;

    public AbstractPathDataBuilder() {
    }

    public int getWindingRule() {
        return windingRule;
    }

    public void setWindingRule(int windingRule) {
        this.windingRule = windingRule;
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        if (!commands.isEmpty()
                && commands.getLastAsByte() != SEG_MOVETO
                && commands.getLastAsByte() != SEG_CLOSE) {
            commands.addAsByte(SEG_CLOSE);
            offsets.addAsInt(coords.size());
        }
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        temp[0] = lastX;
        temp[1] = lastY;
        temp[2] = x1;
        temp[3] = y1;
        temp[4] = x2;
        temp[5] = y2;
        temp[6] = x;
        temp[7] = y;
        commands.addAsByte(SEG_CUBICTO);
        offsets.addAsInt(coords.size());
        coords.addAsDouble(x1);
        coords.addAsDouble(y1);
        coords.addAsDouble(x2);
        coords.addAsDouble(y2);
        coords.addAsDouble(x);
        coords.addAsDouble(y);
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        commands.addAsByte(SEG_LINETO);
        offsets.addAsInt(coords.size());
        coords.addAsDouble(x);
        coords.addAsDouble(y);
    }

    @Override
    protected void doMoveTo(double x, double y) {
        commands.addAsByte(SEG_MOVETO);
        offsets.addAsInt(coords.size());
        coords.addAsDouble(x);
        coords.addAsDouble(y);
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        temp[0] = lastX;
        temp[1] = lastY;
        temp[2] = x1;
        temp[3] = y1;
        temp[4] = x;
        temp[5] = y;
            commands.addAsByte(SEG_QUADTO);
            offsets.addAsInt(coords.size());
            coords.addAsDouble(x1);
            coords.addAsDouble(y1);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
    }


}
