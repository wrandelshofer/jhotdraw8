/*
 * @(#)ExportOutputFormat.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.typesafekey.Key;
import org.jhotdraw8.collection.typesafekey.NonNullObjectKey;

/**
 * ExportOutputFormat.
 *
 * @author Werner Randelshofer
 */
public interface ExportOutputFormat {
    void setOptions(@NonNull ReadOnlyMap<Key<?>, Object> newValue);

    @NonNull ReadOnlyMap<Key<?>, Object> getOptions();

    NonNullObjectKey<Double> EXPORT_DRAWING_DPI_KEY = new NonNullObjectKey<>("exportDrawingDpi", Double.class, 72.0);
    NonNullObjectKey<Boolean> EXPORT_DRAWING_KEY = new NonNullObjectKey<>("exportDrawing", Boolean.class, true);
    NonNullObjectKey<Double> EXPORT_PAGES_DPI_KEY = new NonNullObjectKey<>("exportPagesDpi", Double.class, 300.0);
    NonNullObjectKey<Boolean> EXPORT_PAGES_KEY = new NonNullObjectKey<>("exportPages", Boolean.class, true);
    NonNullObjectKey<Double> EXPORT_SLICES_DPI_KEY = new NonNullObjectKey<>("exportSlicesDpi", Double.class, 72.0);
    NonNullObjectKey<Boolean> EXPORT_SLICES_KEY = new NonNullObjectKey<>("exportSlices", Boolean.class, true);
    NonNullObjectKey<Boolean> EXPORT_SLICES_RESOLUTION_2X_KEY = new NonNullObjectKey<>("exportSlicesResolution2", Boolean.class, false);
    NonNullObjectKey<Boolean> EXPORT_SLICES_RESOLUTION_3X_KEY = new NonNullObjectKey<>("exportSlicesResolution3", Boolean.class, false);


}
