/*
 * @(#)CssColorPicker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import org.jhotdraw8.css.value.CssColor;

import java.util.function.BiConsumer;

public class CssColorPicker extends AbstractPicker<CssColor> {
    private CssColorDialog dialog;

    public CssColorPicker() {
    }

    private void update(Node anchor, CssColor initialValue, BiConsumer<Boolean, CssColor> callback) {
        if (dialog == null) {
            dialog = new CssColorDialog(anchor.getScene().getWindow());
        }
        dialog.setOnUse(() -> callback.accept(true, dialog.getCurrentColor()));
        dialog.setOnSave(() -> callback.accept(true, dialog.getCurrentColor()));
        dialog.setCurrentColor(initialValue);
    }


    @Override
    public void show(Node anchor, double screenX, double screenY, CssColor initialValue, BiConsumer<Boolean, CssColor> callback) {
        update(anchor, initialValue, callback);
        dialog.show();
    }
}
