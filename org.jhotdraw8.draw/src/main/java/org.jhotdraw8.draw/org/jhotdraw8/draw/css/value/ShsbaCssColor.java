/*
 * @(#)SHsbaCssColor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;

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
    private static final DoubleCssConverter num = new DoubleCssConverter(false);
    public static final SrgbaCssColor BLACK = new SrgbaCssColor(CssSize.ZERO, CssSize.ZERO, CssSize.ZERO, CssSize.ONE);

    private final CssSize hue, saturation, brightness, opacity;

    public ShsbaCssColor(Color color) {
        super(toName(
                CssSize.of(color.getHue()),
                CssSize.of(color.getSaturation()),
                CssSize.of(color.getBrightness()),
                CssSize.of(color.getOpacity())), color);
        this.hue = CssSize.of(color.getHue());
        this.saturation = CssSize.of(color.getSaturation());
        this.brightness = CssSize.of(color.getBrightness());
        this.opacity = CssSize.of(color.getOpacity());
    }

    public ShsbaCssColor(CssSize hue, CssSize saturation, CssSize brightness, CssSize opacity) {
        double value = UnitConverter.PERCENTAGE.equals(opacity.getUnits()) ? opacity.getValue() / 100 : opacity.getValue();
        double value1 = UnitConverter.PERCENTAGE.equals(brightness.getUnits()) ? brightness.getValue() / 100 : brightness.getValue();
        double value2 = UnitConverter.PERCENTAGE.equals(saturation.getUnits()) ? saturation.getValue() / 100 : saturation.getValue();
        super(toName(hue, saturation, brightness, opacity),
                Color.hsb(
                        UnitConverter.PERCENTAGE.equals(hue.getUnits()) ? hue.getValue() / 360 : hue.getValue(),
                        Math.clamp(value2, 0, 1),
                        Math.clamp(value1, 0, 1),
                        Math.clamp(value, 0, 1)
                )
        );
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.opacity = opacity;
    }

    private static String toName(CssSize hue, CssSize saturation, CssSize brightness, CssSize opacity) {
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
