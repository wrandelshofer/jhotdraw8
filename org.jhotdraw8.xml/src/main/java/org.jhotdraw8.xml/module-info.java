/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Provides XML interoperability for drawing editors.
 */
@SuppressWarnings("module")
module org.jhotdraw8.xml {
    requires transitive java.logging;
    requires transitive java.xml;
    requires transitive javafx.graphics;
    requires transitive org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.css;
    requires transitive org.jhotdraw8.fxbase;
    requires transitive org.jhotdraw8.collection;

    exports org.jhotdraw8.xml;
    exports org.jhotdraw8.xml.converter;
}