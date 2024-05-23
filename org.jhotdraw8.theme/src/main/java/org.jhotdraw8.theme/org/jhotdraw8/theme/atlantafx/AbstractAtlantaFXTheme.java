/*
 * @(#)AbstractAtlantaFXTheme.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.theme.atlantafx;

import javafx.scene.paint.Color;
import org.jhotdraw8.color.OKLchColorSpace;
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

    protected AbstractAtlantaFXTheme(String name, String appearance, String uaStylesheetUrl) {
        super(name, appearance);
        this.uaStylesheetUrl = uaStylesheetUrl;
    }

    @Override
    public String createUserAgentStylesheet(ThemeParameters params) {
        StringBuilder buf = new StringBuilder();
        buf.append("@import \"").append(uaStylesheetUrl).append("\";\n");
        buf.append(".root {\n");
        buf.append("-fx-font-size:").append(params.getFontSize()).append("px;\n");
        Color accentColor = params.getAccentColor() == null ? Color.BLACK : params.getAccentColor();
        OKLchColorSpace cs = new OKLchColorSpace();
        float[] lch = cs.fromRGB(new float[]{(float) accentColor.getRed(), (float) accentColor.getGreen(), (float) accentColor.getBlue()});
        float[] rgb = new float[3];
        switch (getAppearance()) {

            case "Light" -> {
                if (lch[0] > 0.4f) {
                    lch[0] = 0.4f;
                    cs.toRGB(lch, rgb);
                    accentColor = new Color(rgb[0], rgb[1], rgb[2], 1.0);
                }
            }
            case "Dark" -> {
                if (lch[0] < 0.6f) {
                    lch[0] = 0.6f;
                    cs.toRGB(lch, rgb);
                    accentColor = new Color(rgb[0], rgb[1], rgb[2], 1.0);
                }
            }
        }
        Color accentColorMuted = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 0.4);
        Color accentColorSubtle = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 0.2);
        String accentColorStr = toWebColor(accentColor);
        buf.append("-fx-accent:").append(accentColorStr).append(";\n");
        for (int idx = 0, count = 10; idx < count; idx++) {
            lch[0] = (float) (count + 1 - idx) / (count + 2);
            cs.toRGB(lch, rgb);
            Color cc = new Color(rgb[0], rgb[1], rgb[2], 1.0);
            buf.append("-color-accent-").append(idx).append(":").append(toWebColor(cc)).append(";\n");
        }
        buf.append("-color-accent-fg:").append(accentColorStr).append(";\n");
        buf.append("-color-accent-emphasis:").append(accentColorStr).append(";\n");
        buf.append("-color-accent-muted:").append(toWebColor(accentColorMuted)).append(";\n");
        buf.append("-color-accent-subtle:").append(toWebColor(accentColorSubtle)).append(";\n");
        buf.append("}\n");

        String customCss = params.getApplicationSpecificCss();
        if (customCss != null) {
            buf.append(customCss);
        }

        return Theme.toDataUrl(buf.toString());
    }


}
