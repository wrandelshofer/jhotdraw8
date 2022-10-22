/*
 * @(#)SplitResult.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;

class SplitResult {
    /// Updated starting vertex.
    PlineVertex updatedStart;
    /// Vertex at the split point.
    PlineVertex splitVertex;

    SplitResult() {
    }
}