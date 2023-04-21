/*
 * @(#)TestApplication.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.teddy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class TestApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.initStyle(StageStyle.UNIFIED);

        BorderPane bp = new BorderPane();
        Button b = new Button("Hello");
        bp.setBottom(b);
        Scene scene = new Scene(bp);
        scene.setFill(Color.TRANSPARENT);
        // bright mode
        bp.setBackground(new Background(new BackgroundFill(Color.rgb(207, 207, 207), CornerRadii.EMPTY, null)));
        // dark mode
        bp.setBackground(new Background(new BackgroundFill(Color.rgb(53, 53, 53), CornerRadii.EMPTY, null)));
        primaryStage.setScene(scene);

        primaryStage.setTitle("Hello World");
        primaryStage.setWidth(640);
        primaryStage.setHeight(480);
        primaryStage.show();
    }
}
