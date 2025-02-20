/*
 * @(#)Paintable.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;

import javafx.scene.paint.Paint;
import org.jhotdraw8.css.render.BasicRenderContext;
import org.jspecify.annotations.Nullable;

/**
 * Paintable.
 *
 */
public interface Paintable {

    @Nullable Paint getPaint();

    default @Nullable Paint getPaint(@Nullable BasicRenderContext ctx) {
        return getPaint();
    }

    static @Nullable Paint getPaint(@Nullable Paintable p) {
        return p == null ? null : p.getPaint();
    }

    static @Nullable Paint getPaint(@Nullable Paintable p, @Nullable BasicRenderContext ctx) {
        return p == null ? null : p.getPaint(ctx);
    }
}
