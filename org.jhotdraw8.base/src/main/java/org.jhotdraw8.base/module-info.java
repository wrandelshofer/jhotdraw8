/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Provides common utility classes.
 */
@SuppressWarnings("module")
module org.jhotdraw8.base {
    requires transitive org.jhotdraw8.annotation;
    requires java.logging;
    requires java.prefs;
    exports org.jhotdraw8.base.converter;
    exports org.jhotdraw8.base.io;
    exports org.jhotdraw8.base.precondition;
    exports org.jhotdraw8.base.function;
    exports org.jhotdraw8.base.util;
    exports org.jhotdraw8.base.event;
    exports org.jhotdraw8.base.text;
    exports org.jhotdraw8.base.concurrent;
    exports org.jhotdraw8.base.net;
}