/*
 * @(#)UnifiedStageApplication.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jhotdraw8.os.macos.MacOSPreferencesUtil;


public class UnifiedStageApplication extends Application {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // See
        // https://bugs.openjdk.java.net/browse/JDK-8091497

        if (Screen.getPrimary().getOutputScaleX() >= 2.0) {
            // The following settings improve font rendering quality on
            // retina displays (no color fringes around characters).
            System.setProperty("prism.subpixeltext", "on");
            System.setProperty("prism.lcdtext", "false");
        } else {
            // The following settings improve font rendering on
            // low-res lcd displays (less color fringes around characters).
            System.setProperty("prism.text", "t2k");
            System.setProperty("prism.lcdtext", "true");
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.initStyle(StageStyle.UNIFIED);

        Parent root = FXMLLoader.load(UnifiedStageApplication.class.getResource("MasterDetail.fxml"));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        final Object value = MacOSPreferencesUtil.get(MacOSPreferencesUtil.GLOBAL_PREFERENCES, "AppleInterfaceStyle");
        if ("Dark".equals(value)) {
            scene.getStylesheets().add(UnifiedStageApplication.class.getResource("dark-theme.css").toString());
        } else {
            scene.getStylesheets().add(UnifiedStageApplication.class.getResource("light-theme.css").toString());
        }


        primaryStage.setScene(scene);

        primaryStage.setTitle("Unified Stage");
        primaryStage.setWidth(640);
        primaryStage.setHeight(480);
        primaryStage.show();
    }
}
