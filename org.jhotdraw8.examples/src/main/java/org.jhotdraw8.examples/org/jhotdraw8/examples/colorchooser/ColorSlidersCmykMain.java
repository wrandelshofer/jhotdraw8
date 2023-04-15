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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.CmykNominalColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.fxcontrols.colorchooser.ColorSlider;

import java.util.List;

/**
 * Show sliders for RGB color spaces.
 * <pre>
 * +-----------------+
 * |   cyan →        |
 * +-----------------+
 * +-----------------+
 * |   magenta →     |
 * +-----------------+
 * +-----------------+
 * |   yellow →      |
 * +-----------------+
 * +-----------------+
 * |   black →       |
 * +-----------------+
 * </pre>
 */
public class ColorSlidersCmykMain extends AbstractColorSlidersMain {

    @Override
    public void start(@NonNull Stage primaryStage) {
        VBox vbox = new VBox();

        vbox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(12))));
        vbox.setSpacing(8);

        ColorSlider sliderC = new ColorSlider();
        ColorSlider sliderM = new ColorSlider();
        ColorSlider sliderY = new ColorSlider();
        ColorSlider sliderK = new ColorSlider();

        sliderC.setComponentIndex(0);
        sliderM.setComponentIndex(1);
        sliderY.setComponentIndex(2);
        sliderK.setComponentIndex(3);

        FloatProperty cyan = new SimpleFloatProperty();
        FloatProperty magenta = new SimpleFloatProperty();
        FloatProperty yellow = new SimpleFloatProperty();
        FloatProperty black = new SimpleFloatProperty();
        sliderC.c0Property().bindBidirectional(cyan);
        sliderC.c1Property().bind(magenta);
        sliderC.c2Property().bind(yellow);
        sliderC.c3Property().bind(black);

        sliderM.c0Property().bind(cyan);
        sliderM.c1Property().bindBidirectional(magenta);
        sliderM.c2Property().bind(yellow);
        sliderM.c3Property().bind(black);

        sliderY.c0Property().bind(cyan);
        sliderY.c1Property().bind(magenta);
        sliderY.c2Property().bindBidirectional(yellow);
        sliderY.c3Property().bind(black);

        sliderK.c0Property().bind(cyan);
        sliderK.c1Property().bind(magenta);
        sliderK.c2Property().bind(yellow);
        sliderK.c3Property().bindBidirectional(black);

        ComboBox<NamedColorSpace> colorSpaceBox = createColorSpaceComboBox(

                new CmykNominalColorSpace()//,
                //new NamedColorSpaceAdapter("CMYK generic",CMYKGenericColorSpace.getInstance())
        );
        sliderC.targetColorSpaceProperty().bind(colorSpaceBox.valueProperty());
        sliderM.targetColorSpaceProperty().bind(colorSpaceBox.valueProperty());
        sliderY.targetColorSpaceProperty().bind(colorSpaceBox.valueProperty());
        sliderK.targetColorSpaceProperty().bind(colorSpaceBox.valueProperty());

        var bitDepthBox = createBitDepthComboBox();
        sliderC.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        sliderM.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        sliderY.rgbFilterProperty().bind(bitDepthBox.valueProperty());
        sliderK.rgbFilterProperty().bind(bitDepthBox.valueProperty());


        List<TextField> fields = createTextFields(cyan, magenta, yellow, black);
        HBox componentsHBox = new HBox();
        componentsHBox.getChildren().addAll(colorSpaceBox);
        componentsHBox.getChildren().addAll(fields);
        vbox.getChildren().addAll(sliderC, sliderM, sliderY, sliderK, componentsHBox, bitDepthBox);


        Scene scene = new Scene(vbox, 300, 250);

        primaryStage.setTitle("Hello CMYK sliders!");
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

