/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Defines operation specific functions.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.os {
    requires transitive java.xml;
    requires transitive javafx.graphics;
    requires transitive org.jspecify;

    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.icollection;
    requires java.logging;

    exports org.jhotdraw8.os;
    exports org.jhotdraw8.os.macos;
}