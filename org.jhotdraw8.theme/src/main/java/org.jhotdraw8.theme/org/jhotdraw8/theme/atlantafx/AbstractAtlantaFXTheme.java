/*
 * @(#)AtlantaFXTheme.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.theme.atlantafx;

import javafx.scene.paint.Color;
import org.jhotdraw8.color.HSLuvColorUtil;
import org.jhotdraw8.os.Appearance;
import org.jhotdraw8.theme.AbstractTheme;
import org.jhotdraw8.theme.Theme;
import org.jhotdraw8.theme.ThemeParameters;

import static org.jhotdraw8.color.FXColorUtil.toWebColor;


/**
 * Abstract base class for AtlantaFX themes.
 * <p>
 * References:
 * <dl>
 *     <dt>AtlantaFX, Copyright (c) 2022 mkpaz, MIT License</dt>
 *     <dd><a href="https://github.com/mkpaz/atlantafx">github.com</a></dd>
 * </dl>
 */
public abstract class AbstractAtlantaFXTheme extends AbstractTheme {
    private final String uaStylesheetUrl;

    protected AbstractAtlantaFXTheme(String name, Appearance appearance, String uaStylesheetUrl) {
        super(name, appearance);
        this.uaStylesheetUrl = uaStylesheetUrl;
    }

    @Override
    public String createUserAgentStylesheet(ThemeParameters params) {
        StringBuilder buf = new StringBuilder();
        buf.append("@import \"" + uaStylesheetUrl + "\";\n");
        buf.append(".root {\n");
        buf.append("-fx-font-size:" + params.getFontSize() + "px;\n");
        Color accentColor = params.getAccentColor() == null ? Color.BLACK : params.getAccentColor();
        HSLuvColorUtil hsLuvColorUtil = new HSLuvColorUtil();
        switch (getAppearance()) {

        case LIGHT -> {
            if (hsLuvColorUtil.getLightness(accentColor) > 0.4f) {
                accentColor = hsLuvColorUtil.adjustLightness(accentColor, 0.4f);
            }
        }
        case DARK -> {
            if (hsLuvColorUtil.getLightness(accentColor) < 0.6f) {
                accentColor = hsLuvColorUtil.adjustLightness(accentColor, 0.6f);
            }
        }
        }
        Color accentColorMuted = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 0.4);
        Color accentColorSubtle = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 0.2);
        String accentColorStr = toWebColor(accentColor);
        buf.append("-fx-accent:" + accentColorStr + ";\n");
        for (int idx = 0, count = 10; idx < count; idx++) {
            Color cc = hsLuvColorUtil.adjustLightness(accentColor, (float) (count + 1 - idx) / (count + 2));
            buf.append("-color-accent-" + idx + ":" + toWebColor(cc) + ";\n");
        }
        buf.append("-color-accent-fg:" + accentColorStr + ";\n");
        buf.append("-color-accent-emphasis:" + accentColorStr + ";\n");
        buf.append("-color-accent-muted:" + toWebColor(accentColorMuted) + ";\n");
        buf.append("-color-accent-subtle:" + toWebColor(accentColorSubtle) + ";\n");
        buf.append("}\n");

        String customCss = params.getApplicationSpecificCss();
        if (customCss != null) {
            buf.append(customCss);
        }

        return Theme.toDataUrl(buf.toString());
    }


}
