/*
 * @(#)FXQuadCurves.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

import javafx.scene.shape.QuadCurve;

public class FXQuadCurves {

    /**
     * Don't let anyone instantiate this class.
     */
    private FXQuadCurves() {
    }

    public static double[] toArray(QuadCurve c) {
        return new double[]{c.getStartX(),
                c.getStartY(),
                c.getControlX(),
                c.getControlY(),
                c.getEndX(),
                c.getEndY()};
    }

    public static QuadCurve ofArray(double[] c, int o) {
        return new QuadCurve(
                c[o + 0], c[o + 1],
                c[o + 2], c[o + 3],
                c[o + 4], c[o + 5]
        );
    }
}
