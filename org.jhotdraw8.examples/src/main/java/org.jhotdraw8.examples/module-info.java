@SuppressWarnings("module")
module org.jhotdraw8.examples {
    requires java.desktop;
    requires org.jhotdraw8.draw;
    requires java.logging;
    requires java.prefs;
    requires org.jhotdraw8.annotation;
    requires org.jhotdraw8.color;
    requires org.jhotdraw8.os;
    requires org.jhotdraw8.collection;
    requires org.jhotdraw8.font;
    requires org.jhotdraw8.dock;
    requires org.jhotdraw8.geom;
    requires org.jhotdraw8.base;
    requires org.jhotdraw8.fxbase;

    exports org.jhotdraw8.examples.mini to javafx.graphics, javafx.fxml;
    exports org.jhotdraw8.examples.colorspace;

    exports org.jhotdraw8.examples.fxml to javafx.graphics, javafx.fxml;
    opens org.jhotdraw8.examples.fxml to javafx.fxml;
}