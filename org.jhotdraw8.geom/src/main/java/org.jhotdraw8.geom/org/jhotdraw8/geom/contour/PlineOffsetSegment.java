/*
 * @(#)PlineOffsetSegment.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;


import java.awt.geom.Point2D;

class PlineOffsetSegment {
    final PlineVertex v1;
    final PlineVertex v2;
    final Point2D.Double origV2Pos;
    final boolean collapsedArc;


    public PlineOffsetSegment(PlineVertex v1, PlineVertex v2, Point2D.Double origV2Pos, boolean collapsedArc) {
        this.v1 = v1;
        this.v2 = v2;
        this.origV2Pos = origV2Pos;
        this.collapsedArc = collapsedArc;
    }

    @Override
    public String toString() {
        return "PlineOffsetSegment{" +
                "v1=" + v1 +
                ", v2=" + v2 +
                ", origV2Pos=" + origV2Pos +
                ", collapsedArc=" + collapsedArc +
                '}';
    }
}
