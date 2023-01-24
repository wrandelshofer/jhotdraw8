/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

@SuppressWarnings("module")
module org.jhotdraw8.os {
    exports org.jhotdraw8.os;
    exports org.jhotdraw8.os.macos;
    requires transitive org.jhotdraw8.annotation;
    requires transitive java.xml;
}