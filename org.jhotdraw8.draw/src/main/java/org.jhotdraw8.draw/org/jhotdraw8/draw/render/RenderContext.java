/*
 * @(#)RenderContext.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.render;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.css.value.DefaultSystemColorConverter;
import org.jhotdraw8.draw.css.value.SystemColorConverter;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Page;
import org.jhotdraw8.fxbase.beans.ReadOnlyPropertyBean;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;
import org.jhotdraw8.fxcollection.typesafekey.NullableObjectKey;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * RenderContext.
 *
 */
public interface RenderContext extends ReadOnlyPropertyBean {

    // ---
    // keys
    // ---
    NonNullKey<RenderingIntent> RENDERING_INTENT = new NonNullObjectKey<>("renderingIntent", RenderingIntent.class, RenderingIntent.EDITOR);

    /**
     * The dots per inch of the rendering device.
     */
    NonNullKey<Double> DPI = new NonNullObjectKey<>("dpi", Double.class, 96.0);
    /**
     * Contains a non-null value if the rendering is clipped. The clip bounds are given in world coordinates.
     */
    Key<Bounds> CLIP_BOUNDS = new NullableObjectKey<>("clipBounds", Bounds.class, null);
    /**
     * Number of nodes that can be rendered per layer in the drawing editor..
     */
    NonNullObjectKey<Integer> MAX_NODES_PER_LAYER = new NonNullObjectKey<>("maxNodesPerLayer", Integer.class, 10_000);

    Key<Page> RENDER_PAGE = new NullableObjectKey<>("renderPage", Page.class, null);
    Key<Integer> RENDER_PAGE_NUMBER = new NullableObjectKey<>("renderPageNumber", Integer.class, 0);
    Key<Integer> RENDER_NUMBER_OF_PAGES = new NullableObjectKey<>("renderNumberOfPages", Integer.class, 1);
    Key<Integer> RENDER_PAGE_INTERNAL_NUMBER = new NullableObjectKey<>("renderPageInternalNumber", Integer.class, 0);
    Key<Instant> RENDER_TIMESTAMP = new NullableObjectKey<>("renderTimestamp", Instant.class, Instant.now());

    NonNullObjectKey<UnitConverter> UNIT_CONVERTER_KEY = new NonNullObjectKey<>("unitConverter", UnitConverter.class, new DefaultUnitConverter());
    NonNullObjectKey<SystemColorConverter> SYSTEM_COLOR_CONVERTER_KEY = new NonNullObjectKey<>("colorConverter", SystemColorConverter.class, new DefaultSystemColorConverter());
    // ---
    // behavior
    // ---

    /**
     * Gets the JavaFX node which is used to render the specified figure by this
     * {@code RenderContext}.
     *
     * @param f The figure
     * @return The JavaFX node associated to the figure
     */
    @Nullable Node getNode(Figure f);

}
