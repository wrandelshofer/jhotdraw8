/*
 * @(#)FontFamilyPicker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import org.jhotdraw8.fxcontrols.fontchooser.FontFamilyDialog;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

public class FontFamilyPicker extends AbstractPicker<String> {
    private FontFamilyDialog dialog;

    public FontFamilyPicker() {
    }

    private void update(Node anchor) {
        if (dialog == null) {
            dialog = new FontFamilyDialog();
        }
    }

    @Override
    public void show(Node anchor, double screenX, double screenY,
                     @Nullable String initialValue, BiConsumer<Boolean, String> callback) {
        String initial = initialValue == null
                ? "Arial"
                : initialValue;
        update(anchor);
        Optional<String> s = dialog.showAndWait(initial);
        s.ifPresent(v -> callback.accept(true, v));
    }

    @Override
    public void hide() {
        dialog.hide();
    }


}
