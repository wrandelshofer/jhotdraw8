/*
 * @(#)module-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.application.spi.ApplicationResourceBundleProvider;
import org.jspecify.annotations.NullMarked;

/**
 * Defines a framework for document-oriented applications.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.application {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires java.logging;
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires transitive javafx.fxml;
    requires transitive org.jspecify;

    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.fxbase;

    exports org.jhotdraw8.application;
    exports org.jhotdraw8.application.action;
    exports org.jhotdraw8.application.action.edit;
    exports org.jhotdraw8.application.action.file;
    exports org.jhotdraw8.application.action.view;
    exports org.jhotdraw8.application.spi;
    exports org.jhotdraw8.application.action.app;
    exports org.jhotdraw8.application.resources;
    exports org.jhotdraw8.application.controls.urichooser;

    uses java.util.spi.ResourceBundleProvider;
    provides java.util.spi.ResourceBundleProvider with ApplicationResourceBundleProvider;

    uses org.jhotdraw8.fxbase.spi.NodeReaderProvider;
}