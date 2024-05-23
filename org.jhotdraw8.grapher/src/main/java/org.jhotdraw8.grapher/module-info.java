/*
 * @(#)module-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.grapher.spi.GrapherResourceBundleProvider;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.grapher {
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.draw;
    requires transitive org.jhotdraw8.fxcontrols;
    requires transitive org.jhotdraw8.os;
    requires transitive org.jhotdraw8.svg;
    requires transitive org.jhotdraw8.theme;
    requires transitive static org.jspecify;


    opens org.jhotdraw8.grapher to javafx.fxml, javafx.graphics;

    uses java.util.spi.ResourceBundleProvider;
    provides java.util.spi.ResourceBundleProvider with GrapherResourceBundleProvider;
}