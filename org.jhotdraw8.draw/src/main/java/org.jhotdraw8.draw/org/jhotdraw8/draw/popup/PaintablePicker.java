/*
 * @(#)PaintablePicker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.Paintable;

import java.util.function.BiConsumer;

public class PaintablePicker extends AbstractPicker<Paintable> {
    // FIXME create CssPaintableDialog
    private CssColorPopup dialog;

    public PaintablePicker() {
    }

    private void update(Node anchor, CssColor initialValue, BiConsumer<Boolean, CssColor> callback) {
        if (dialog == null) {
            dialog = new CssColorPopup();
        }

        dialog.setCallback(callback);
        dialog.setCurrentColor(initialValue);

    }

    @Override
    public void show(Node anchor, double screenX, double screenY, Paintable initial, BiConsumer<Boolean, Paintable> callback) {
        update(anchor,
                (initial instanceof CssColor) ? ((CssColor) initial) : null,
                callback::accept
        );
        dialog.show(anchor, screenX, screenY);
    }
}
