/*
 * @(#)RenderContext.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.render;

import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * RenderContext.
 */
public interface BasicRenderContext {

    /**
     * Gets a property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    @Nullable
    <T> T get(MapAccessor<T> key);

    /**
     * Gets a nonnull property value.
     *
     * @param <T> the value type
     * @param key the key
     * @return the value
     */
    <T> T getNonNull(NonNullMapAccessor<T> key);
}
