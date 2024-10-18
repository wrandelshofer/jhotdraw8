/*
 * @(#)MarkerStartableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.shape.PathElement;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

/**
 * A figure which supports start markers.
 *
 * @author Werner Randelshofer
 */
public interface MarkerStartableFigure extends Figure {
    /**
     * Marker start is an SVG path that points to the right, with coordinate 0,0 at the head of the path.
     */
    NullableFXPathElementsStyleableKey MARKER_START_SHAPE = new NullableFXPathElementsStyleableKey("marker-start-shape", null);
    DoubleStyleableKey MARKER_START_SCALE_FACTOR = new DoubleStyleableKey("marker-start-scale-factor", 1.0);

    default @Nullable PersistentList<PathElement> getMarkerStartShape() {
        return getStyled(MarkerStartableFigure.MARKER_START_SHAPE);
    }

    default double getMarkerStartScaleFactor() {
        return getStyledNonNull(MarkerStartableFigure.MARKER_START_SCALE_FACTOR);
    }
}
