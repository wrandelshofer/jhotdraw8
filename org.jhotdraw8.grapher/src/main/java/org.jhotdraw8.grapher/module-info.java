/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.grapher.spi.GrapherResourceBundleProvider;

@SuppressWarnings("module")
module org.jhotdraw8.grapher {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires java.logging;
    requires java.desktop;
    requires transitive java.prefs;
    requires transitive javafx.fxml;
    requires jdk.javadoc;
    requires transitive org.jhotdraw8.draw;
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.os;
    requires org.jhotdraw8.collection;
    requires org.jhotdraw8.dock;
    requires org.jhotdraw8.base;

    opens org.jhotdraw8.grapher to javafx.fxml, javafx.graphics;

    uses java.util.spi.ResourceBundleProvider;
    provides java.util.spi.ResourceBundleProvider with GrapherResourceBundleProvider;
}