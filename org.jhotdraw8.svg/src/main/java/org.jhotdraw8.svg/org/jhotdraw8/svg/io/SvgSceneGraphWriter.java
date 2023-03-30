/*
 * @(#)SvgSceneGraphWriter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.io;

import javafx.scene.Node;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNonNullKey;

public interface SvgSceneGraphWriter {
    /**
     * If {@link Node#getProperties()} contains a String property with this name,
     * the {@code SvgSceneGraphExporter} exports a {@literal <title>}
     * element with the property value as its content.
     * <p>
     * The value of this constant is: {@value #TITLE_PROPERTY_NAME}.
     */
    String TITLE_PROPERTY_NAME = "title";
    /**
     * If {@link Node#getProperties()} contains a String property with this name,
     * the {@code SvgSceneGraphExporter} exports a {@literal <desc>}
     * element with the property value as its content.
     * <p>
     * The value of this constant is: {@value #DESC_PROPERTY_NAME}.
     */
    String DESC_PROPERTY_NAME = "desc";
    SimpleNonNullKey<Boolean> EXPORT_INVISIBLE_ELEMENTS_KEY = new SimpleNonNullKey<>("exportInvisibleElements", Boolean.class, false);
    SimpleNonNullKey<Boolean> RELATIVIZE_PATHS_KEY = new SimpleNonNullKey<>("relativizePaths", Boolean.class, false);
    SimpleNonNullKey<Boolean> CONVERT_TEXT_TO_PATH_KEY = new SimpleNonNullKey<>("convertTextToPath", Boolean.class, false);
}
