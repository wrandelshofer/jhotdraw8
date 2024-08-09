/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;
/**
 * Provides basic utility classes used by the JHotDraw framework.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.base {
    requires transitive org.jspecify;

    requires java.logging;
    exports org.jhotdraw8.base.converter;
    exports org.jhotdraw8.base.io;
    exports org.jhotdraw8.base.function;
    exports org.jhotdraw8.base.event;
    exports org.jhotdraw8.base.text;
    exports org.jhotdraw8.base.concurrent;
    exports org.jhotdraw8.base.net;
}