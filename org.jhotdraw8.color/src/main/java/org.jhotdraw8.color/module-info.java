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
    requires transitive org.jhotdraw8.annotation;

    exports org.jhotdraw8.color;
    exports org.jhotdraw8.color.linalg;
}