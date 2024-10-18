/*
 * @(#)MappedSystemColorConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

public class MappedSystemColorConverter implements SystemColorConverter {
    private final PersistentMap<String, Color> systemColors;

    public MappedSystemColorConverter(PersistentMap<String, Color> systemColors) {
        this.systemColors = systemColors;
    }

    @Override
    public @Nullable Color convert(CssColor value) {
        Color systemColor = systemColors.get(value.getName());
        return systemColor != null ? systemColor : value.getColor();
    }

}
