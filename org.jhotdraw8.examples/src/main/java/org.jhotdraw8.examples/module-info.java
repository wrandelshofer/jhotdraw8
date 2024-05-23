/*
 * @(#)module-info.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

import org.jspecify.annotations.NullMarked;
/**
 * Provides examples for various features of the JHotDraw drawing framework.
 */
@SuppressWarnings("module")
@NullMarked
module org.jhotdraw8.examples {
    requires transitive java.desktop;
    requires transitive org.jhotdraw8.draw;
    requires transitive static org.jspecify;

    requires transitive org.jhotdraw8.color;
    requires transitive org.jhotdraw8.os;
    requires transitive org.jhotdraw8.collection;
    requires transitive org.jhotdraw8.fxcontrols;
    requires transitive org.jhotdraw8.geom;
    requires transitive org.jhotdraw8.base;
    requires transitive org.jhotdraw8.fxbase;

    exports org.jhotdraw8.examples.mini to javafx.graphics, javafx.fxml;
    exports org.jhotdraw8.examples.colorspace;

    exports org.jhotdraw8.examples.fxml to javafx.graphics, javafx.fxml;
    opens org.jhotdraw8.examples.fxml to javafx.fxml;
    opens org.jhotdraw8.examples.mini to javafx.fxml;
    exports org.jhotdraw8.examples.colorchooser to javafx.fxml, javafx.graphics;
}