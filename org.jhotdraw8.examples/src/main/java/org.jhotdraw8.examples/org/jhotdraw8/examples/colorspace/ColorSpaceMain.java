package org.jhotdraw8.examples.colorspace;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.color.CIELABColorSpace;
import org.jhotdraw8.color.CIELCHabColorSpace;
import org.jhotdraw8.color.HPLuvColorSpace;
import org.jhotdraw8.color.HSBColorSpace;
import org.jhotdraw8.color.HSLColorSpace;
import org.jhotdraw8.color.HSLLinearColorSpace;
import org.jhotdraw8.color.HSLPhysiologicColorSpace;
import org.jhotdraw8.color.HSLuvColorSpace;
import org.jhotdraw8.color.HSVColorSpace;
import org.jhotdraw8.color.HSVLinearColorSpace;
import org.jhotdraw8.color.HSVPhysiologicColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.NamedColorSpaceAdapter;
import org.jhotdraw8.color.RgbLinearColorSpace;

import java.awt.color.ColorSpace;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ColorSpaceMain extends Application {
    public ColorSpaceMain() {
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("ColorSpace Demo");

        ComboBox<ColorSpace> colorSpace1ComboBox = createColorSpaceBox();
        ComboBox<ColorSpace> colorSpace2ComboBox = createColorSpaceBox();
        ComboBox<ColorSpace> visualisationColorSpace = createColorSpaceBox();
        colorSpace1ComboBox.setValue(colorSpace1ComboBox.getItems().get(0));
        colorSpace2ComboBox.setValue(colorSpace1ComboBox.getItems().get(1));
        visualisationColorSpace.setValue(new HSVColorSpace());

        ColorPicker fromPicker = new ColorPicker();
        ColorPicker toPicker = new ColorPicker();
        fromPicker.setValue(Color.BLACK);
        toPicker.setValue(Color.WHITE);
        HBox hbox = new HBox();
        Label from1Label = new Label();
        Label to1Label = new Label();
        Label from2Label = new Label();
        Label to2Label = new Label();
        VBox vbox1 = new VBox(fromPicker, (Node) from1Label, (Node) from2Label);
        VBox vbox2 = new VBox(toPicker, to1Label, to2Label);
        hbox.getChildren().addAll(vbox1, vbox2);

        from1Label.textProperty().bind(
                createColorLabelBinding(colorSpace1ComboBox, fromPicker));
        from2Label.textProperty().bind(
                createColorLabelBinding(colorSpace2ComboBox, fromPicker));
        to1Label.textProperty().bind(
                createColorLabelBinding(colorSpace1ComboBox, toPicker));
        to2Label.textProperty().bind(
                createColorLabelBinding(colorSpace2ComboBox, toPicker));

        VBox componentsVBox = new VBox();

        colorSpace1ComboBox.valueProperty().addListener((o, oldv, newv) ->
                updateComponentsVBox(componentsVBox, newv));
        ColorInterpolationStrip interpolation1Strip = new ColorInterpolationStrip();
        interpolation1Strip.colorSpaceProperty().bind(colorSpace1ComboBox.valueProperty());
        interpolation1Strip.fromColorProperty().bind(fromPicker.valueProperty());
        interpolation1Strip.toColorProperty().bind(toPicker.valueProperty());

        ColorInterpolationStrip interpolation2Strip = new ColorInterpolationStrip();
        interpolation2Strip.colorSpaceProperty().bind(colorSpace2ComboBox.valueProperty());
        interpolation2Strip.fromColorProperty().bind(fromPicker.valueProperty());
        interpolation2Strip.toColorProperty().bind(toPicker.valueProperty());
        interpolation1Strip.setPrefWidth(256);
        interpolation2Strip.setPrefWidth(256);
        interpolation1Strip.setPrefHeight(30);
        interpolation2Strip.setPrefHeight(30);

        ColorRect colorRect = new ColorRect();
        colorRect.setPrefWidth(256);
        colorRect.setPrefHeight(256);
        colorRect.colorSpaceProperty().bind(colorSpace2ComboBox.valueProperty());
        colorRect.baseColorProperty().bind(Bindings.createObjectBinding(
                () -> {
                    ColorSpace value = colorSpace2ComboBox.getValue();
                    if (value == null) {
                        return new float[]{1, 1, 1};
                    }
                    float[] floats = new float[value.getNumComponents()];
                    for (int i = 0; i < floats.length; i++) {
                        floats[i] = 0.5f * (value.getMaxValue(i) - value.getMinValue(i)) + value.getMinValue(0);
                    }
                    return floats;
                },
                colorSpace2ComboBox.valueProperty()
        ));

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hbox,
                new HBox(
                        colorSpace1ComboBox,
                        interpolation1Strip),
                new HBox(
                        colorSpace2ComboBox,
                        interpolation2Strip),
                colorRect);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.sizeToScene();

        stage.show();
    }

    @NonNull
    private static ObjectBinding<String> createColorLabelBinding(ComboBox<ColorSpace> colorSpace1ComboBox, ColorPicker fromPicker) {
        ObjectProperty<ColorSpace> colorSpaceProperty = colorSpace1ComboBox.valueProperty();
        return Bindings.createObjectBinding(new Callable<String>() {
                                                @Override
                                                public String call() throws Exception {
                                                    ColorSpace value = colorSpace1ComboBox.getValue();
                                                    return
                                                            (colorSpaceProperty.get() instanceof NamedColorSpace n ? n.getName() : colorSpaceProperty.get().getClass().getSimpleName())
                                                                    + ": "
                                                                    + (value == null ? "null" : Arrays.toString(value.fromRGB(
                                                                    new float[]{
                                                                            (float) fromPicker.getValue().getRed(),
                                                                            (float) fromPicker.getValue().getGreen(),
                                                                            (float) fromPicker.getValue().getBlue()
                                                                    })));
                                                }
                                            },
                fromPicker.valueProperty(),
                colorSpaceProperty);
    }

    private void updateComponentsVBox(VBox vbox, ColorSpace cs) {
        vbox.getChildren().clear();
        for (int c = 0, n = cs.getNumComponents(); c < n; c++) {
            ColorStrip strip = new ColorStrip();
            strip.setColorSpace(cs);
            strip.setComponent(c);
            strip.setPrefHeight(30);
            strip.setPrefWidth(256);
            vbox.getChildren().add(strip);
        }
    }

    @Nullable
    private static ComboBox<ColorSpace> createColorSpaceBox() {
        var colorSpaceBox = new ComboBox<ColorSpace>();
        ObservableList<ColorSpace> list = FXCollections.observableArrayList();
        list.addAll(
                new NamedColorSpaceAdapter("sRGB", ColorSpace.getInstance(ColorSpace.CS_sRGB)),
                new NamedColorSpaceAdapter("RGB Linear (Java)", ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)),
                new RgbLinearColorSpace(),
                new HPLuvColorSpace(),
                new HSLuvColorSpace(),
                new CIELABColorSpace(),
                new CIELCHabColorSpace(),
                new HSBColorSpace(),
                new HSLColorSpace(),
                new HSLLinearColorSpace(),
                new HSLPhysiologicColorSpace(),
                new HSVColorSpace(),
                new HSVLinearColorSpace(),
                new HSVPhysiologicColorSpace()
        );
        colorSpaceBox.setItems(list);
        colorSpaceBox.setValue(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB));
        colorSpaceBox.setConverter(new StringConverter<ColorSpace>() {
            @Override
            public String toString(ColorSpace object) {
                if (object instanceof NamedColorSpace ncs) {
                    return ncs.getName();
                }
                return object == null ? "null" : object.getClass().getSimpleName();
            }

            @Override
            public ColorSpace fromString(String string) {
                return null;
            }
        });
        return colorSpaceBox;
    }
}
