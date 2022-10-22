/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.css {
    exports org.jhotdraw8.css;
    exports org.jhotdraw8.css.converter;
    exports org.jhotdraw8.css.ast;
    exports org.jhotdraw8.css.function;
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.application;
    requires org.jhotdraw8.collection;
    requires java.logging;
    requires org.jhotdraw8.base;
}