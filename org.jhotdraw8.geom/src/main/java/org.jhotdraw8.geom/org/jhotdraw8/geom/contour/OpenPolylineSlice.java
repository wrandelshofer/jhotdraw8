/*
 * @(#)OpenPolylineSlice.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

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
