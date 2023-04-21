/*
 * @(#)EnumPicker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.draw.DrawLabels;

import java.util.function.BiConsumer;

/**
 * Picker for boolean values.
 *
 * @param <T> the enum type
 */
public class EnumPicker<T extends Enum<T>> extends AbstractPicker<T> {
    private ContextMenu contextMenu;
    private MenuItem noneItem;
    private BiConsumer<Boolean, T> callback;
    private final @NonNull Converter<T> converter;
    private final @NonNull Class<T> enumClazz;

    public EnumPicker(@NonNull Class<T> enumClazz, @NonNull Converter<T> converter) {
        this.enumClazz = enumClazz;
        this.converter = converter;
    }


    private void init() {
        Resources labels = DrawLabels.getResources();
        contextMenu = new ContextMenu();
        for (T enumConstant : enumClazz.getEnumConstants()) {
            String s = converter.toString(enumConstant);
            MenuItem valueItem = new MenuItem(s);
            valueItem.setOnAction(e -> callback.accept(true, enumConstant));
            contextMenu.getItems().add(valueItem);
        }

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
    }

    private void update(T initialValue) {
        if (contextMenu == null) {
            init();
        }
        if (converter instanceof CssConverter<?>) {
            CssConverter<?> cssConverter = (CssConverter<?>) converter;
            noneItem.setVisible(cssConverter.isNullable());
        }
    }

    @Override
    public void show(Node anchor, double screenX, double screenY,
                     T initialValue, BiConsumer<Boolean, T> callback) {
        update(initialValue);
        this.callback = callback;
        contextMenu.show(anchor, screenX, screenY);
    }
}
