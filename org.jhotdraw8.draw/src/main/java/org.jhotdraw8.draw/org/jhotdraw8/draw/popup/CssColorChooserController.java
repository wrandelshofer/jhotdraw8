/*
 * @(#)CssColorChooserController.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/**
 * Sample Skeleton for 'CssColorChooser.fxml' Controller Class
 */

package org.jhotdraw8.draw.popup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.css.converter.CssColorConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.fxbase.binding.CustomBinding;

import java.net.URL;
import java.util.ResourceBundle;

public class CssColorChooserController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="colorPicker"
    private ColorPicker colorPicker; // Value injected by FXMLLoader
    private final @NonNull CssColorConverter converter = new CssColorConverter();

    public CssColorChooserController() {
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert colorPicker != null : "fx:id=\"colorPicker\" was not injected: check your FXML file 'CssColorChooser.fxml'.";

        CustomBinding.bindBidirectionalAndConvert(
                colorPicker.valueProperty(),
                color,
                CssColor::ofColor,
                CssColor::toColor);

    }

    private @NonNull
    final ObjectProperty<CssColor> color = new SimpleObjectProperty<>(this, "color");

    public @NonNull ObjectProperty<CssColor> colorProperty() {
        return color;
    }
}
