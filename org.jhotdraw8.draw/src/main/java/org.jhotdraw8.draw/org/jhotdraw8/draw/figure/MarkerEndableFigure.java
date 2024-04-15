/*
 * @(#)MarkerEndableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.shape.PathElement;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.icollection.immutable.ImmutableList;

/**
 * A figure which supports end markers.
 *
 * @author Werner Randelshofer
 */
public interface MarkerEndableFigure extends Figure {

    /**
     * Marker end is an SVG path that points to the right, with coordinate 0,0 at the tail of the path.
     */
    @NonNull
    NullableFXPathElementsStyleableKey MARKER_END_SHAPE = new NullableFXPathElementsStyleableKey("marker-end-shape", null);

    @NonNull
    DoubleStyleableKey MARKER_END_SCALE_FACTOR = new DoubleStyleableKey("marker-end-scale-factor", 1.0);

    default @Nullable ImmutableList<PathElement> getMarkerEndShape() {
        return getStyled(MARKER_END_SHAPE);
    }

    default double getMarkerEndScaleFactor() {
        return getStyledNonNull(MARKER_END_SCALE_FACTOR);
    }

}
