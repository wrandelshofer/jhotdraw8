/*
/*
 * @(#)module-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.fxcontrols.spi.FXControlsResourceBundleProvider;
import org.jspecify.annotations.NullMarked;

/**
 * Provides controls that are based on JavaFX controls.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.fxcontrols {
    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive org.jspecify;

    requires transitive org.jhotdraw8.application;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.fxbase;
    requires transitive org.jhotdraw8.os;
    requires transitive org.jhotdraw8.color;
    requires transitive org.jhotdraw8.geom;
    requires transitive org.jhotdraw8.icollection;
    requires java.logging;

    exports org.jhotdraw8.fxcontrols.dock;
    exports org.jhotdraw8.fxcontrols.colorchooser;
    exports org.jhotdraw8.fxcontrols.spi;
    exports org.jhotdraw8.fxcontrols.fontchooser;
    opens org.jhotdraw8.fxcontrols.colorchooser to javafx.fxml;
    opens org.jhotdraw8.fxcontrols.fontchooser to javafx.fxml;

    provides java.util.spi.ResourceBundleProvider with FXControlsResourceBundleProvider;
    uses FXControlsResourceBundleProvider;
}