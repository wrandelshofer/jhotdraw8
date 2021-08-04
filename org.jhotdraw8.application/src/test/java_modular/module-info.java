/*
 * @(#)module-info.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
open module org.jhotdraw8.application {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires java.logging;
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires transitive javafx.fxml;
    requires org.junit.jupiter.api;
}