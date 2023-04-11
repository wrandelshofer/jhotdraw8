/**
 * Provides examples for various features of the JHotDraw drawing framework.
 */
@SuppressWarnings("module")
module org.jhotdraw8.examples {
    requires transitive java.desktop;
    requires transitive org.jhotdraw8.draw;
    requires java.logging;
    requires java.prefs;
    requires transitive org.jhotdraw8.annotation;
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
    exports org.jhotdraw8.examples.colorchooser to javafx.fxml, javafx.graphics;
}