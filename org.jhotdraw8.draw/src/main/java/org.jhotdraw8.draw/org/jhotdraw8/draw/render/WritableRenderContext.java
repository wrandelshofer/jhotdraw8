/*
 * @(#)WritableRenderContext.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.render;

import org.jspecify.annotations.Nullable;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;

/**
 * RenderContext.
 *
 * @author Werner Randelshofer
 */
public interface WritableRenderContext extends RenderContext {
    <T> void set(MapAccessor<T> key, @Nullable T value);
}
