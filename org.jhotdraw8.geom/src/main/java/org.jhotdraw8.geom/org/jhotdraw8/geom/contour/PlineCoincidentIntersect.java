/*
 * @(#)PlineCoincidentIntersect.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

import java.awt.geom.Point2D;

/**
 * PlineCoincidentIntersect.
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
public class PlineCoincidentIntersect {
    /// Index of the start vertex of the first segment
    int sIndex1;
    /// Index of the start vertex of the second segment
    int sIndex2;
    /// One end point of the coincident path
    Point2D.Double point1;
    /// Other end point of the coincident path
    Point2D.Double point2;

    PlineCoincidentIntersect() {
    }

    PlineCoincidentIntersect(int si1, int si2, Point2D.Double point1,
                             Point2D.Double point2) {
        this.sIndex1 = si1;
        this.sIndex2 = si2;
        this.point1 = point1;
        this.point2 = point2;
    }
}
