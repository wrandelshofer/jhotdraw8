/*
 * @(#)ColorSlidersHslMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorchooser;

import javafx.beans.InvalidationListener;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import org.jhotdraw8.color.CieLabColorSpace;
import org.jhotdraw8.color.DisplayP3ColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.OKHlsColorSpace;
import org.jhotdraw8.color.OKLchColorSpace;
import org.jhotdraw8.color.ParametricHlsColorSpace;
import org.jhotdraw8.color.ParametricLchColorSpace;
import org.jhotdraw8.color.ProPhotoRgbColorSpace;
import org.jhotdraw8.color.Rec2020ColorSpace;
import org.jhotdraw8.color.RgbBitConverters;
import org.jhotdraw8.color.SrgbColorSpace;
import org.jhotdraw8.fxcontrols.colorchooser.AlphaSlider;
import org.jhotdraw8.fxcontrols.colorchooser.ColorRectangleSlider;
import org.jhotdraw8.fxcontrols.colorchooser.ColorSlider;

import java.awt.color.ColorSpace;
import java.util.List;
import java.util.function.ToIntFunction;

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
    public void start(Stage primaryStage) {
        // start is called on the FX Application Thread,
        // so Thread.currentThread() is the FX application thread:
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> System.out.println("Handler caught exception: " + throwable.getMessage()));

        VBox vbox = new VBox();

        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(12))));
        vbox.setSpacing(8);

        FloatProperty c0 = new SimpleFloatProperty();
        FloatProperty c1 = new SimpleFloatProperty();
        FloatProperty c2 = new SimpleFloatProperty();
        FloatProperty alpha = new SimpleFloatProperty(1f);
        ObjectProperty<NamedColorSpace> targetColorSpace = new SimpleObjectProperty<>(
                new ParametricHlsColorSpace("HSL", new SrgbColorSpace()));
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
                new ParametricHlsColorSpace("HSL sRGB", new SrgbColorSpace()),
                new OKHlsColorSpace(),
                new OKLchColorSpace(),
                new ParametricLchColorSpace("LCH CieLab", new CieLabColorSpace())
        );
        ComboBox<NamedColorSpace> displayColorSpaceBox = createColorSpaceComboBox(
                new SrgbColorSpace(),
                new DisplayP3ColorSpace(),
                // new A98RgbColorSpace(), -> not a display
                new ProPhotoRgbColorSpace(),
                new Rec2020ColorSpace()
        );
        colorSpaceBox.valueProperty().bindBidirectional(targetColorSpace);
        displayColorSpaceBox.valueProperty().bindBidirectional(displayColorSpace);
        colorRectangleSlider.targetColorSpaceProperty().bind(targetColorSpace);
        hueSlider.targetColorSpaceProperty().bind(targetColorSpace);
        alphaSlider.targetColorSpaceProperty().bind(targetColorSpace);
        colorRectangleSlider.displayColorSpaceProperty().bind(displayColorSpace);
        hueSlider.displayColorSpaceProperty().bind(displayColorSpace);
        alphaSlider.displayColorSpaceProperty().bind(displayColorSpace);

        var bitDepthBox = createBitDepthComboBox();
        ObjectProperty<ToIntFunction<Integer>> displayBitDepth = bitDepthBox.valueProperty();
        colorRectangleSlider.rgbFilterProperty().bind(displayBitDepth);
        hueSlider.rgbFilterProperty().bind(displayBitDepth);

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
            displayColorSpace.get().fromRGB(targetColorSpace.get().toRGB(component, rgb), rgb);
            int argb = displayBitDepth.get().applyAsInt(RgbBitConverters.rgbFloatToArgb32(rgb, alpha.floatValue()));
            Color previewColorWithoutAlpha = Color.rgb((argb >>> 16) & 0xff, (argb >>> 8) & 0xff, (argb) & 0xff, 1);
            Color previewColorWithAlpha = Color.rgb((argb >>> 16) & 0xff, (argb >>> 8) & 0xff, (argb) & 0xff, alpha.floatValue());
            selectedColor.setBackground(new Background(
                    new BackgroundFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0.5, Color.WHITE),
                            new Stop(0.5, Color.BLACK)
                    ), null, null),
                    new BackgroundFill(previewColorWithAlpha, null, null),
                    new BackgroundFill(previewColorWithoutAlpha, null, new Insets(8))
            ));
            String hexStr = "00000000" + Integer.toHexString(argb);
            hexStr = hexStr.substring(hexStr.length() - 8);
            hexRgbLabel.setText("#" + hexStr.substring(2) + hexStr.substring(0, 2));
        };
        c0.addListener(updateSelectedColor);
        c1.addListener(updateSelectedColor);
        c2.addListener(updateSelectedColor);
        alpha.addListener(updateSelectedColor);
        displayColorSpace.addListener(updateSelectedColor);
        targetColorSpace.addListener(updateSelectedColor);
        displayBitDepth.addListener(updateSelectedColor);

        List<TextField> fields = createTextFields(c0, c1, c2);
        HBox componentsHBox = new HBox();
        componentsHBox.setAlignment(Pos.BASELINE_LEFT);
        componentsHBox.getChildren().addAll(selectedColor, hexRgbLabel, colorSpaceBox);
        componentsHBox.getChildren().addAll(fields);
        HBox displayHBox = new HBox();
        displayHBox.getChildren().addAll(new Label("Display:"), displayColorSpaceBox, new Label("Bit Depth:"), bitDepthBox);
        displayHBox.setAlignment(Pos.BASELINE_LEFT);
        vbox.getChildren().addAll(colorRectangleSlider, hueSlider, alphaSlider, componentsHBox,
                displayHBox);

        ChangeListener<NamedColorSpace> csListener = (o, oldv, newv) -> {
            hueSlider.c0Property().unbindBidirectional(c0);
            hueSlider.c0Property().unbindBidirectional(c1);
            hueSlider.c0Property().unbindBidirectional(c2);
            hueSlider.c0Property().unbind();
            hueSlider.c1Property().unbindBidirectional(c0);
            hueSlider.c1Property().unbindBidirectional(c1);
            hueSlider.c1Property().unbindBidirectional(c2);
            hueSlider.c1Property().unbind();
            hueSlider.c2Property().unbindBidirectional(c0);
            hueSlider.c2Property().unbindBidirectional(c1);
            hueSlider.c2Property().unbindBidirectional(c2);
            hueSlider.c2Property().unbind();
            colorRectangleSlider.c0Property().unbindBidirectional(c0);
            colorRectangleSlider.c0Property().unbindBidirectional(c1);
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
                    colorRectangleSlider.c0Property().bind(c0);
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
                    colorRectangleSlider.c0Property().bind(c0);
                    colorRectangleSlider.c1Property().bindBidirectional(c1);
                    colorRectangleSlider.c2Property().bindBidirectional(c2);
                }
                case NamedColorSpace.TYPE_LCH -> {
                    hueSlider.setComponentIndex(2);//hue
                    hueSlider.c2Property().bindBidirectional(c2);
                    hueSlider.setMinorTickUnit(0.1f);
                    if (newv.getName().contains("OKLCH")) {
                        hueSlider.setC1(newv.getMaxValue(1) * 0.25f);//chroma
                        hueSlider.setC0(newv.getMaxValue(0) * 0.6f);//ligthness

                    } else {
                        hueSlider.setC1(newv.getMaxValue(1) * 0.16f);//chroma
                        hueSlider.setC0(newv.getMaxValue(0) * 0.5f);//ligthness
                    }
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
        colorSpaceBox.setValue(colorSpaceBox.getItems().getFirst());
        csListener.changed(colorSpaceBox.valueProperty(), colorSpaceBox.getValue(), colorSpaceBox.getValue());

        Scene scene = new Scene(vbox, 300, 250);

        primaryStage.setTitle("Hello HSL sliders!");
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

