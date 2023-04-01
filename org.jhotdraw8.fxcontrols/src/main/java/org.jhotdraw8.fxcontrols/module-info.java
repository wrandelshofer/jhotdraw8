/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Provides controls that are based on JavaFX controls.
 */
@SuppressWarnings("module")
module org.jhotdraw8.fxcontrols {
    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.application;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.fxbase;
    exports org.jhotdraw8.fxcontrols.dock;
    exports org.jhotdraw8.fxcontrols.colorchooser;
    exports org.jhotdraw8.fxcontrols.fontchooser;
}