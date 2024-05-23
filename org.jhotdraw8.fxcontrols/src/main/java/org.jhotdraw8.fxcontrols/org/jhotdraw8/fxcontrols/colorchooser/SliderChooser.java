/*
 * @(#)SliderChooser.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

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
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Shows 3 horizontal sliders:
 * <pre>
 *     +---------------------+
 *     |                     |
 *     +---------------------+
 *     +---------------------+
 *     |                     |
 *     +---------------------+
 *     +---------------------+
 *     |                     |
 *     +---------------------+
 * </pre>
 */
public class SliderChooser extends VBox {
    @SuppressWarnings("this-escape")
    private final ObjectProperty<ColorChooserPaneModel> model = new SimpleObjectProperty<>(this, "model");

    public ColorChooserPaneModel getModel() {
        return model.get();
    }

    public ObjectProperty<ColorChooserPaneModel> modelProperty() {
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


        c0Slider.c1Property().bind(model.flatMap(ColorChooserPaneModel::c1Property));
        c0Slider.c2Property().bind(model.flatMap(ColorChooserPaneModel::c2Property));
        c0Slider.c3Property().bind(model.flatMap(ColorChooserPaneModel::c3Property));
        c0Slider.targetColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSpaceProperty));
        c0Slider.displayColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::displayColorSpaceProperty));
        c0Slider.rgbFilterProperty().bind(model.flatMap(ColorChooserPaneModel::displayBitDepthProperty).map(Map.Entry::getValue));
        c1Slider.c0Property().bind(model.flatMap(ColorChooserPaneModel::c0Property));
        c1Slider.c2Property().bind(model.flatMap(ColorChooserPaneModel::c2Property));
        c1Slider.c3Property().bind(model.flatMap(ColorChooserPaneModel::c3Property));
        c1Slider.targetColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSpaceProperty));
        c1Slider.displayColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::displayColorSpaceProperty));
        c1Slider.rgbFilterProperty().bind(model.flatMap(ColorChooserPaneModel::displayBitDepthProperty).map(Map.Entry::getValue));
        c2Slider.c0Property().bind(model.flatMap(ColorChooserPaneModel::c0Property));
        c2Slider.c1Property().bind(model.flatMap(ColorChooserPaneModel::c1Property));
        c2Slider.c3Property().bind(model.flatMap(ColorChooserPaneModel::c3Property));
        c2Slider.targetColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSpaceProperty));
        c2Slider.displayColorSpaceProperty().bind(model.flatMap(ColorChooserPaneModel::displayColorSpaceProperty));
        c2Slider.rgbFilterProperty().bind(model.flatMap(ColorChooserPaneModel::displayBitDepthProperty).map(Map.Entry::getValue));

        c0Slider.c0Property().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::c0Property).get());
        c1Slider.c1Property().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::c1Property).get());
        c2Slider.c2Property().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::c2Property).get());
    }

}
