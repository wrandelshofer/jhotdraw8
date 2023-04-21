/*
 * @(#)ExportOutputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNonNullKey;

/**
 * ExportOutputFormat.
 *
 * @author Werner Randelshofer
 */
public interface ExportOutputFormat {
    void setOptions(@NonNull ImmutableMap<Key<?>, Object> newValue);

    @NonNull ImmutableMap<Key<?>, Object> getOptions();

    SimpleNonNullKey<Double> EXPORT_DRAWING_DPI_KEY = new SimpleNonNullKey<>("exportDrawingDpi", Double.class, 72.0);
    SimpleNonNullKey<Boolean> EXPORT_DRAWING_KEY = new SimpleNonNullKey<>("exportDrawing", Boolean.class, true);
    SimpleNonNullKey<Double> EXPORT_PAGES_DPI_KEY = new SimpleNonNullKey<>("exportPagesDpi", Double.class, 300.0);
    SimpleNonNullKey<Boolean> EXPORT_PAGES_KEY = new SimpleNonNullKey<>("exportPages", Boolean.class, true);
    SimpleNonNullKey<Double> EXPORT_SLICES_DPI_KEY = new SimpleNonNullKey<>("exportSlicesDpi", Double.class, 72.0);
    SimpleNonNullKey<Boolean> EXPORT_SLICES_KEY = new SimpleNonNullKey<>("exportSlices", Boolean.class, true);
    SimpleNonNullKey<Boolean> EXPORT_SLICES_RESOLUTION_2X_KEY = new SimpleNonNullKey<>("exportSlicesResolution2", Boolean.class, false);
    SimpleNonNullKey<Boolean> EXPORT_SLICES_RESOLUTION_3X_KEY = new SimpleNonNullKey<>("exportSlicesResolution3", Boolean.class, false);


}
