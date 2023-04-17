/**
 * Sample Skeleton for 'ColorChooserPane.fxml' Controller Class
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * HSL Chooser.
 * <p>
 * Useful for the following color spaces:
 * <dl>
 *     <dt>HSL</dt><dd>Hue, Saturation, Lightness</dd>
 *     <dt>HSB</dt><dd>Hue, Saturation, Brightness</dd>
 *     <dt></dt><dd></dd>
 * </dl>
 * <pre>
 *           saturation→
 *           +--------------+ +---+
 *           |              | |   |
 *         ↑ |              | |   | ↑
 * lightness |              | |   | hue
 *           +--------------+ +---+
 * </pre>
 */
public class HlsChooser extends HBox {


    public HlsChooser() {
        load();
    }

    private void load() {
        try {
            FXMLLoader loader = new FXMLLoader(HlsChooser.getFxml());
            loader.setController(this);
            loader.setRoot(this);
            loader.setResources(ResourceBundle.getBundle("org.jhotdraw8.fxcontrols.colorchooser.Labels"));
            loader.load();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    private static URL getFxml() {
        String name = "HlsChooser.fxml";
        return Objects.requireNonNull(HlsChooser.class.getResource(name), name);
    }


    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="huePane"
    private StackPane huePane; // Value injected by FXMLLoader

    @FXML // fx:id="colorRectPane"
    private StackPane colorRectPane; // Value injected by FXMLLoader

    private ColorRectangleSlider colorRectSlider;
    private ColorSlider hueSlider;

    private final @NonNull ObjectProperty<ColorChooserPaneModel> model = new SimpleObjectProperty<>(this, "model");
    private ChangeListener<NamedColorSpace> targetColorSpaceListener;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert huePane != null : "fx:id=\"huePane\" was not injected: check your FXML file 'HlsChooser.fxml'.";
        assert colorRectPane != null : "fx:id=\"colorRectPane\" was not injected: check your FXML file 'HlsChooser.fxml'.";

        Background checkerboardBackground = new Background(new BackgroundFill(CheckerboardFactory.createCheckerboardPattern(4, 0xffffffff, 0xffaaaaaa), null, null));
        colorRectPane.setBackground(checkerboardBackground);
        huePane.setBackground(checkerboardBackground);
        colorRectSlider = new ColorRectangleSlider();
        hueSlider = new ColorSlider();
        colorRectPane.getChildren().add(colorRectSlider);
        huePane.getChildren().add(hueSlider);


        hueSlider.setThumbTranslateX(1);
        hueSlider.setOrientation(Orientation.VERTICAL);
        hueSlider.c0Property().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceProperty).map((NamedColorSpace v) ->
                0.5f * (v.getMaxValue(0) - v.getMinValue(0)) + v.getMinValue(0)
        ));
        hueSlider.c1Property().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceProperty).map((NamedColorSpace v) ->
                0.5f * (v.getMaxValue(1) - v.getMinValue(1)) + v.getMinValue(1)
        ));
        hueSlider.c2Property().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceProperty).map((NamedColorSpace v) ->
                0.5f * (v.getMaxValue(2) - v.getMinValue(2)) + v.getMinValue(2)
        ));
        hueSlider.setMinorTickUnit(0.1);
        hueSlider.setMajorTickUnit(1);

        hueSlider.componentIndexProperty().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceHueIndexProperty));
        hueSlider.sourceColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceProperty));
        hueSlider.targetColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSpaceProperty));
        hueSlider.displayColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::displayColorSpaceProperty));
        hueSlider.rgbFilterProperty().bind(model.flatMap(ColorChooserPaneModel::displayBitDepthProperty).map(Map.Entry::getValue));
        colorRectSlider.c0Property().bind(model.flatMap(ColorChooserPaneModel::c0Property));
        colorRectSlider.c1Property().bind(model.flatMap(ColorChooserPaneModel::c1Property));
        colorRectSlider.c2Property().bind(model.flatMap(ColorChooserPaneModel::c2Property));
        colorRectSlider.c3Property().bind(model.flatMap(ColorChooserPaneModel::c3Property));
        colorRectSlider.alphaProperty().bind(model.flatMap(ColorChooserPaneModel::alphaProperty));
        colorRectSlider.sourceColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceProperty));
        colorRectSlider.targetColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSpaceProperty));
        colorRectSlider.displayColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::displayColorSpaceProperty));
        colorRectSlider.xComponentIndexProperty().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceSaturationChromaIndexProperty));
        colorRectSlider.yComponentIndexProperty().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceLightnessValueIndexProperty));
        colorRectSlider.rgbFilterProperty().bind(model.flatMap(ColorChooserPaneModel::displayBitDepthProperty).map(Map.Entry::getValue));

        hueSlider.valueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::hueProperty).get());
        colorRectSlider.xValueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::chromaProperty).get());
        colorRectSlider.yValueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::lightnessProperty).get());

    }


    public ColorChooserPaneModel getModel() {
        return model.get();
    }

    public @NonNull ObjectProperty<ColorChooserPaneModel> modelProperty() {
        return model;
    }

    public void setModel(ColorChooserPaneModel model) {
        this.model.set(model);
    }
}
