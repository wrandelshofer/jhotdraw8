/*
 * @(#)OpenPolylineSlice.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

/**
 * OpenPolylineSlice.
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
public class OpenPolylineSlice {
    final int intrStartIndex;
    final PolyArcPath pline;

    public OpenPolylineSlice(int startIndex) {
        this.pline = new PolyArcPath();
        this.intrStartIndex = startIndex;
    }

    /**
     * Creates a new instance with a clone of the specified polyline slice.
     *
     * @param sIndex start index
     * @param slice  a polyline slice (will be cloned)
     */
    public OpenPolylineSlice(int sIndex, PolyArcPath slice) {
        this.intrStartIndex = sIndex;
        this.pline = slice.clone();
    }

    @Override
    public String toString() {
        return "OpenPolylineSlice{" +
                "startIndex=" + intrStartIndex +
                ", pline=" + pline +
                '}';
    }
}
