/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.font {
    requires javafx.base;
    requires org.jhotdraw8.annotation;
    requires javafx.graphics;
    //FIXME should not depend on application!
    requires org.jhotdraw8.application;
    requires org.jhotdraw8.fxbase;

    exports org.jhotdraw8.font.fontchooser;
    opens org.jhotdraw8.font.fontchooser to javafx.fxml;
}