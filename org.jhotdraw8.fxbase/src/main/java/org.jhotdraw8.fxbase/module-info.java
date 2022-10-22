/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

module org.jhotdraw8.fxbase {
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.base;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires org.jhotdraw8.collection;
    requires javafx.controls;
    exports org.jhotdraw8.fxbase.spi;
    exports org.jhotdraw8.fxbase.beans;
    exports org.jhotdraw8.fxbase.tree;
    exports org.jhotdraw8.fxbase.clipboard;
    exports org.jhotdraw8.fxbase.control;
    exports org.jhotdraw8.fxbase.converter;
    exports org.jhotdraw8.fxbase.binding;
    exports org.jhotdraw8.fxbase.styleable;
    exports org.jhotdraw8.fxbase.concurrent;
    exports org.jhotdraw8.fxbase.fxml;
    exports org.jhotdraw8.fxbase.event;

    provides org.jhotdraw8.fxbase.spi.NodeReaderProvider
            with org.jhotdraw8.fxbase.spi.FxmlNodeReaderProvider,
                    org.jhotdraw8.fxbase.spi.ImageNodeReaderProvider;
    uses org.jhotdraw8.fxbase.spi.NodeReaderProvider;
}