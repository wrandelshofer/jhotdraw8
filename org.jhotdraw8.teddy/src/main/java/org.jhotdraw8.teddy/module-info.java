/*
 * @(#)module-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.teddy.spi.TeddyResourceBundleProvider;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.teddy {
    requires transitive java.desktop;
    requires transitive org.jhotdraw8.application;
    requires transitive org.jspecify;

    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.fxcontrols;
    requires transitive org.jhotdraw8.fxbase;
    requires transitive org.jhotdraw8.base;
    requires transitive javafx.base;
    provides java.util.spi.ResourceBundleProvider with TeddyResourceBundleProvider;

    opens org.jhotdraw8.teddy
            to javafx.fxml, javafx.graphics;
}