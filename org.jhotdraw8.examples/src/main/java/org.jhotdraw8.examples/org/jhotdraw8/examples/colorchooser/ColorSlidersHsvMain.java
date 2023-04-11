package org.jhotdraw8.examples.colorchooser;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.AbstractNamedColorSpace;
import org.jhotdraw8.color.HsvColorSpace;
import org.jhotdraw8.color.HsvPhysiologicColorSpace;
import org.jhotdraw8.fxcontrols.colorchooser.ColorRectangleSlider;
import org.jhotdraw8.fxcontrols.colorchooser.ColorSlider;

import java.util.List;

/**
 * Shows sliders for HSB/HSV color spaces.
 * <pre>
 *     saturation →
 * +-----------------+
 * |                 | ↑
 * |                 | brightness/value
 * |                 |
 * +-----------------+
 *
 * +-----------------+
 * |   hue →         |
 * +-----------------+
 * </pre>
 */
public class ColorSlidersHsvMain extends AbstractColorSlidersMain {


    @Override
    public void start(@NonNull Stage primaryStage) {
        VBox vbox = new VBox();

        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(12))));
        vbox.setSpacing(8);

        ColorRectangleSlider colorRectangleSlider = new ColorRectangleSlider();
        ColorSlider colorSlider = new ColorSlider();

        FloatProperty c0 = new SimpleFloatProperty();
        FloatProperty c1 = new SimpleFloatProperty();
        FloatProperty c2 = new SimpleFloatProperty();

        colorSlider.c0Property().bindBidirectional(c0);
        colorRectangleSlider.c0Property().bind(c0);
        colorRectangleSlider.adjustingProperty().bind(colorSlider.pressedProperty());
        colorRectangleSlider.c1Property().bindBidirectional(c1);
        colorRectangleSlider.c2Property().bindBidirectional(c2);


        ComboBox<AbstractNamedColorSpace> colorSpaceBox = createColorSpaceComboBox(
                HsvColorSpace.getInstance(),
                HsvPhysiologicColorSpace.getInstance()
        );
        colorRectangleSlider.colorSpaceProperty().bind(colorSpaceBox.valueProperty());
        colorSlider.colorSpaceProperty().bind(colorSpaceBox.valueProperty());

        var bitDepthBox = createBitDepthComboBox();
        colorRectangleSlider.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        colorSlider.rgbFilterProperty().bind(bitDepthBox.valueProperty());

        VBox.setVgrow(colorRectangleSlider, Priority.ALWAYS);

        List<TextField> fields = createTextFields(c0, c1, c2);
        HBox componentsHBox = new HBox();
        componentsHBox.getChildren().addAll(colorSpaceBox);
        componentsHBox.getChildren().addAll(fields);
        vbox.getChildren().addAll(colorRectangleSlider, colorSlider, componentsHBox, bitDepthBox);


        Scene scene = new Scene(vbox, 300, 250);

        primaryStage.setTitle("Hello HSV sliders!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    /**
     * @param args the command line arguments
     */
    public static void main(@NonNull String[] args) {
        launch(args);
    }

}

