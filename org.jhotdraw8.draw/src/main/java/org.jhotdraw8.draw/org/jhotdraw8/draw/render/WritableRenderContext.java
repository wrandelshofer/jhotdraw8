/*
 * @(#)WritableRenderContext.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.render;

import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jspecify.annotations.Nullable;

/**
 * RenderContext.
 *
 */
public interface WritableRenderContext extends RenderContext {
    <T> void set(MapAccessor<T> key, @Nullable T value);
}
