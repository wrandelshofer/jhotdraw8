package org.jhotdraw8.examples.colorchooser;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.NamedColorSpaceAdapter;
import org.jhotdraw8.fxcontrols.colorchooser.ColorSlider;

import java.awt.color.ColorSpace;

/**
 * Show sliders for RGB color spaces.
 * <pre>
 * +-----------------+
 * |   red →         |
 * +-----------------+
 * +-----------------+
 * |   green →       |
 * +-----------------+
 * +-----------------+
 * |   blue →        |
 * +-----------------+
 * </pre>
 */
public class ColorSlidersTristimulusMain extends AbstractColorSlidersMain {


    @Override
    public void start(@NonNull Stage primaryStage) {
        VBox vbox = new VBox();

        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(12))));
        vbox.setSpacing(8);

        ColorSlider sliderR = new ColorSlider();
        ColorSlider sliderG = new ColorSlider();
        ColorSlider sliderB = new ColorSlider();

        sliderR.setComponentIndex(0);
        sliderG.setComponentIndex(1);
        sliderB.setComponentIndex(2);

        FloatProperty red = new SimpleFloatProperty();
        FloatProperty green = new SimpleFloatProperty();
        FloatProperty blue = new SimpleFloatProperty();
        sliderR.c0Property().bindBidirectional(red);
        sliderR.c1Property().bind(green);
        sliderR.c2Property().bind(blue);
        sliderG.c0Property().bind(red);
        sliderG.c1Property().bindBidirectional(green);
        sliderG.c2Property().bind(blue);
        sliderB.c0Property().bind(red);
        sliderB.c1Property().bind(green);
        sliderB.c2Property().bindBidirectional(blue);

        ComboBox<NamedColorSpace> colorSpaceBox = createColorSpaceComboBox(
                new NamedColorSpaceAdapter("sRGB", ColorSpace.getInstance(ColorSpace.CS_sRGB)),
                new NamedColorSpaceAdapter("CIE XYZ", ColorSpace.getInstance(ColorSpace.CS_CIEXYZ)),
                new NamedColorSpaceAdapter("PYCC", ColorSpace.getInstance(ColorSpace.CS_PYCC))
        );
        sliderR.colorSpaceProperty().bind(colorSpaceBox.valueProperty());
        sliderG.colorSpaceProperty().bind(colorSpaceBox.valueProperty());
        sliderB.colorSpaceProperty().bind(colorSpaceBox.valueProperty());

        var bitDepthBox = createBitDepthComboBox();
        sliderR.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        sliderG.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        sliderB.rgbFilterProperty().bind(bitDepthBox.valueProperty());


        vbox.getChildren().addAll(sliderR, sliderG, sliderB, colorSpaceBox, bitDepthBox);


        Scene scene = new Scene(vbox, 300, 250);

        primaryStage.setTitle("Hello Tristimulus sliders!");
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

