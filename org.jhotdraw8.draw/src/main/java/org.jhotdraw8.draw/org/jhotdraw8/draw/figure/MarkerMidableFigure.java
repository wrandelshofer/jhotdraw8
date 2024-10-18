/*
 * @(#)MarkerMidableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.shape.PathElement;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

/**
 * A figure which supports markers at the nodes of a path.
 *
 * @author Werner Randelshofer
 */
public interface MarkerMidableFigure extends Figure {
    /**
     * "Marker Mid" is an SVG path that points to the right, with coordinate 0,0 at a node of the path.
     */
    @Nullable NullableFXPathElementsStyleableKey MARKER_MID_SHAPE = new NullableFXPathElementsStyleableKey("marker-mid-shape", null);
    DoubleStyleableKey MARKER_MID_SCALE_FACTOR = new DoubleStyleableKey("marker-mid-scale-factor", 1.0);

    default @Nullable PersistentList<PathElement> getMarkerMidShape() {
        return getStyled(MARKER_MID_SHAPE);
    }

    default double getMarkerMidScaleFactor() {
        return getStyledNonNull(MARKER_MID_SCALE_FACTOR);
    }
}
