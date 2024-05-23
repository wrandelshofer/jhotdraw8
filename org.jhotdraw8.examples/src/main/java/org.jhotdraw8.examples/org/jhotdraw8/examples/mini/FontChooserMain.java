/*
 * @(#)FontChooserMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.jhotdraw8.fxcontrols.fontchooser.FontDialog;
import org.jhotdraw8.fxcontrols.fontchooser.FontFamilyDialog;
import org.jhotdraw8.fxcontrols.fontchooser.FontFamilySize;

/**
 * FontChooserMain.
 *
 * @author Werner Randelshofer
 */
public class FontChooserMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label fontChooserLabel = new Label("No font selected");
        Button fontChooserButton = new Button("Open FontChooser");
        fontChooserButton.setOnAction(event -> {
            FontDialog fd = new FontDialog();
            FontFamilySize fontName = fd.showAndWait().orElse(null);
            if (fontName != null) {
                fontChooserLabel.setText(fontName.family() + " " + fontName.size());
                fontChooserLabel.setFont(new Font(fontName.family(), fontName.size()));
            }
        });

        Label fontFamilyLabel = new Label("No font family selected");
        Button fontFamilyChooserButton = new Button("Open FontFamilyChooser");
        fontFamilyChooserButton.setOnAction(event -> {
            FontFamilyDialog fd = new FontFamilyDialog();
            String fontFamily = fd.showAndWait().orElse(null);
            if (fontFamily != null) {
                fontFamilyLabel.setText(fontFamily);
                fontFamilyLabel.setFont(new Font(fontFamily, 13f));
            }
        });

        VBox root = new VBox();
        root.getChildren().addAll(fontChooserLabel, fontChooserButton, fontFamilyLabel, fontFamilyChooserButton);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
