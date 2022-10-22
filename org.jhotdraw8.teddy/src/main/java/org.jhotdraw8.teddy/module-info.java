/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.teddy.spi.TeddyResourceBundleProvider;

@SuppressWarnings("module")
module org.jhotdraw8.teddy {
    requires java.desktop;
    requires java.prefs;
    requires org.jhotdraw8.application;
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.collection;
    requires org.jhotdraw8.fxcontrols;
    requires org.jhotdraw8.fxbase;
    requires org.jhotdraw8.base;
    provides java.util.spi.ResourceBundleProvider with TeddyResourceBundleProvider;

    opens org.jhotdraw8.teddy
            to javafx.fxml, javafx.graphics;
}