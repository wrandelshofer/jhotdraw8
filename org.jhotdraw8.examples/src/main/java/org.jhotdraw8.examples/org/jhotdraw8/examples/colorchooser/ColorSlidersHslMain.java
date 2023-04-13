package org.jhotdraw8.examples.colorchooser;

import javafx.beans.InvalidationListener;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.CieLchColorSpace;
import org.jhotdraw8.color.CssHslColorSpace;
import org.jhotdraw8.color.DisplayP3ColorSpace;
import org.jhotdraw8.color.HlsColorSpace;
import org.jhotdraw8.color.HlsPhysiologicColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.OKHSLColorSpace;
import org.jhotdraw8.color.OKLchColorSpace;
import org.jhotdraw8.color.RgbBitConverters;
import org.jhotdraw8.color.SrgbColorSpace;
import org.jhotdraw8.fxcontrols.colorchooser.AlphaSlider;
import org.jhotdraw8.fxcontrols.colorchooser.ColorRectangleSlider;
import org.jhotdraw8.fxcontrols.colorchooser.ColorSlider;

import java.awt.color.ColorSpace;
import java.util.List;

import static org.jhotdraw8.base.util.MathUtil.clamp;

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
        // start is called on the FX Application Thread,
        // so Thread.currentThread() is the FX application thread:
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("Handler caught exception: " + throwable.getMessage());
        });

        VBox vbox = new VBox();

        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(12))));
        vbox.setSpacing(8);

        FloatProperty c0 = new SimpleFloatProperty();
        FloatProperty c1 = new SimpleFloatProperty();
        FloatProperty c2 = new SimpleFloatProperty();
        FloatProperty alpha = new SimpleFloatProperty(1f);
        ObjectProperty<NamedColorSpace> colorSpace = new SimpleObjectProperty<>(new CssHslColorSpace());
        ObjectProperty<NamedColorSpace> displayColorSpace = new SimpleObjectProperty<>(new SrgbColorSpace());


        ColorRectangleSlider colorRectangleSlider = new ColorRectangleSlider();
        ColorSlider hueSlider = new ColorSlider();
        AlphaSlider alphaSlider = new AlphaSlider();
        hueSlider.c0Property().bindBidirectional(c0);
        alphaSlider.alphaProperty().bindBidirectional(alpha);
        alphaSlider.c0Property().bind(c0);
        alphaSlider.c1Property().bind(c1);
        alphaSlider.c2Property().bind(c2);
        colorRectangleSlider.c0Property().bind(c0);
        colorRectangleSlider.adjustingProperty().bind(hueSlider.pressedProperty());
        colorRectangleSlider.c1Property().bindBidirectional(c1);
        colorRectangleSlider.c2Property().bindBidirectional(c2);

        ComboBox<NamedColorSpace> colorSpaceBox = createColorSpaceComboBox(
                new CssHslColorSpace(),
                new HlsColorSpace(),
                new HlsPhysiologicColorSpace(),
                new OKHSLColorSpace(),
                new OKLchColorSpace(),
                new CieLchColorSpace()
        );
        ComboBox<NamedColorSpace> displayColorSpaceBox = createColorSpaceComboBox(
                new SrgbColorSpace(),
                new DisplayP3ColorSpace()
        );
        colorSpaceBox.valueProperty().bindBidirectional(colorSpace);
        displayColorSpaceBox.valueProperty().bindBidirectional(displayColorSpace);
        colorRectangleSlider.colorSpaceProperty().bind(colorSpace);
        hueSlider.colorSpaceProperty().bind(colorSpace);
        alphaSlider.colorSpaceProperty().bind(colorSpace);
        colorRectangleSlider.displayColorSpaceProperty().bind(displayColorSpace);
        hueSlider.displayColorSpaceProperty().bind(displayColorSpace);
        alphaSlider.displayColorSpaceProperty().bind(displayColorSpace);

        var bitDepthBox = createBitDepthComboBox();
        colorRectangleSlider.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        hueSlider.rgbFilterProperty().bind(bitDepthBox.valueProperty());

        VBox.setVgrow(colorRectangleSlider, Priority.ALWAYS);
        Region selectedColor = new Region();
        Label hexRgbLabel = new Label();
        hexRgbLabel.setMinWidth(80);
        hexRgbLabel.setPrefWidth(80);
        hexRgbLabel.setMaxWidth(80);
        selectedColor.setMinSize(40, 40);
        selectedColor.setMaxSize(40, 40);
        selectedColor.setPrefSize(40, 40);
        InvalidationListener updateSelectedColor = i -> {
            float[] component = {c0.floatValue(), c1.floatValue(), c2.floatValue()};
            float[] rgb = new float[3];
            displayColorSpace.get().fromRGB(colorSpace.get().toRGB(component, rgb), rgb);
            Color color = new Color(clamp(rgb[0], 0, 1), clamp(rgb[1], 0, 1), clamp(rgb[2], 0, 1), alpha.floatValue());
            selectedColor.setBackground(new Background(new BackgroundFill(color, null, null)));
            String hexStr = "00000000" + Integer.toHexString(RgbBitConverters.rgbFloatToArgb32(rgb, alpha.floatValue()));
            hexStr = hexStr.substring(hexStr.length() - 8);
            hexRgbLabel.setText("#" + hexStr.substring(2) + hexStr.substring(0, 2));
        };
        c0.addListener(updateSelectedColor);
        c1.addListener(updateSelectedColor);
        c2.addListener(updateSelectedColor);
        alpha.addListener(updateSelectedColor);
        displayColorSpace.addListener(updateSelectedColor);
        colorSpace.addListener(updateSelectedColor);

        List<TextField> fields = createTextFields(c0, c1, c2);
        HBox componentsHBox = new HBox();
        componentsHBox.getChildren().addAll(selectedColor, hexRgbLabel, colorSpaceBox);
        componentsHBox.getChildren().addAll(fields);
        HBox displayHBox = new HBox();
        displayHBox.getChildren().addAll(new Label("Display:"), displayColorSpaceBox, new Label("Bit Depth:"), bitDepthBox);
        vbox.getChildren().addAll(colorRectangleSlider, hueSlider, alphaSlider, componentsHBox,
                displayHBox);

        ChangeListener<NamedColorSpace> csListener = (o, oldv, newv) -> {
            hueSlider.c0Property().unbindBidirectional(c0);
            hueSlider.c0Property().unbindBidirectional(c1);
            hueSlider.c0Property().unbindBidirectional(c2);
            hueSlider.c0Property().unbind();
            colorRectangleSlider.c0Property().unbindBidirectional(c2);
            colorRectangleSlider.c1Property().unbindBidirectional(c0);
            colorRectangleSlider.c1Property().unbindBidirectional(c1);
            colorRectangleSlider.c1Property().unbindBidirectional(c2);
            colorRectangleSlider.c2Property().unbindBidirectional(c0);
            colorRectangleSlider.c2Property().unbindBidirectional(c1);
            colorRectangleSlider.c2Property().unbindBidirectional(c2);
            colorRectangleSlider.c0Property().unbind();
            colorRectangleSlider.c1Property().unbind();
            colorRectangleSlider.c2Property().unbind();

            float[] xyz = oldv.toCIEXYZ(new float[]{c0.floatValue(), c1.floatValue(), c2.floatValue()});
            float[] c = newv.fromCIEXYZ(xyz);
            c0.set(c[0]);
            c1.set(c[1]);
            c2.set(c[2]);

            switch (newv.getType()) {
                case ColorSpace.TYPE_HSV -> {
                    hueSlider.setComponentIndex(0);//hue
                    hueSlider.c0Property().bindBidirectional(c0);
                    hueSlider.setC1(1.0f);//saturation
                    hueSlider.setC2(1.0f);//value
                    colorRectangleSlider.setXComponentIndex(1);//saturation
                    colorRectangleSlider.setYComponentIndex(2);//value
                    colorRectangleSlider.c1Property().bindBidirectional(c1);
                    colorRectangleSlider.c2Property().bindBidirectional(c2);
                }
                case ColorSpace.TYPE_HLS -> {
                    hueSlider.setComponentIndex(0);//hue
                    hueSlider.c0Property().bindBidirectional(c0);
                    hueSlider.setC1(0.5f);//lightness
                    hueSlider.setC2(1.0f);//saturation
                    colorRectangleSlider.setYComponentIndex(1);//saturation
                    colorRectangleSlider.setXComponentIndex(2);//lightness
                    colorRectangleSlider.c1Property().bindBidirectional(c1);
                    colorRectangleSlider.c2Property().bindBidirectional(c2);
                }
                case NamedColorSpace.TYPE_HSL -> {
                    hueSlider.setComponentIndex(0);//hue
                    hueSlider.c0Property().bindBidirectional(c0);
                    hueSlider.setC1(1.0f);//saturation
                    hueSlider.setC2(0.5f);//ligthness
                    hueSlider.setTickUnit(1f / 3600);

                    colorRectangleSlider.setXComponentIndex(1);//saturation
                    colorRectangleSlider.setYComponentIndex(2);//lightness
                    colorRectangleSlider.c1Property().bindBidirectional(c1);
                    colorRectangleSlider.c2Property().bindBidirectional(c2);
                }
                case NamedColorSpace.TYPE_LCH -> {
                    hueSlider.setComponentIndex(2);//hue
                    hueSlider.c2Property().bindBidirectional(c2);
                    hueSlider.setTickUnit(0.1f);

                    hueSlider.setC1(newv.getMaxValue(1) * 0.15f);//chroma
                    hueSlider.setC0(newv.getMaxValue(0) * 0.6f);//ligthness
                    colorRectangleSlider.setYComponentIndex(0);//lightness
                    colorRectangleSlider.setXComponentIndex(1);//chroma
                    colorRectangleSlider.c1Property().bindBidirectional(c1);
                    colorRectangleSlider.c0Property().bindBidirectional(c0);
                    colorRectangleSlider.c2Property().bind(c2);//hue
                }
                default -> {
                    hueSlider.setComponentIndex(0);//?
                    hueSlider.c0Property().unbindBidirectional(c0);
                    hueSlider.c0Property().unbindBidirectional(c1);
                    hueSlider.c0Property().unbindBidirectional(c2);
                    hueSlider.c0Property().bindBidirectional(c0);
                    hueSlider.setC1(1f);//?
                    hueSlider.setC2(1f);//?
                    colorRectangleSlider.setXComponentIndex(1);//?
                    colorRectangleSlider.setYComponentIndex(2);//?
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

