/**
 * Sample Skeleton for 'ColorChooserPane.fxml' Controller Class
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.NamedColor;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.fxbase.binding.Via;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private VBox choosersPane; // Value injected by FXMLLoader

    @FXML // fx:id="displayColorField"
    private Label displayColorField; // Value injected by FXMLLoader

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
    @FXML // fx:id="colorChooserCombo"
    private ComboBox<ColorChooserPaneModel.ChooserType> colorChooserCombo; // Value injected by FXMLLoader

    @FXML // fx:id="targetLabel"
    private Label targetLabel; // Value injected by FXMLLoader

    /**
     * The current value of this color chooser pane.
     */
    private final @NonNull ObjectProperty<NamedColor> value = new SimpleObjectProperty<>(this, "value");
    private final @NonNull ObjectProperty<ColorChooserPaneModel> model = new SimpleObjectProperty<>(this, "model", new ColorChooserPaneModel());


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

    /**
     * Keep strong references to bindings.
     */
    private List<Object> refs = new ArrayList<>();

    private <T> T ref(T binding) {
        refs.add(binding);
        return binding;
    }

    private static URL getFxml() {
        String name = "ColorChooserPane.fxml";
        return Objects.requireNonNull(ColorChooserPane.class.getResource(name), name);
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert targetSyntaxCombo != null : "fx:id=\"targetSyntaxCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert colorChooserCombo != null : "fx:id=\"colorChooserCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert choosersPane != null : "fx:id=\"alphaSliderPane\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert displayColorField != null : "fx:id=\"displayColorField\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert displayColorSpaceCombo != null : "fx:id=\"displayColorSpaceCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert displayDepthCombo != null : "fx:id=\"displayDepthCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetColorRegion != null : "fx:id=\"targetColorRegion\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetColorField != null : "fx:id=\"targetColorField\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetColorSpaceCombo != null : "fx:id=\"targetColorSpaceCombo\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";
        assert targetLabel != null : "fx:id=\"targetLabel\" was not injected: check your FXML file 'ColorChooserPane.fxml'.";

        initSubPane();

        Via<ColorChooserPaneModel> viaModel = ref(new Via<>(model));
        targetColorSpaceCombo.itemsProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSpacesProperty).get());
        targetColorSpaceCombo.valueProperty().bindBidirectional(viaModel.via(ColorChooserPaneModel::targetColorSpaceProperty).get());
        displayColorSpaceCombo.itemsProperty().bind(viaModel.via(ColorChooserPaneModel::displayColorSpacesProperty).get());
        displayColorSpaceCombo.valueProperty().bindBidirectional(viaModel.via(ColorChooserPaneModel::displayColorSpaceProperty).get());
        displayDepthCombo.itemsProperty().bind(viaModel.via(ColorChooserPaneModel::displayBitDepthsProperty).get());
        displayDepthCombo.valueProperty().bindBidirectional(viaModel.via(ColorChooserPaneModel::displayBitDepthProperty).get());
        targetSyntaxCombo.itemsProperty().bind(viaModel.via(ColorChooserPaneModel::targetColorSyntaxesProperty).get());
        targetSyntaxCombo.valueProperty().bindBidirectional(viaModel.via(ColorChooserPaneModel::targetColorSyntaxProperty).get());
        colorChooserCombo.itemsProperty().bind(viaModel.via(ColorChooserPaneModel::colorChoosersProperty).get());
        colorChooserCombo.valueProperty().bindBidirectional(viaModel.via(ColorChooserPaneModel::chooserTypeProperty).get());
        viaModel.via(ColorChooserPaneModel::previewColorProperty).get().addListener(this.<ChangeListener<? super Color>>ref(this::updatePreviewColor));
    }

    private void initSubPane() {
        HlsChooser hlsChooser = new HlsChooser();
        SliderChooser sliderChooser = new SliderChooser();
        AlphaChooser alphaChooser = new AlphaChooser();
        hlsChooser.setModel(getModel());//FIXME That model can change any time!
        sliderChooser.setModel(getModel());//FIXME That model can change any time!
        alphaChooser.setModel(getModel());//FIXME That model can change any time!

        choosersPane.getChildren().setAll(hlsChooser, alphaChooser);
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

    public @NonNull ObjectProperty<ColorChooserPaneModel> modelProperty() {
        return model;
    }

    public void setModel(ColorChooserPaneModel model) {
        this.model.set(model);
    }
}
