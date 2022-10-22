/*
 * @(#)EmptyPathIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import java.awt.geom.PathIterator;

/**
 * An immutable empty path iterator.
 */
public class EmptyPathIterator implements PathIterator {
    public EmptyPathIterator() {
    }

    @Override
    public int getWindingRule() {
        return PathIterator.WIND_EVEN_ODD;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public void next() {

    }

    @Override
    public int currentSegment(float[] coords) {
        return 0;
    }

    @Override
    public int currentSegment(double[] coords) {
        return 0;
    }
}
