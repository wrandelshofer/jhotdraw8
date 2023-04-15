package org.jhotdraw8.examples.colorchooser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxcontrols.colorchooser.ColorChooserPane;

public class ColorChooserPaneMain extends Application {

    @Override
    public void start(@NonNull Stage primaryStage) {
        ColorChooserPane root = new ColorChooserPane();

        root.getModel().initWithDefaultValues();

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
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

