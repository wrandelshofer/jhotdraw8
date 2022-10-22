/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.draw.spi.DrawResourceBundleProvider;
import org.jhotdraw8.draw.spi.SvgImageReaderProvider;

@SuppressWarnings("module")
module org.jhotdraw8.draw {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive java.logging;
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires transitive org.jhotdraw8.application;
    requires transitive org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.font;
    requires transitive org.jhotdraw8.css;
    requires transitive org.jhotdraw8.geom;
    requires transitive org.jhotdraw8.graph;
    requires transitive org.jhotdraw8.fxbase;

    opens org.jhotdraw8.draw.inspector;
    opens org.jhotdraw8.draw;
    opens org.jhotdraw8.draw.action.images;
    opens org.jhotdraw8.draw.gui to javafx.fxml;
    opens org.jhotdraw8.draw.popup to javafx.fxml;

    exports org.jhotdraw8.draw.css.text;
    exports org.jhotdraw8.draw.css.function;
    exports org.jhotdraw8.draw;
    exports org.jhotdraw8.draw.action;
    exports org.jhotdraw8.draw.constrain;
    exports org.jhotdraw8.draw.css;
    exports org.jhotdraw8.draw.figure;
    exports org.jhotdraw8.draw.handle;
    exports org.jhotdraw8.draw.inspector;
    exports org.jhotdraw8.draw.popup;
    exports org.jhotdraw8.draw.input;
    exports org.jhotdraw8.draw.io;
    exports org.jhotdraw8.draw.tool;
    exports org.jhotdraw8.draw.spi;
    exports org.jhotdraw8.draw.model;
    exports org.jhotdraw8.draw.key;
    exports org.jhotdraw8.draw.render;
    exports org.jhotdraw8.draw.connector;
    exports org.jhotdraw8.draw.locator;
    exports org.jhotdraw8.xml.text;
    exports org.jhotdraw8.xml;
    exports org.jhotdraw8.draw.gui;
    exports org.jhotdraw8.svg.io;

    provides java.util.spi.ResourceBundleProvider with DrawResourceBundleProvider;
    provides org.jhotdraw8.fxbase.spi.NodeReaderProvider with SvgImageReaderProvider;

}