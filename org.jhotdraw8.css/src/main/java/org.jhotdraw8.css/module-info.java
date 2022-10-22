/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.css {
    exports org.jhotdraw8.css.converter;
    exports org.jhotdraw8.css.ast;
    exports org.jhotdraw8.css.function;
    exports org.jhotdraw8.css.parser;
    exports org.jhotdraw8.css.value;
    exports org.jhotdraw8.css.model;
    exports org.jhotdraw8.css.manager;
    exports org.jhotdraw8.css.io;
    requires transitive org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.collection;
    requires java.logging;
    requires transitive org.jhotdraw8.base;
    requires transitive javafx.graphics;
    requires transitive java.xml;
}