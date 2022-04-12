/*
 * @(#)PlineIntersectsResult.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

import java.util.ArrayList;
import java.util.List;

class PlineIntersectsResult {
    List<PlineIntersect> intersects = new ArrayList<>();
    List<PlineCoincidentIntersect> coincidentIntersects = new ArrayList<>();

    PlineIntersectsResult() {
    }

    @Override
    public String toString() {
        return "PlineIntersectsResult{" +
                "intersects=" + intersects +
                ", coincidentIntersects=" + coincidentIntersects +
                '}';
    }
}
