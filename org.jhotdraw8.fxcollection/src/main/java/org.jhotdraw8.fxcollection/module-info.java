/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

@SuppressWarnings("module")
module org.jhotdraw8.fxcollection {
    requires transitive org.jhotdraw8.annotation;
    requires transitive javafx.base;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.fxbase;
    requires transitive javafx.controls;
}