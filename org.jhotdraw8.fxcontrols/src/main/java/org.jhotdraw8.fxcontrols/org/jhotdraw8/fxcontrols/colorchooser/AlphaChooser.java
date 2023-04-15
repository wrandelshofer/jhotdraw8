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

public class AlphaChooser extends VBox {
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

    public AlphaChooser() {
        load();
    }

    private void load() {
        try {
            FXMLLoader loader = new FXMLLoader(AlphaChooser.getFxml());
            loader.setController(this);
            loader.setRoot(this);
            loader.setResources(ResourceBundle.getBundle("org.jhotdraw8.fxcontrols.colorchooser.Labels"));
            loader.load();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    private static URL getFxml() {
        String name = "AlphaChooser.fxml";
        return Objects.requireNonNull(AlphaChooser.class.getResource(name), name);
    }


    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="alphaPane"
    private BorderPane alphaPane; // Value injected by FXMLLoader

    private AlphaSlider alphaSlider;

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
        assert alphaPane != null : "fx:id=\"alphaPane\" was not injected: check your FXML file 'HlsChooser.fxml'.";

        alphaSlider = new AlphaSlider();
        alphaPane.setCenter(alphaSlider);

        Via<ColorChooserPaneModel> viaModel = ref(new Via<>(model));
        alphaSlider.c0Property().bind(viaModel.via(ColorChooserPaneModel::c0Property).get());
        alphaSlider.c1Property().bind(viaModel.via(ColorChooserPaneModel::c1Property).get());
        alphaSlider.c2Property().bind(viaModel.via(ColorChooserPaneModel::c2Property).get());
        alphaSlider.c3Property().bind(viaModel.via(ColorChooserPaneModel::c3Property).get());
        alphaSlider.alphaProperty().bindBidirectional(viaModel.via(ColorChooserPaneModel::alphaProperty).get());
        alphaSlider.sourceColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::sourceColorSpaceProperty).get());
        alphaSlider.targetColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        alphaSlider.displayColorSpaceProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        alphaSlider.rgbFilterProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get().map(Map.Entry::getValue));
    }

}
