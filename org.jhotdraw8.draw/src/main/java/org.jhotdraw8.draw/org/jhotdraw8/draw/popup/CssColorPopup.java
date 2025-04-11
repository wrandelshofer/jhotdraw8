/*
 * @(#)CssColorPopup.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.NamedCssColor;
import org.jhotdraw8.fxbase.binding.CustomBinding;

import java.util.function.BiConsumer;

public class CssColorPopup {
    private final ContextMenu contextMenu;
    private final MenuItem noneItem;
    private final ColorPicker colorPicker;
    private BiConsumer<Boolean, CssColor> callback;

    private final ObjectProperty<CssColor> currentColor = new SimpleObjectProperty<>(NamedCssColor.WHITE);

    public CssColorPopup() {
        Resources labels = DrawLabels.getResources();
        contextMenu = new ContextMenu();

        colorPicker = new ColorPicker();
        MenuItem colorPickerItem = new MenuItem(null, colorPicker);
        contextMenu.getItems().add(colorPickerItem);
        colorPicker.setOnAction(event -> callback.accept(true, CssColor.ofColor(colorPicker.getValue())));

        MenuItem unsetItem = new MenuItem();
        unsetItem.setOnAction(e -> callback.accept(false, null));
        noneItem = new MenuItem();
        noneItem.setOnAction(e -> callback.accept(true, null));
        labels.configureMenuItem(unsetItem, "value.unset");
        labels.configureMenuItem(noneItem, "value.none");
        contextMenu.getItems().addAll(
                new SeparatorMenuItem(),
                unsetItem, noneItem
        );

        CustomBinding.bindBidirectionalAndConvert(
                colorPicker.valueProperty(),
                currentColor,
                CssColor::ofColor,
                CssColor::toColor);
    }


    public BiConsumer<Boolean, CssColor> getCallback() {
        return callback;
    }

    public void setCallback(BiConsumer<Boolean, CssColor> callback) {
        this.callback = callback;
    }

    public CssColor getCurrentColor() {
        return currentColor.get();
    }

    public @Nullable ObjectProperty<CssColor> currentColorProperty() {
        return currentColor;
    }

    public void setCurrentColor(CssColor currentColor) {
        this.currentColor.set(currentColor);
    }


    public void show(Node anchor, double screenX, double screenY) {
        contextMenu.show(anchor, screenX, screenY);
    }

}
