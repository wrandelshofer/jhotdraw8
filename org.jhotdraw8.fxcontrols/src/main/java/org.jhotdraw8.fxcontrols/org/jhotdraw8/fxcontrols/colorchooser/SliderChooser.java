/**
 * Sample Skeleton for 'ColorChooserPane.fxml' Controller Class
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class SliderChooser extends VBox {
    private final @NonNull ObjectProperty<ColorChooserPaneModel> model = new SimpleObjectProperty<>(this, "model");

    public ColorChooserPaneModel getModel() {
        return model.get();
    }

    public @NonNull ObjectProperty<ColorChooserPaneModel> modelProperty() {
        return model;
    }

    public void setModel(ColorChooserPaneModel model) {
        this.model.set(model);
    }

    public SliderChooser() {
        load();
    }

    private void load() {
        try {
            FXMLLoader loader = new FXMLLoader(SliderChooser.getFxml());
            loader.setController(this);
            loader.setRoot(this);
            loader.setResources(ResourceBundle.getBundle("org.jhotdraw8.fxcontrols.colorchooser.Labels"));
            loader.load();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    private static URL getFxml() {
        String name = "SliderChooser.fxml";
        return Objects.requireNonNull(SliderChooser.class.getResource(name), name);
    }


    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="c0Pane"
    private BorderPane c0Pane; // Value injected by FXMLLoader

    private ColorSlider c0Slider;

    @FXML // fx:id="c1Pane"
    private BorderPane c1Pane; // Value injected by FXMLLoader

    private ColorSlider c1Slider;

    @FXML // fx:id="c2Pane"
    private BorderPane c2Pane; // Value injected by FXMLLoader

    private ColorSlider c2Slider;

    /**
     * Keep strong references to bindings.
     */
    private List<Object> refs = new ArrayList<>();

    private <T> T ref(T binding) {
        refs.add(binding);
        return binding;
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert c0Pane != null : "fx:id=\"c0Pane\" was not injected: check your FXML file 'HlsChooser.fxml'.";
        assert c1Pane != null : "fx:id=\"c1Pane\" was not injected: check your FXML file 'HlsChooser.fxml'.";
        assert c2Pane != null : "fx:id=\"c2Pane\" was not injected: check your FXML file 'HlsChooser.fxml'.";

        c0Slider = new ColorSlider();
        c0Pane.setCenter(c0Slider);
        c1Slider = new ColorSlider();
        c1Pane.setCenter(c1Slider);
        c2Slider = new ColorSlider();
        c2Pane.setCenter(c2Slider);

        c0Slider.setComponentIndex(0);
        c1Slider.setComponentIndex(1);
        c2Slider.setComponentIndex(2);

        Via<ColorChooserPaneModel> viaModel = ref(new Via<>(model));
        c0Slider.c0Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c0Property).get());
        c0Slider.c1Property().bind(viaModel.via(ColorChooserPaneModel::c1Property).get());
        c0Slider.c2Property().bind(viaModel.via(ColorChooserPaneModel::c2Property).get());
        c0Slider.c3Property().bind(viaModel.via(ColorChooserPaneModel::c3Property).get());
        c0Slider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        c0Slider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        c0Slider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));
        c1Slider.c0Property().bind(viaModel.via(ColorChooserPaneModel::c0Property).get());
        c1Slider.c1Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c1Property).get());
        c1Slider.c2Property().bind(viaModel.via(ColorChooserPaneModel::c2Property).get());
        c1Slider.c3Property().bind(viaModel.via(ColorChooserPaneModel::c3Property).get());
        c1Slider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        c1Slider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        c1Slider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));
        c2Slider.c0Property().bind(viaModel.via(ColorChooserPaneModel::c0Property).get());
        c2Slider.c1Property().bind(viaModel.via(ColorChooserPaneModel::c1Property).get());
        c2Slider.c2Property().bindBidirectional(viaModel.via(ColorChooserPaneModel::c2Property).get());
        c2Slider.c3Property().bind(viaModel.via(ColorChooserPaneModel::c3Property).get());
        c2Slider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        c2Slider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        c2Slider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));
    }

}
