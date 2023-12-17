/*
 * @(#)module-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

import org.jhotdraw8.svg.spi.SvgImageReaderProvider;

/**
 * Provides SVG interoperability for drawing editors.
 */
@SuppressWarnings("module")
module org.jhotdraw8.svg {
    requires transitive java.logging;
    requires transitive java.xml;
    requires transitive javafx.graphics;
    requires static org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.css;
    requires transitive org.jhotdraw8.draw;
    requires org.jhotdraw8.icollection;

    exports org.jhotdraw8.svg.io;
    exports org.jhotdraw8.svg.gui;
    exports org.jhotdraw8.svg.figure;
    exports org.jhotdraw8.svg.draw.figure;

    opens org.jhotdraw8.svg.gui to javafx.fxml;

    provides org.jhotdraw8.fxbase.spi.NodeReaderProvider with SvgImageReaderProvider;
}