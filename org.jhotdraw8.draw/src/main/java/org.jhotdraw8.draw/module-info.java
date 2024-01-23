/*
 * @(#)module-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.draw.spi.DrawResourceBundleProvider;

/**
 * Defines a framework for vector drawing editors.
 */
@SuppressWarnings("module")
module org.jhotdraw8.draw {
    requires transitive java.desktop;
    requires transitive java.logging;
    requires transitive java.prefs;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires static org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.application;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.css;
    requires transitive org.jhotdraw8.fxbase;
    requires transitive org.jhotdraw8.fxcollection;
    requires transitive org.jhotdraw8.fxcontrols;
    requires transitive org.jhotdraw8.geom;
    requires transitive org.jhotdraw8.graph;
    requires transitive org.jhotdraw8.xml;
    requires org.jhotdraw8.icollection;

    opens org.jhotdraw8.draw.action.images;
    opens org.jhotdraw8.draw.gui to javafx.fxml;
    opens org.jhotdraw8.draw.inspector;
    opens org.jhotdraw8.draw.popup to javafx.fxml;
    opens org.jhotdraw8.draw;

    exports org.jhotdraw8.draw.action;
    exports org.jhotdraw8.draw.connector;
    exports org.jhotdraw8.draw.constrain;
    exports org.jhotdraw8.draw.css.converter;
    exports org.jhotdraw8.draw.css.function;
    exports org.jhotdraw8.draw.css.value;
    exports org.jhotdraw8.draw.figure;
    exports org.jhotdraw8.draw.gui;
    exports org.jhotdraw8.draw.handle;
    exports org.jhotdraw8.draw.input;
    exports org.jhotdraw8.draw.inspector;
    exports org.jhotdraw8.draw.io;
    exports org.jhotdraw8.draw.key;
    exports org.jhotdraw8.draw.locator;
    exports org.jhotdraw8.draw.model;
    exports org.jhotdraw8.draw.popup;
    exports org.jhotdraw8.draw.render;
    exports org.jhotdraw8.draw.spi;
    exports org.jhotdraw8.draw.tool;
    exports org.jhotdraw8.draw;
    exports org.jhotdraw8.draw.css.model;
    exports org.jhotdraw8.draw.xml.converter;
    exports org.jhotdraw8.draw.undo;

    provides java.util.spi.ResourceBundleProvider with DrawResourceBundleProvider;

}