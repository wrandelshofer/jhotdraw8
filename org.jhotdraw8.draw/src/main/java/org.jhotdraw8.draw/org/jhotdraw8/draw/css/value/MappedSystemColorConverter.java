/*
 * @(#)MappedSystemColorConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

public class MappedSystemColorConverter implements SystemColorConverter {
    private final ImmutableMap<String, Color> systemColors;

    public MappedSystemColorConverter(ImmutableMap<String, Color> systemColors) {
        this.systemColors = systemColors;
    }

    @Override
    public Color convert(@NonNull CssColor value) {
        Color systemColor = systemColors.get(value.getName());
        return systemColor != null ? systemColor : value.getColor();
    }

}
