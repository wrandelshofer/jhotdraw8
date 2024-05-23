/*
 * @(#)ExamplesPicker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.text.ParseException;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Picker for picking an example from a set of provided examples.
 *
 * @param <T> the type of the examples
 */
public class ExamplesPicker<T> extends AbstractPicker<T> {
    private ContextMenu contextMenu;
    private MenuItem noneItem;
    private BiConsumer<Boolean, T> callback;
    private final ImmutableList<String> examples;
    private final Converter<T> converter;

    public ExamplesPicker(ImmutableList<String> examples, Converter<T> converter) {
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
                } catch (ParseException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

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
        if (converter instanceof CssConverter<?> cssConverter) {
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
