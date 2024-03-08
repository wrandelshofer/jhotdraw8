/*
 * @(#)DefaultSystemColorConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import org.jhotdraw8.icollection.ChampMap;
import org.jhotdraw8.icollection.MapEntries;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

import java.util.Map;

/**
 * The default color converter provides a fixed set of system colors.
 */
public class DefaultSystemColorConverter extends MappedSystemColorConverter {
    public static final ImmutableMap<String, Color> LIGHT_SYSTEM_COLORS;

    static {
        LIGHT_SYSTEM_COLORS = ChampMap.copyOf(MapEntries.ofEntries(
                Map.entry(CANVAS, Color.WHITE),
                Map.entry(CANVAS_TEXT, Color.BLACK),
                Map.entry(LINK_TEXT, Color.NAVY),
                Map.entry(VISITED_TEXT, Color.PURPLE),
                Map.entry(ACTIVE_TEXT, Color.RED),
                Map.entry(BUTTON_FACE, Color.SILVER),
                Map.entry(BUTTON_TEXT, Color.BLACK),
                Map.entry(FIELD, Color.WHITE),
                Map.entry(FIELD_TEXT, Color.BLACK),
                Map.entry(HIGHLIGHT, Color.YELLOW),
                Map.entry(HIGHLIGHT_TEXT, Color.BLACK),
                Map.entry(GRAY_TEXT, Color.GRAY)));
    }

    public DefaultSystemColorConverter() {
        super(LIGHT_SYSTEM_COLORS);
    }

}
