/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;

/**
 * Provides JavaFX-specific utility classes used by the JHotDraw framework.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.fxbase {
    requires transitive org.jspecify;

    requires transitive org.jhotdraw8.base;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.fxml;
    requires transitive org.jhotdraw8.fxcollection;
    requires transitive javafx.controls;
    requires transitive java.desktop;
    exports org.jhotdraw8.fxbase.spi;
    exports org.jhotdraw8.fxbase.beans;
    exports org.jhotdraw8.fxbase.clipboard;
    exports org.jhotdraw8.fxbase.control;
    exports org.jhotdraw8.fxbase.converter;
    exports org.jhotdraw8.fxbase.binding;
    exports org.jhotdraw8.fxbase.styleable;
    exports org.jhotdraw8.fxbase.concurrent;
    exports org.jhotdraw8.fxbase.fxml;
    exports org.jhotdraw8.fxbase.event;
    exports org.jhotdraw8.fxbase.undo;
    exports org.jhotdraw8.fxbase.transition;
    exports org.jhotdraw8.fxbase.lang;
    exports org.jhotdraw8.fxbase.tree;

    provides org.jhotdraw8.fxbase.spi.NodeReaderProvider
            with org.jhotdraw8.fxbase.spi.FxmlNodeReaderProvider,
                    org.jhotdraw8.fxbase.spi.ImageNodeReaderProvider;
    uses org.jhotdraw8.fxbase.spi.NodeReaderProvider;
}