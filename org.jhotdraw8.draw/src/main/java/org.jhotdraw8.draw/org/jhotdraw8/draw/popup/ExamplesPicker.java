/*
 * @(#)ExamplesPicker.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.css.text.CssConverter;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.text.Converter;
import org.jhotdraw8.util.Resources;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.BiConsumer;

/**
 * Picker for boolean values.
 */
public class ExamplesPicker<T> extends AbstractPicker<T> {
    private ContextMenu contextMenu;
    private MenuItem noneItem;
    private BiConsumer<Boolean, T> callback;
    private final @NonNull ImmutableList<String> examples;
    private final @NonNull Converter<T> converter;

    public ExamplesPicker(@NonNull ImmutableList<String> examples, @NonNull Converter<T> converter) {
        this.examples = examples;
        this.converter = converter;
    }


    private void init() {
        Resources labels = DrawLabels.getResources();
        contextMenu = new ContextMenu();
        for (String s : examples) {
            MenuItem valueItem = new MenuItem(s);
            valueItem.setOnAction(e -> {
                try {
                    callback.accept(true, converter.fromString(s));
                } catch (ParseException | IOException ex) {
                    ex.printStackTrace();
                }
            });
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
