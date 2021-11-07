module org.jhotdraw8.examples {
    requires java.desktop;
    requires org.jhotdraw8.draw;
    requires java.logging;
    requires java.prefs;

    exports org.jhotdraw8.examples.mini to javafx.graphics, javafx.fxml;
}