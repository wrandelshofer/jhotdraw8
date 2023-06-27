/*
 * @(#)PlineIntersect.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;


import java.awt.geom.Point2D;

/**
 * PlineIntersect.
 * <p>
 * References:
 * <p>
 * This code has been derived from CavalierContours.
 * <dl>
 *     <dt>CavalierContours. Copyright (c) 2019 Jedidiah Buck McCready.
 *    <a href="https://github.com/jbuckmccready/CavalierContours/blob/7a35376eb4c2d5f917d3e0564ea630c94137255e/LICENSE">MIT License.</a></dt>
 *     <dd><a href="https://github.com/jbuckmccready/CavalierContours">github.com</a></dd>
 * </dl>
 */
public class PlineIntersect {
    /**
     * Index of the start vertex of the first segment.
     */
    int sIndex1;
    /**
     * Index of the start vertex of the second segment.
     */
    int sIndex2;
    /**
     * Point of intersection.
     */
    Point2D.Double pos;

    PlineIntersect() {
    }

    PlineIntersect(int si1, int si2, Point2D.Double p) {
        this.sIndex1 = si1;
        this.sIndex2 = si2;
        this.pos = p;
    }

    @Override
    public String toString() {
        return "PlineIntersect{" +
                "sIndex1=" + sIndex1 +
                ", sIndex2=" + sIndex2 +
                ", pos=" + pos +
                '}';
    }
}
