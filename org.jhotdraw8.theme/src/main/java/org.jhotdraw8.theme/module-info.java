/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Provides support for JavaFX themes.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.theme {
    requires transitive org.jhotdraw8.color;
    requires transitive org.jspecify;

    requires transitive org.jhotdraw8.fxbase;
    exports org.jhotdraw8.theme;
    exports org.jhotdraw8.theme.atlantafx;
    opens org.jhotdraw8.theme.atlantafx;
}