/*
 * @(#)MarkerEndableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.shape.PathElement;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

/**
 * A figure which supports end markers.
 *
 */
public interface MarkerEndableFigure extends Figure {

    /**
     * Marker end is an SVG path that points to the right, with coordinate 0,0 at the tail of the path.
     */
    NullableFXPathElementsStyleableKey MARKER_END_SHAPE = new NullableFXPathElementsStyleableKey("marker-end-shape", null);

    DoubleStyleableKey MARKER_END_SCALE_FACTOR = new DoubleStyleableKey("marker-end-scale-factor", 1.0);

    default @Nullable PersistentList<PathElement> getMarkerEndShape() {
        return getStyled(MARKER_END_SHAPE);
    }

    default double getMarkerEndScaleFactor() {
        return getStyledNonNull(MARKER_END_SCALE_FACTOR);
    }

}
