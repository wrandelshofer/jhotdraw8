/**
 * Sample Skeleton for 'ColorChooserPane.fxml' Controller Class
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.jhotdraw8.fxcontrols.colorchooser.CheckerboardFactory.createCheckerboardPattern;

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

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert alphaPane != null : "fx:id=\"alphaPane\" was not injected: check your FXML file 'HlsChooser.fxml'.";

        alphaSlider = new AlphaSlider();
        alphaPane.setCenter(alphaSlider);
        Background checkerboardBackground = new Background(new BackgroundFill(createCheckerboardPattern(4, 0xffffffff, 0xffaaaaaa), null, null));
        alphaSlider.setBackground(checkerboardBackground);

        alphaSlider.c0Property().bind(model.flatMap(ColorChooserPaneModel::c0Property));
        alphaSlider.c1Property().bind(model.flatMap(ColorChooserPaneModel::c1Property));
        alphaSlider.c2Property().bind(model.flatMap(ColorChooserPaneModel::c2Property));
        alphaSlider.c3Property().bind(model.flatMap(ColorChooserPaneModel::c3Property));
        alphaSlider.sourceColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::sourceColorSpaceProperty));
        alphaSlider.targetColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSpaceProperty));
        alphaSlider.displayColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::displayColorSpaceProperty));

        alphaSlider.alphaProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::alphaProperty).get());
        alphaSlider.rgbFilterProperty().bind(model.flatMap(ColorChooserPaneModel::displayBitDepthProperty).map(Map.Entry::getValue));
    }
}