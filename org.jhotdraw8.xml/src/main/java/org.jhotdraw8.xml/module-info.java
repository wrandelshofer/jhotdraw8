/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Provides XML interoperability for drawing editors.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.xml {
    requires transitive java.logging;
    requires transitive java.xml;
    requires transitive javafx.graphics;
    requires transitive static org.jspecify;

    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.css;
    requires transitive org.jhotdraw8.fxbase;
    requires transitive org.jhotdraw8.collection;
    requires org.jhotdraw8.icollection;

    exports org.jhotdraw8.xml;
    exports org.jhotdraw8.xml.converter;
}