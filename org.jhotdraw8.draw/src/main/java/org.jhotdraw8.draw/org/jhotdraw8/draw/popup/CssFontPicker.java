/*
 * @(#)CssFontPicker.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.scene.Node;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssFont;
import org.jhotdraw8.fxcontrols.fontchooser.FontDialog;
import org.jhotdraw8.fxcontrols.fontchooser.FontFamilySize;

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
                     @Nullable CssFont initialValue, @NonNull BiConsumer<Boolean, CssFont> callback) {
        CssFont initial = initialValue == null ? new CssFont("Arial", FontWeight.NORMAL, FontPosture.REGULAR, CssSize.from(13)) : initialValue;
        update(anchor);
        Optional<FontFamilySize> s = dialog.showAndWait(new FontFamilySize(initial.getFamily(), initial.getSize().getConvertedValue()));
        s.ifPresent(v -> callback.accept(true, new CssFont(v.getFamily(), initial.getWeight(), initial.getPosture(),
                CssSize.from(v.getSize()))));
    }
}
