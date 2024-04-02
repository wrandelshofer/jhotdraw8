/*
 * @(#)MarkerSegmentableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.shape.PathElement;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NullableFXPathElementsStyleableKey;
import org.jhotdraw8.icollection.immutable.ImmutableList;

/**
 * A figure which supports markers in the middle of a path segment.
 *
 * @author Werner Randelshofer
 */
public interface MarkerSegmentableFigure extends Figure {
    /**
     * "Marker Segment" is an SVG path that points to the right, with coordinate 0,0 at the center of a path segment.
     */
    @Nullable NullableFXPathElementsStyleableKey MARKER_SEGMENT_SHAPE = new NullableFXPathElementsStyleableKey("marker-segment-shape", null);
    DoubleStyleableKey MARKER_SEGMENT_SCALE_FACTOR = new DoubleStyleableKey("marker-segment-scale-factor", 1.0);

    default @Nullable ImmutableList<PathElement> getMarkerSegmentShape() {
        return getStyled(MARKER_SEGMENT_SHAPE);
    }

    default double getMarkerSegmentScaleFactor() {
        return getStyledNonNull(MARKER_SEGMENT_SCALE_FACTOR);
    }

}
