/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Defines color spaces and color conversion utilities.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.color {
    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires transitive org.jspecify;

    requires java.logging;

    exports org.jhotdraw8.color;
    exports org.jhotdraw8.color.math;
}