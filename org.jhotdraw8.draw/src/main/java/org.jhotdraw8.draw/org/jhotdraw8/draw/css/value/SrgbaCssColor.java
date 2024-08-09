/*
 * @(#)SrgbaCssColor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import org.jhotdraw8.base.converter.FloatConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;

/**
 * sRGBA color encoded with numbers and/or percentages.
 *
 * <pre>ISO EBNF 14977:
 *
 * sRGBA = "rgb(" red , [ "," ] , green , [ "," ] , blue ,
 *              [ [ ("/" , ",") ] , alpha ] ,
 *              ")"
 *       ;
 * red   = number | percentage ;
 * green = number | percentage ;
 * blue  = number | percentage ;
 * alpha = number | percentage ;
 * percentage = number , "%" ;
 *
 * number = integer | decimal ;
 * integer = digit , {digit} ;
 * decimal = {digit} , '.' , digit , {digit} ;
 *
 * digit = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' ;
 * </pre>
 * References:
 * <dl>
 *     <dt>CSS-4 RGB functions</dt>
 *     <dd><a href="https://www.w3.org/TR/css-color-4/#rgb-functions">w3.org</a></dd>
 * </dl>
 */
public class SrgbaCssColor extends CssColor {
    private static final FloatConverter num = new FloatConverter();
    public static final SrgbaCssColor BLACK = new SrgbaCssColor(CssSize.ZERO, CssSize.ZERO, CssSize.ZERO, CssSize.ONE);

    private final CssSize red, green, blue, opacity;

    public SrgbaCssColor(Color color) {
        super(toName(
                CssSize.of(color.getRed() * 100, UnitConverter.PERCENTAGE),
                CssSize.of(color.getGreen() * 100, UnitConverter.PERCENTAGE),
                CssSize.of(color.getBlue() * 100, UnitConverter.PERCENTAGE),
                CssSize.of(color.getOpacity())), color);
        this.red = CssSize.of(color.getRed() * 100, UnitConverter.PERCENTAGE);
        this.green = CssSize.of(color.getGreen() * 100, UnitConverter.PERCENTAGE);
        this.blue = CssSize.of(color.getBlue() * 100, UnitConverter.PERCENTAGE);
        this.opacity = CssSize.of(color.getOpacity());
    }

    public SrgbaCssColor(CssSize red, CssSize green, CssSize blue, CssSize opacity) {
        double value = UnitConverter.PERCENTAGE.equals(opacity.getUnits()) ? opacity.getValue() / 100 : opacity.getValue();
        double value1 = UnitConverter.PERCENTAGE.equals(blue.getUnits()) ? blue.getValue() * 2.55 : blue.getValue();
        double value2 = UnitConverter.PERCENTAGE.equals(green.getUnits()) ? green.getValue() * 2.55 : green.getValue();
        double value3 = UnitConverter.PERCENTAGE.equals(red.getUnits()) ? red.getValue() * 2.55 : red.getValue();
        double value4 = UnitConverter.PERCENTAGE.equals(opacity.getUnits()) ? opacity.getValue() / 100 : opacity.getValue();
        double value5 = UnitConverter.PERCENTAGE.equals(blue.getUnits()) ? blue.getValue() / 100 : blue.getValue() / 255;
        double value6 = UnitConverter.PERCENTAGE.equals(green.getUnits()) ? green.getValue() / 100 : green.getValue() / 255;
        double value7 = UnitConverter.PERCENTAGE.equals(red.getUnits()) ? red.getValue() / 100 : red.getValue() / 255;
        super(toName(red, green, blue, opacity),

                (UnitConverter.PERCENTAGE.equals(red.getUnits())
                        || UnitConverter.PERCENTAGE.equals(green.getUnits())
                        || UnitConverter.PERCENTAGE.equals(blue.getUnits()))
                        ? Color.color(
                        Math.clamp(value7, 0, 1),
                        Math.clamp(value6, 0, 1),
                        Math.clamp(value5, 0, 1),
                        Math.clamp(value4, 0, 1)
                )
                        : Color.rgb(
                        (int) Math.round(Math.clamp(value3, 0, 255)),
                        (int) Math.round(Math.clamp(value2, 0, 255)),
                        (int) Math.round(Math.clamp(value1, 0, 255)),
                        Math.clamp(value, 0, 1)
                )
        );
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.opacity = opacity;
    }

    private static String toName(CssSize red, CssSize green, CssSize blue, CssSize opacity) {
        StringBuilder buf = new StringBuilder(20);
        if (UnitConverter.PERCENTAGE.equals(opacity.getUnits()) && opacity.getValue() == 100.0
                || opacity.getValue() == 1) {
            buf.append("rgb(");
            buf.append(num.toString((float) red.getValue()));
            buf.append(red.getUnits());
            buf.append(",");
            buf.append(num.toString((float) green.getValue()));
            buf.append(green.getUnits());
            buf.append(",");
            buf.append(num.toString((float) blue.getValue()));
            buf.append(blue.getUnits());
        } else {
            buf.append("rgba(");
            buf.append(num.toString((float) red.getValue()));
            buf.append(red.getUnits());
            buf.append(",");
            buf.append(num.toString((float) green.getValue()));
            buf.append(green.getUnits());
            buf.append(",");
            buf.append(num.toString((float) blue.getValue()));
            buf.append(blue.getUnits());
            buf.append(",");
            buf.append(num.toString((float) opacity.getValue()));
            buf.append(opacity.getUnits());
        }
        buf.append(')');
        return buf.toString();
    }
}
