/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Defines operation specific functions.
 */
@SuppressWarnings("module")
module org.jhotdraw8.os {
    requires transitive java.xml;
    requires transitive javafx.graphics;
    requires static org.jhotdraw8.annotation;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.icollection;
    requires java.logging;

    exports org.jhotdraw8.os;
    exports org.jhotdraw8.os.macos;
}