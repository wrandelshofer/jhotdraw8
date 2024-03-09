/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines color spaces and color conversion utilities.
 */
@SuppressWarnings("module")
module org.jhotdraw8.color {
    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires static org.jhotdraw8.annotation;
    requires java.logging;

    exports org.jhotdraw8.color;
    exports org.jhotdraw8.color.math;
    exports org.jhotdraw8.color.util;
}