/*
 * @(#)module-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.fxcontrols.spi.FXControlsResourceBundleProvider;
/**
 * Provides controls that are based on JavaFX controls.
 */
@SuppressWarnings("module")
module org.jhotdraw8.fxcontrols {
    requires transitive java.desktop;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires static org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.application;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.fxbase;
    requires transitive org.jhotdraw8.color;
    requires transitive org.jhotdraw8.geom;
    requires org.jhotdraw8.icollection;
    exports org.jhotdraw8.fxcontrols.dock;
    exports org.jhotdraw8.fxcontrols.colorchooser;
    exports org.jhotdraw8.fxcontrols.spi;
    exports org.jhotdraw8.fxcontrols.fontchooser;
    opens org.jhotdraw8.fxcontrols.colorchooser to javafx.fxml;

    provides java.util.spi.ResourceBundleProvider with FXControlsResourceBundleProvider;

}