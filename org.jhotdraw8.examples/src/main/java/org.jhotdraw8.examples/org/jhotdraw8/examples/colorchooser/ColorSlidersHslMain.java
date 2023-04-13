package org.jhotdraw8.examples.colorchooser;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
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
import org.jhotdraw8.color.CssHslColorSpace;
import org.jhotdraw8.color.HlsColorSpace;
import org.jhotdraw8.color.HlsPhysiologicColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.OKHSLColorSpace;
import org.jhotdraw8.fxcontrols.colorchooser.ColorRectangleSlider;
import org.jhotdraw8.fxcontrols.colorchooser.ColorSlider;

import java.awt.color.ColorSpace;
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
public class ColorSlidersHslMain extends AbstractColorSlidersMain {


    @Override
    public void start(@NonNull Stage primaryStage) {
        VBox vbox = new VBox();

        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(12))));
        vbox.setSpacing(8);

        FloatProperty c0 = new SimpleFloatProperty();
        FloatProperty c1 = new SimpleFloatProperty();
        FloatProperty c2 = new SimpleFloatProperty();


        ColorRectangleSlider colorRectangleSlider = new ColorRectangleSlider();
        ColorSlider colorSlider = new ColorSlider();
        colorSlider.c0Property().bindBidirectional(c0);
        colorRectangleSlider.c0Property().bind(c0);
        colorRectangleSlider.adjustingProperty().bind(colorSlider.pressedProperty());
        colorRectangleSlider.c1Property().bindBidirectional(c1);
        colorRectangleSlider.c2Property().bindBidirectional(c2);

        ComboBox<AbstractNamedColorSpace> colorSpaceBox = createColorSpaceComboBox(
                new CssHslColorSpace(),
                new HlsColorSpace(),
                new HlsPhysiologicColorSpace(),
                new OKHSLColorSpace()
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

        ChangeListener<AbstractNamedColorSpace> csListener = (o, oldv, newv) -> {

            switch (newv.getType()) {
                case ColorSpace.TYPE_HSV -> {
                    colorSlider.setC1(1.0f);//saturation
                    colorSlider.setC2(1.0f);//value
                    colorRectangleSlider.setXComponentIndex(1);
                    colorRectangleSlider.setYComponentIndex(2);
                }
                case ColorSpace.TYPE_HLS -> {
                    colorSlider.setC1(0.5f);//lightness
                    colorSlider.setC2(1.0f);//saturation
                    colorRectangleSlider.setXComponentIndex(2);
                    colorRectangleSlider.setYComponentIndex(1);
                }
                default -> {
                    colorSlider.setC1(1f);//?
                    colorSlider.setC2(1f);//?
                    colorRectangleSlider.setXComponentIndex(1);
                    colorRectangleSlider.setYComponentIndex(2);
                }
                case NamedColorSpace.TYPE_HSL -> {
                    colorSlider.setC1(1.0f);//saturation
                    colorSlider.setC2(0.5f);//ligthness
                    colorRectangleSlider.setXComponentIndex(1);
                    colorRectangleSlider.setYComponentIndex(2);
                }
            }
        };
        colorSpaceBox.valueProperty().addListener(csListener);
        colorSpaceBox.setValue(colorSpaceBox.getItems().get(0));
        csListener.changed(colorSpaceBox.valueProperty(), colorSpaceBox.getValue(), colorSpaceBox.getValue());

        Scene scene = new Scene(vbox, 300, 250);

        primaryStage.setTitle("Hello HSL sliders!");
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

