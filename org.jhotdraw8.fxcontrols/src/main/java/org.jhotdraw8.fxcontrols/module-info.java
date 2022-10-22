/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.fxcontrols {
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.controls;
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.application;
    requires org.jhotdraw8.collection;
    requires org.jhotdraw8.fxbase;
    exports org.jhotdraw8.fxcontrols.dock;
    exports org.jhotdraw8.fxcontrols.colorchooser;
    exports org.jhotdraw8.fxcontrols.fontchooser;
}