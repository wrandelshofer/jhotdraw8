/*
 * @(#)HandlesInspector.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.converter.CssColorConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.NamedCssColor;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxbase.binding.CustomBinding;
import org.jhotdraw8.fxbase.concurrent.PlatformUtil;
import org.jhotdraw8.fxbase.converter.StringConverterAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class HandlesInspector extends AbstractDrawingViewInspector {

    private final @NonNull NonNullObjectProperty<CssColor> handleColorProperty = new NonNullObjectProperty<>(this, "handleColor", NamedCssColor.BLUE);
    private final @NonNull IntegerProperty handleSizeProperty = new SimpleIntegerProperty(this, "handleSize", 11);
    private final @NonNull IntegerProperty handleStrokeWidthProperty = new SimpleIntegerProperty(this, "handleStrokeWidth", 1);
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="handleColorField"
    private TextField handleColorField; // Value injected by FXMLLoader
    @FXML // fx:id="handleColorPicker"
    private ColorPicker handleColorPicker; // Value injected by FXMLLoader
    @FXML // fx:id="handleSizeField"
    private TextField handleSizeField; // Value injected by FXMLLoader
    @FXML // fx:id="handleSizeSlider"
    private Slider handleSizeSlider; // Value injected by FXMLLoader
    @FXML
    private TextField handleStrokeWidthField;
    @FXML
    private Slider handleStrokeWidthSlider;
    private Node node;

    public HandlesInspector() {
        this(GridInspector.class.getResource("HandlesInspector.fxml"));
    }

    public HandlesInspector(@NonNull URL fxmlUrl) {
        init(fxmlUrl);
    }

    @Override
    public Node getNode() {
        return node;
    }

    private void init(@NonNull URL fxmlUrl) {
        // We must use invoke and wait here, because we instantiate Tooltips
        // which immediately instanciate a Window and a Scene.
        PlatformUtil.invokeAndWait(() -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(InspectorLabels.getResources().asResourceBundle());
            loader.setController(this);

            try (InputStream in = fxmlUrl.openStream()) {
                node = loader.load(in);
            } catch (IOException ex) {
                throw new InternalError(ex);
            }
        });
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert handleColorField != null : "fx:id=\"handleColorField\" was not injected.";
        assert handleColorPicker != null : "fx:id=\"handleColorPicker\" was not injected.";
        assert handleSizeField != null : "fx:id=\"handleSizeField\" was not injected.";
        assert handleSizeSlider != null : "fx:id=\"handleSizeSlider\" was not injected.";
        assert handleStrokeWidthField != null : "fx:id=\"handleStrokeWidthField\" was not injected: check your FXML file 'HandlesInspector.fxml'.";
        assert handleStrokeWidthSlider != null : "fx:id=\"handleStrokeWidthSlider\" was not injected: check your FXML file 'HandlesInspector.fxml'.";

        handleColorPicker.setValue(handleColorProperty.getValue().getColor());
        CustomBinding.bindBidirectionalAndConvert(//
                handleColorPicker.valueProperty(),//
                handleColorProperty,//
                CssColor::new,//
                (CssColor c) -> c == null ? null : c.getColor() //
        );
        handleColorField.textProperty().bindBidirectional(handleColorProperty, new StringConverterAdapter<>(
                new CssColorConverter(false)));


        handleSizeSlider.valueProperty().bindBidirectional(handleSizeProperty);
        Bindings.bindBidirectional(
                handleSizeField.textProperty(),
                handleSizeProperty,
                new NumberStringConverter());

        handleStrokeWidthSlider.valueProperty().bindBidirectional(handleStrokeWidthProperty);
        Bindings.bindBidirectional(
                handleStrokeWidthField.textProperty(),
                handleStrokeWidthProperty,
                new NumberStringConverter());
    }

    @Override
    protected void onDrawingViewChanged(ObservableValue<? extends DrawingView> observable, @Nullable DrawingView oldValue, @Nullable DrawingView newValue) {
        if (oldValue != null) {
            handleColorProperty.unbindBidirectional(oldValue.getEditor().handleColorProperty());
            handleSizeProperty.unbindBidirectional(oldValue.getEditor().handleSizeProperty());
            oldValue.getEditor().toleranceProperty().unbind();
            handleStrokeWidthProperty.unbindBidirectional(oldValue.getEditor().handleStrokeWidthProperty());
        }
        try {
            if (newValue != null) {
                handleColorProperty.bindBidirectional(newValue.getEditor().handleColorProperty());
                handleSizeProperty.bindBidirectional(newValue.getEditor().handleSizeProperty());
                newValue.getEditor().toleranceProperty().bind(handleSizeProperty);
                handleStrokeWidthProperty.bindBidirectional(newValue.getEditor().handleStrokeWidthProperty());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
