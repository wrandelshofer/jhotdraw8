/*
 * @(#)FXCubicCurves.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import javafx.scene.shape.CubicCurve;

public class FXCubicCurves {

    /**
     * Don't let anyone instantiate this class.
     */
    private FXCubicCurves() {
    }

    public static double[] toArray(CubicCurve c) {
        return new double[]{c.getStartX(),
                c.getStartY(),
                c.getControlX1(),
                c.getControlY1(),
                c.getControlX2(),
                c.getControlY2(),
                c.getEndX(),
                c.getEndY()};
    }

    public static CubicCurve ofArray(double[] c, int o) {
        return new CubicCurve(
                c[o + 0], c[o + 1],
                c[o + 2], c[o + 3],
                c[o + 4], c[o + 5],
                c[o + 6], c[o + 7]
        );
    }
}
