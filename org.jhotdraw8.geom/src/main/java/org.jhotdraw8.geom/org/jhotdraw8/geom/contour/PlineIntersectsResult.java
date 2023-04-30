/*
 * @(#)PlineIntersectsResult.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

import java.util.ArrayList;
import java.util.List;

/**
 * PlineIntersectsResult.
 * <p>
 * References:
 * <p>
 * This code has been derived from CavalierContours.
 * <dl>
 *     <dt>CavalierCoutours. Copyright (c) 2019 Jedidiah Buck McCready.
 *    <a href="https://github.com/jbuckmccready/CavalierContours/blob/7a35376eb4c2d5f917d3e0564ea630c94137255e/LICENSE">MIT License.</a></dt>
 *     <dd><a href="https://github.com/jbuckmccready/CavalierContours">github.com</a></dd>
 * </dl>
 */
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
