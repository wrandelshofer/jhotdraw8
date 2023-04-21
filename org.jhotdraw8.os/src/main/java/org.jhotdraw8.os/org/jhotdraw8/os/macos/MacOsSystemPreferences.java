/*
 * @(#)MacOsSystemPreferences.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.os.macos;

import javafx.scene.paint.Color;
import org.jhotdraw8.os.AbstractSystemPreferences;

import java.util.LinkedHashMap;

public class MacOsSystemPreferences extends AbstractSystemPreferences {
    public MacOsSystemPreferences() {
    }

    /**
     * Synchronously loads the system preferences.
     */
    public void load() {
        LinkedHashMap<String, Object> prefs = new LinkedHashMap<>();
        MacOSPreferencesUtil.readPreferences(MacOSPreferencesUtil.GLOBAL_PREFERENCES, prefs);
        Object interfaceStyleValue = MacOSPreferencesUtil.get(prefs, "AppleInterfaceStyle");
        if (interfaceStyleValue instanceof String appearance) {
            this.appearance.set(appearance);
        }
        Object accentColorValue = MacOSPreferencesUtil.get(prefs, "AppleAccentColor");
        Color accentColor = Color.BLACK;
        if (accentColorValue instanceof Number) {
            int accentColorIntValue = ((Number) (accentColorValue)).intValue();
            if ("Dark".equals(interfaceStyleValue)) {
                switch (accentColorIntValue) {
                    case 0://red
                        accentColor = Color.web("#ec5f5e");
                        break;
                    case 1://orange
                        accentColor = Color.web("#e8883a");
                        break;
                    case 2://yellow
                        accentColor = Color.web("#f7c844");
                        break;
                    case 3://green
                        accentColor = Color.web("#77b856");
                        break;
                    case 4://blue
                        accentColor = Color.web("#3378F7");
                        break;
                    case 5://purple
                        accentColor = Color.web("#9a55a2");
                        break;
                    case 6://pink
                        accentColor = Color.web("#e45c9c");
                        break;
                    case -1://graphite
                        accentColor = Color.web("#8c8c8c");
                        break;
                    default:
                        // multicolor
                        break;
                }
            } else {
                switch (accentColorIntValue) {
                    case 0://red
                        accentColor = Color.web("#cf4745");
                        break;
                    case 1://orange
                        accentColor = Color.web("#e8883a");
                        break;
                    case 2://yellow
                        accentColor = Color.web("#f7c94e");
                        break;
                    case 3://green
                        accentColor = Color.web("#78b957");
                        break;
                    case 4://blue
                        accentColor = Color.web("#3378F6");
                        break;
                    case 5://purple
                        accentColor = Color.web("#8a4292");
                        break;
                    case 6://pink
                        accentColor = Color.web("#e45c9c");
                        break;
                    case -1://graphite
                        accentColor = Color.web("#989898");
                        break;
                    default:
                        // multicolor
                        break;
                }
            }
        } else {
            // multicolor!
        }
        if (accentColorValue != null) {
            System.out.println("accentColorValue " + accentColorValue + " " + accentColorValue.getClass());
        }
    }
}
