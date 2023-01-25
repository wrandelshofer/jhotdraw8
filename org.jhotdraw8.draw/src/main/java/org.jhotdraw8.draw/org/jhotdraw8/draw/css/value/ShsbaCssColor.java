/*
 * @(#)ShsbaCssColor.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.converter.CssNumberConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;

import static org.jhotdraw8.base.util.MathUtil.clamp;

/**
 * sHSBA color encoded with numbers and/or percentages.
 *
 * <pre>ISO EBNF 14977:
 *
 * sRGBA = "hsb(" hue , [ "," ] , saturation , [ "," ] , brightness ,
 *              [ [ ("/" , ",") ] , alpha ] ,
 *              ")"
 *       ;
 * hue         = number | percentage | degrees;
 * saturation  = number | percentage ;
 * brightness  = number | percentage ;
 * alpha       = number | percentage ;
 * degrees     = number , "deg" ;
 * percentage  = number , "%" ;
 *
 * number = integer | decimal ;
 * integer = digit , {digit} ;
 * decimal = {digit} , '.' , digit , {digit} ;
 *
 * digit = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' ;
 * </pre>
 * References:
 * <dl>
 *     <dt>CSS-4 HSL Colors</dt>
 *     <dd><a href="https://www.w3.org/TR/css-color-4/#the-hsl-notation">w3.org</a></dd>
 * </dl>
 */
public class ShsbaCssColor extends CssColor {
    private static final CssNumberConverter num = new CssNumberConverter(false);
    public static final SrgbaCssColor BLACK = new SrgbaCssColor(CssSize.ZERO, CssSize.ZERO, CssSize.ZERO, CssSize.ONE);

    private final @NonNull CssSize hue, saturation, brightness, opacity;

    public ShsbaCssColor(@NonNull Color color) {
        super(toName(
                CssSize.from(color.getHue()),
                CssSize.from(color.getSaturation()),
                CssSize.from(color.getBrightness()),
                CssSize.from(color.getOpacity())), color);
        this.hue = CssSize.from(color.getHue());
        this.saturation = CssSize.from(color.getSaturation());
        this.brightness = CssSize.from(color.getBrightness());
        this.opacity = CssSize.from(color.getOpacity());
    }

    public ShsbaCssColor(@NonNull CssSize hue, @NonNull CssSize saturation, @NonNull CssSize brightness, @NonNull CssSize opacity) {
        super(toName(hue, saturation, brightness, opacity),
                Color.hsb(
                        UnitConverter.PERCENTAGE.equals(hue.getUnits()) ? hue.getValue() / 360 : hue.getValue(),
                        clamp(UnitConverter.PERCENTAGE.equals(saturation.getUnits()) ? saturation.getValue() / 100 : saturation.getValue(), 0, 1),
                        clamp(UnitConverter.PERCENTAGE.equals(brightness.getUnits()) ? brightness.getValue() / 100 : brightness.getValue(), 0, 1),
                        clamp(UnitConverter.PERCENTAGE.equals(opacity.getUnits()) ? opacity.getValue() / 100 : opacity.getValue(), 0, 1)
                )
        );
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.opacity = opacity;
    }

    private static String toName(@NonNull CssSize hue, @NonNull CssSize saturation, @NonNull CssSize brightness, @NonNull CssSize opacity) {
        StringBuilder buf = new StringBuilder(20);
        if (UnitConverter.PERCENTAGE.equals(opacity.getUnits()) && opacity.getValue() == 100.0
                || opacity.getValue() == 1) {
            buf.append("hsb(");
            buf.append(num.toString(hue.getValue()));
            buf.append(hue.getUnits());
            buf.append(",");
            buf.append(num.toString(saturation.getValue()));
            buf.append(saturation.getUnits());
            buf.append(",");
            buf.append(num.toString(brightness.getValue()));
            buf.append(brightness.getUnits());
        } else {
            buf.append("hsba(");
            buf.append(num.toString(hue.getValue()));
            buf.append(hue.getUnits());
            buf.append(",");
            buf.append(num.toString(saturation.getValue()));
            buf.append(saturation.getUnits());
            buf.append(",");
            buf.append(num.toString(brightness.getValue()));
            buf.append(brightness.getUnits());
            buf.append(",");
            buf.append(num.toString(opacity.getValue()));
            buf.append(opacity.getUnits());
        }
        buf.append(')');
        return buf.toString();
    }
}
