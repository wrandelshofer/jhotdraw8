/*
 * @(#)ColorChooserPaneMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorchooser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxcontrols.colorchooser.ColorChooserPane;

/**
 * Displays the ColorChooserPane in the primary stage.
 */
public class ColorChooserPaneMain extends Application {

    @Override
    public void start(@NonNull Stage primaryStage) {
        ColorChooserPane root = new ColorChooserPane();

        root.getModel().initWithDefaultValues();

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("JHotDraw8 Color Chooser");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(@NonNull String[] args) {
        launch(args);
    }

}

