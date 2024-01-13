/*
 * @(#)PathMetricsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.primitive.ByteArrayList;
import org.jhotdraw8.collection.primitive.DoubleArrayList;
import org.jhotdraw8.collection.primitive.IntArrayList;

import java.awt.geom.PathIterator;

public abstract class AbstractPathDataBuilder<B> extends AbstractPathBuilder<B> {
    @NonNull
    protected final ByteArrayList commands = new ByteArrayList();
    protected @NonNull
    final IntArrayList offsets = new IntArrayList();

    protected @NonNull
    final DoubleArrayList coords = new DoubleArrayList();
    protected @NonNull
    final double[] temp = new double[8];
    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
    private static final byte SEG_MOVETO = (int) PathIterator.SEG_MOVETO;
    private static final byte SEG_LINETO = (int) PathIterator.SEG_LINETO;
    private static final byte SEG_QUADTO = (int) PathIterator.SEG_QUADTO;
    private static final byte SEG_CUBICTO = (int) PathIterator.SEG_CUBICTO;
    private static final byte SEG_CLOSE = (byte) PathIterator.SEG_CLOSE;
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
    protected void doClosePath() {
        if (!commands.isEmpty()
                && commands.getLastAsByte() != SEG_MOVETO
                && commands.getLastAsByte() != SEG_CLOSE) {
            commands.addAsByte(SEG_CLOSE);
            offsets.addAsInt(coords.size());
        }
    }

    @Override
    protected void doPathDone() {

    }

    @Override
    protected void doCurveTo(double x1, double y1, double x2, double y2, double x, double y) {
        temp[0] = getLastX();
        temp[1] = getLastY();
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
    protected void doLineTo(double x, double y) {
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
    protected void doQuadTo(double x1, double y1, double x, double y) {
        temp[0] = getLastX();
        temp[1] = getLastY();
        temp[2] = x1;
        temp[3] = y1;
        temp[4] = x;
        temp[5] = y;
        double arcLength = QuadCurves.arcLength(temp, 0);
        if (arcLength > 0) {
            commands.addAsByte(SEG_QUADTO);
            offsets.addAsInt(coords.size());
            coords.addAsDouble(x1);
            coords.addAsDouble(y1);
            coords.addAsDouble(x);
            coords.addAsDouble(y);
        }
    }

}
