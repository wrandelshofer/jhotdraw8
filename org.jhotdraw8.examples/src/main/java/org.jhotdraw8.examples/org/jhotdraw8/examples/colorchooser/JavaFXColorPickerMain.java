/*
 * @(#)JavaFXColorChooserMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorchooser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class JavaFXColorPickerMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ColorPicker colorPicker = new ColorPicker();
        Pane pane = new Pane();
        pane.getChildren().add(colorPicker);
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle("JavaFX ColorPicker");
        primaryStage.sizeToScene();
        primaryStage.show();
    }
}
