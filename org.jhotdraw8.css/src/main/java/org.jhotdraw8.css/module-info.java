/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Defines an interpreter for CSS stylesheets.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.css {

    requires transitive org.jhotdraw8.base;
    requires org.jhotdraw8.color;
    requires org.jhotdraw8.fxcollection;
    requires java.logging;

    exports org.jhotdraw8.css.ast;
    exports org.jhotdraw8.css.converter;
    exports org.jhotdraw8.css.function;
    exports org.jhotdraw8.css.io;
    exports org.jhotdraw8.css.manager;
    exports org.jhotdraw8.css.model;
    exports org.jhotdraw8.css.parser;
    exports org.jhotdraw8.css.value;
    exports org.jhotdraw8.css.util;
    exports org.jhotdraw8.css.render;
}