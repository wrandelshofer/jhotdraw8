/*
 * @(#)ColorSlidersLabMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorchooser;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jhotdraw8.color.CieLabColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.OKLabColorSpace;
import org.jhotdraw8.fxcontrols.colorchooser.ColorRectangleSlider;
import org.jhotdraw8.fxcontrols.colorchooser.ColorSlider;

import java.util.List;

/**
 * Shows sliders for Lab color spaces.
 * <pre>
 *     a →
 * +-----------------+  +--+
 * | ↑               |  |  |↑
 * | b               |  |  |brightness/value
 * |                 |  |  |
 * +-----------------+  +--+
 * </pre>
 */
public class ColorSlidersLabMain extends AbstractColorSlidersMain {


    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(12))));
        vbox.setSpacing(8);

        HBox hbox = new HBox();
        ColorRectangleSlider colorRectangleSlider = new ColorRectangleSlider();
        ColorSlider colorSlider = new ColorSlider();
        colorSlider.setOrientation(Orientation.VERTICAL);
        colorSlider.setC0(0);
        colorSlider.setC1(0);
        colorSlider.setC2(0);
        colorSlider.setC3(0);

        FloatProperty c0 = new SimpleFloatProperty();
        FloatProperty c1 = new SimpleFloatProperty();
        FloatProperty c2 = new SimpleFloatProperty();

        colorSlider.c0Property().bindBidirectional(c0);
        colorRectangleSlider.c0Property().bind(c0);
        colorRectangleSlider.adjustingProperty().bind(colorSlider.pressedProperty());
        colorRectangleSlider.c1Property().bindBidirectional(c1);
        colorRectangleSlider.c2Property().bindBidirectional(c2);

        ComboBox<NamedColorSpace> colorSpaceBox = createColorSpaceComboBox(
                new CieLabColorSpace(),
                new OKLabColorSpace()
        );
        colorRectangleSlider.targetColorSpaceProperty().bind(colorSpaceBox.valueProperty());
        colorSlider.targetColorSpaceProperty().bind(colorSpaceBox.valueProperty());

        var bitDepthBox = createBitDepthComboBox();
        colorRectangleSlider.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        colorSlider.rgbFilterProperty().bind(bitDepthBox.valueProperty());

        HBox.setHgrow(colorRectangleSlider, Priority.ALWAYS);
        VBox.setVgrow(hbox, Priority.ALWAYS);

        hbox.getChildren().addAll(colorRectangleSlider, colorSlider);

        List<TextField> fields = createTextFields(c0, c1, c2);
        HBox componentsHBox = new HBox();
        componentsHBox.getChildren().addAll(colorSpaceBox);
        componentsHBox.getChildren().addAll(fields);
        vbox.getChildren().addAll(hbox, componentsHBox, bitDepthBox);

        Scene scene = new Scene(vbox, 300, 250);

        primaryStage.setTitle("Hello Lab colors!");
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

