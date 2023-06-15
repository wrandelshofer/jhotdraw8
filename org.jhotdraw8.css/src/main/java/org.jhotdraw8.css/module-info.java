/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines an interpreter for CSS stylesheets.
 */
@SuppressWarnings("module")
module org.jhotdraw8.css {
    requires transitive java.logging;
    requires transitive java.xml;
    requires transitive javafx.graphics;
    requires static org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.collection;

    exports org.jhotdraw8.css.ast;
    exports org.jhotdraw8.css.converter;
    exports org.jhotdraw8.css.function;
    exports org.jhotdraw8.css.io;
    exports org.jhotdraw8.css.manager;
    exports org.jhotdraw8.css.model;
    exports org.jhotdraw8.css.parser;
    exports org.jhotdraw8.css.value;
    exports org.jhotdraw8.css.util;
}