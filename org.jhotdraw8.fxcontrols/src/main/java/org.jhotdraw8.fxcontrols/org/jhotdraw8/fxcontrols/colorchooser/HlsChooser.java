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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private BorderPane huePane; // Value injected by FXMLLoader

    @FXML // fx:id="colorRectPane"
    private BorderPane colorRectPane; // Value injected by FXMLLoader

    private ColorRectangleSlider colorRectSlider;
    private ColorSlider hueSlider;
    /**
     * Keep strong references to bindings.
     */
    private List<Object> refs = new ArrayList<>();

    private <T> T ref(T object) {
        refs.add(object);
        return object;
    }

    private final @NonNull ObjectProperty<ColorChooserPaneModel> model = new SimpleObjectProperty<>(this, "model");
    private ChangeListener<NamedColorSpace> targetColorSpaceListener;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert huePane != null : "fx:id=\"huePane\" was not injected: check your FXML file 'HlsChooser.fxml'.";
        assert colorRectPane != null : "fx:id=\"colorRectPane\" was not injected: check your FXML file 'HlsChooser.fxml'.";

        colorRectSlider = new ColorRectangleSlider();
        hueSlider = new ColorSlider();
        colorRectPane.setCenter(colorRectSlider);
        huePane.setCenter(hueSlider);

        Via<ColorChooserPaneModel> viaModel = ref(new Via<>(model));

        hueSlider.setThumbTranslateX(1);
        hueSlider.setOrientation(Orientation.VERTICAL);
        hueSlider.c0Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c0Property).get());
        hueSlider.c1Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c1Property).get());
        hueSlider.c2Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c2Property).get());
        hueSlider.componentIndexProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceHueIndexProperty).get());
        hueSlider.sourceColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get());
        hueSlider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        hueSlider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        hueSlider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));

        colorRectSlider.c0Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c0Property).get());
        colorRectSlider.c1Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c1Property).get());
        colorRectSlider.c2Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c2Property).get());
        colorRectSlider.c3Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c3Property).get());
        colorRectSlider.sourceColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get());
        colorRectSlider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        colorRectSlider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        colorRectSlider.xComponentIndexProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceSaturationChromaIndexProperty).get());
        colorRectSlider.yComponentIndexProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceLightnessValueIndexProperty).get());
        colorRectSlider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));

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
