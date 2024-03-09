/*
 * @(#)ChromaticityDiagramMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorspace;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jhotdraw8.color.A98RgbColorSpace;
import org.jhotdraw8.color.CieRgbColorSpace;
import org.jhotdraw8.color.CmykNominalColorSpace;
import org.jhotdraw8.color.D50XyzColorSpace;
import org.jhotdraw8.color.DisplayP3ColorSpace;
import org.jhotdraw8.color.LinearSrgbColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.ProPhotoRgbColorSpace;
import org.jhotdraw8.color.Rec2020ColorSpace;
import org.jhotdraw8.color.SrgbColorSpace;

public class ChromaticityDiagramMain extends Application {
    public ChromaticityDiagramMain() {
    }


    @Override
    public void start(Stage stage) {
        ChromaticityDiagram chromaticityDiagram = new ChromaticityDiagram();
        ComboBox<NamedColorSpace> csComboBox = new ComboBox<>();
        csComboBox.getItems().setAll(
                new A98RgbColorSpace(),
                //new CieLabColorSpace(),
                //new CieLchColorSpace(),
                new CieRgbColorSpace(),
                new D50XyzColorSpace(),
                new CmykNominalColorSpace(),
                //new D50XyzColorSpace(),
                new DisplayP3ColorSpace(),
                //new HlsColorSpace(),
                //new HlsPhysiologicColorSpace(),
                //new HlsuvColorSpace(),
                //new HpluvColorSpace(),
                //new HsbColorSpace(),
                //new HsvColorSpace(),
                //new HsvPhysiologicColorSpace(),
                new LinearSrgbColorSpace(),
                //new OKHlsColorSpace(),
                //new OKLabColorSpace(),
                //new OKLchColorSpace(),
                new ProPhotoRgbColorSpace(),
                new Rec2020ColorSpace(),
                new SrgbColorSpace()
        );
        csComboBox.setValue(csComboBox.getItems().getFirst());
        ComboBox<NamedColorSpace> displayCsComboBox = new ComboBox<>();
        displayCsComboBox.getItems().setAll(
                new SrgbColorSpace(),
                new DisplayP3ColorSpace()
        );
        displayCsComboBox.setValue(displayCsComboBox.getItems().getFirst());
        chromaticityDiagram.colorSpaceProperty().bind(csComboBox.valueProperty());
        chromaticityDiagram.displayColorSpaceProperty().bind(displayCsComboBox.valueProperty());
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        VBox.setVgrow(chromaticityDiagram, Priority.ALWAYS);
        VBox.setVgrow(hbox, Priority.NEVER);
        hbox.setMinHeight(50);
        hbox.setPrefHeight(50);
        hbox.setMaxHeight(50);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setFillHeight(false);
        hbox.getChildren().addAll(new Label("color space:"), csComboBox, new Label("display:"), displayCsComboBox);
        vbox.getChildren().addAll(chromaticityDiagram, hbox);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.setTitle("ChromacityDiagram Demo");
        stage.setWidth(400);
        stage.setHeight(400);
        stage.show();
    }
}
