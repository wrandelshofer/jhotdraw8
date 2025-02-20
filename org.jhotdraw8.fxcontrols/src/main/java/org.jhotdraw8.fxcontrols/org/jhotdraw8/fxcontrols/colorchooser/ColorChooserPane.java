/*
 * @(#)ColorChooserPane.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Sample Skeleton for 'ColorChooserPane.fxml' Controller Class
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import org.jhotdraw8.color.NamedColor;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.SrgbColorSpace;
import org.jhotdraw8.css.converter.ColorCssConverter;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.NamedCssColor;
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.ToIntFunction;

public class ColorChooserPane extends VBox {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="slidersPane"
    private VBox chooserPane; // Value injected by FXMLLoader

    @FXML // fx:id="displayColorField"
    private Label displayColorField; // Value injected by FXMLLoader
    @FXML // fx:id="sourceColorField"
    private Label sourceColorField; // Value injected by FXMLLoader

    @FXML // fx:id="displayColorSpaceCombo"
    private ComboBox<NamedColorSpace> displayColorSpaceCombo; // Value injected by FXMLLoader

    @FXML // fx:id="displayDepthCombo"
    private ComboBox<Map.Entry<String, ToIntFunction<Integer>>> displayDepthCombo; // Value injected by FXMLLoader

    @FXML // fx:id="targetColorRegion"
    private Region targetColorRegion; // Value injected by FXMLLoader

    @FXML // fx:id="targetColorField"
    private TextField targetColorField; // Value injected by FXMLLoader

    @FXML // fx:id="targetColorSpaceCombo"
    private ComboBox<NamedColorSpace> targetColorSpaceCombo; // Value injected by FXMLLoader
    @FXML // fx:id="targetSyntaxCombo"
    private ComboBox<ColorChooserPaneModel.ColorSyntax> targetSyntaxCombo; // Value injected by FXMLLoader
    @FXML // fx:id="chooserCombo"
    private ComboBox<ColorChooserPaneModel.ChooserType> chooserCombo; // Value injected by FXMLLoader

    @FXML // fx:id="targetLabel"
    private Label targetLabel; // Value injected by FXMLLoader

    /**
     * The current value of this color chooser pane.
     */
    @SuppressWarnings("this-escape")
    private final ObjectProperty<NamedColor> value = new SimpleObjectProperty<>(this, "value");
    @SuppressWarnings("this-escape")
    private final ObjectProperty<ColorChooserPaneModel> model = new SimpleObjectProperty<>(this, "model", new ColorChooserPaneModel());


    public ColorChooserPane() {
        load();
    }

    private void load() {
        try {
            FXMLLoader loader = new FXMLLoader(ColorChooserPane.getFxml());
            loader.setController(this);
            loader.setRoot(this);
            loader.setResources(ResourceBundle.getBundle("org.jhotdraw8.fxcontrols.colorchooser.Labels"));
            loader.load();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    private static URL getFxml() {
        String name = "ColorChooserPane.fxml";
        return Objects.requireNonNull(ColorChooserPane.class.getResource(name), name);
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert targetSyntaxCombo != null : "fx:id=\"targetSyntaxCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert chooserCombo != null : "fx:id=\"chooserCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert chooserPane != null : "fx:id=\"alphaSliderPane\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert displayColorField != null : "fx:id=\"displayColorField\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert displayColorSpaceCombo != null : "fx:id=\"displayColorSpaceCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert displayDepthCombo != null : "fx:id=\"displayDepthCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetColorRegion != null : "fx:id=\"targetColorRegion\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetColorField != null : "fx:id=\"targetColorField\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert sourceColorField != null : "fx:id=\"sourceColorField\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetColorSpaceCombo != null : "fx:id=\"targetColorSpaceCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetLabel != null : "fx:id=\"targetLabel\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";

        initSubPane();

        targetColorSpaceCombo.itemsProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSpacesProperty));
        displayColorSpaceCombo.itemsProperty().bind(model.flatMap(ColorChooserPaneModel::displayColorSpacesProperty));
        displayDepthCombo.itemsProperty().bind(model.flatMap(ColorChooserPaneModel::displayBitDepthsProperty));
        targetSyntaxCombo.itemsProperty().bind(model.flatMap(ColorChooserPaneModel::targetColorSyntaxesProperty));
        chooserCombo.itemsProperty().bind(model.flatMap(ColorChooserPaneModel::colorChoosersProperty));

        targetColorSpaceCombo.valueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        displayColorSpaceCombo.valueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        displayDepthCombo.valueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::displayBitDepthProperty).get());
        targetSyntaxCombo.valueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::targetColorSyntaxProperty).get());
        chooserCombo.valueProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::chooserTypeProperty).get());
        targetColorField.textProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::targetColorFieldProperty).get());
        sourceColorField.textProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::sourceColorFieldProperty).get());
        displayColorField.textProperty().bindBidirectional(new Via<>(model).via(ColorChooserPaneModel::displayColorFieldProperty).get());
        model.flatMap(ColorChooserPaneModel::previewColorProperty).addListener((ChangeListener<? super Color>) this::updatePreviewColor);

        targetColorField.setOnAction(this::onTargetColorField);
    }

    private void onTargetColorField(ActionEvent actionEvent) {
        String text = targetColorField.getText();
        try {
            CssColor color = new ColorCssConverter().parse(new StreamCssTokenizer(text), null);
            if (color == null) {
                System.err.println("Can not parse color " + text);
                return;
            }
            Color c = color.getColor();
            model.get().setC0((float) c.getRed());
            model.get().setC1((float) c.getGreen());
            model.get().setC2((float) c.getBlue());
            /*
                    new NamedColor(
                    (float)c.getRed(),(float)c.getGreen(),(float)c.getBlue(),0f,(float)c.getOpacity(),
                    new SrgbColorSpace(),text));*/
        } catch (ParseException | IOException e) {
            System.err.println("Can not parse color " + text);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private void initSubPane() {
        HlsChooser hlsChooser = new HlsChooser();
        SliderChooser sliderChooser = new SliderChooser();
        AlphaChooser alphaChooser = new AlphaChooser();

        hlsChooser.modelProperty().bind(model);
        sliderChooser.modelProperty().bind(model);
        alphaChooser.modelProperty().bind(model);

        chooserPane.getChildren().setAll(hlsChooser, alphaChooser);
        //choosersPane.getChildren().setAll(sliderChooser,alphaChooser);

        VBox.setVgrow(hlsChooser, Priority.ALWAYS);
        VBox.setVgrow(sliderChooser, Priority.ALWAYS);
    }

    /**
     * Target color region shows a fat white rectangle with the target color overlaid over a white and black
     * pattern.
     *
     * <pre>
     *  +----------+
     *  |  +----+  |
     *  |  |   /|  |
     *  |  |/   |  |
     *  |  +----+  |
     *  +----------+
     * </pre>
     */
    private void updatePreviewColor(Observable o, Color oldv, Color newv) {
        Color previewColor = newv == null ? Color.TRANSPARENT : newv;
        targetColorRegion.setBackground(new Background(
                new BackgroundFill(Color.WHITE,
                        null, null),
                new BackgroundFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.5, Color.WHITE),
                        new Stop(0.5, Color.BLACK)
                ), null, new Insets(4)),
                new BackgroundFill(previewColor, null, new Insets(4))
        ));
    }

    public ColorChooserPaneModel getModel() {
        return model.get();
    }

    public ObjectProperty<ColorChooserPaneModel> modelProperty() {
        return model;
    }

    public void setModel(ColorChooserPaneModel model) {
        this.model.set(model);
    }
}
