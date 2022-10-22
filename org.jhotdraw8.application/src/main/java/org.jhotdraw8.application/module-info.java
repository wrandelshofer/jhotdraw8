/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.application.spi.ApplicationResourceBundleProvider;

@SuppressWarnings("module")
module org.jhotdraw8.application {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires java.logging;
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires transitive javafx.fxml;
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.collection;
    requires org.jhotdraw8.base;
    requires org.jhotdraw8.fxbase;

    exports org.jhotdraw8.application;
    exports org.jhotdraw8.application.action;
    exports org.jhotdraw8.application.action.edit;
    exports org.jhotdraw8.application.action.file;
    exports org.jhotdraw8.application.action.view;
    exports org.jhotdraw8.application.spi;
    exports org.jhotdraw8.application.undo;
    exports org.jhotdraw8.net;
    exports org.jhotdraw8.util;
    exports org.jhotdraw8.util.prefs;
    exports org.jhotdraw8.gui;
    exports org.jhotdraw8.text;
    exports org.jhotdraw8.application.action.app;
    exports org.jhotdraw8.application.resources;

    uses java.util.spi.ResourceBundleProvider;
    provides java.util.spi.ResourceBundleProvider with ApplicationResourceBundleProvider;

    uses org.jhotdraw8.fxbase.spi.NodeReaderProvider;
}