/*
 * @(#)CssFontPicker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jhotdraw8.css.value.CssFont;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.fxcontrols.fontchooser.FontDialog;
import org.jhotdraw8.fxcontrols.fontchooser.FontFamilySize;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

public class CssFontPicker extends AbstractPicker<CssFont> {
    private FontDialog dialog;

    public CssFontPicker() {
    }

    private void update(Node anchor) {
        if (dialog == null) {
            dialog = new FontDialog();
        }
    }

    @Override
    public void show(Node anchor, double screenX, double screenY,
                     @Nullable CssFont initialValue, BiConsumer<Boolean, CssFont> callback) {
        CssFont initial = initialValue == null ? new CssFont("Arial", FontWeight.NORMAL, FontPosture.REGULAR, CssSize.of(13)) : initialValue;
        update(anchor);
        Optional<FontFamilySize> s = dialog.showAndWait(new FontFamilySize(initial.getFamily(), initial.getSize().getConvertedValue()));
        s.ifPresent(v -> callback.accept(true, new CssFont(v.family(), initial.getWeight(), initial.getPosture(),
                CssSize.of(v.size()))));
    }

    @Override
    public void hide() {
        dialog.hide();
    }
}
