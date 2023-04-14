/*
 * @(#)CssColorConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.converter.NumberConverter;
import org.jhotdraw8.color.CssColorSpaces;
import org.jhotdraw8.color.CssHslColorSpace;
import org.jhotdraw8.color.CssLegacySrgbColorSpace;
import org.jhotdraw8.color.HsbColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.SrgbColorSpace;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.NamedCssColor;
import org.jhotdraw8.draw.css.value.ShsbaCssColor;
import org.jhotdraw8.draw.css.value.SrgbaCssColor;
import org.jhotdraw8.draw.css.value.SystemCssColor;
import org.jhotdraw8.draw.css.value.Uint4HexSrgbaCssColor;
import org.jhotdraw8.draw.css.value.Uint8HexSrgbaCssColor;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.jhotdraw8.base.util.MathUtil.clamp;

/**
 * CssColorConverter.
 * <p>
 * Parses the following EBNF:
 * </p>
 * <pre>
 * CssColor ::= NamedColor | HexColor | ColorFunction  ;
 *
 * NamedColor ::= 'none' | TT_IDENT;
 *
 * HexColor ::= ('#'|'0x') , ( hexdigit * 3 | hexdigit * 4 | hexdigit * 6 | hexdigit * 8 );
 *
 * ColorFunction ::= RGBFunction | RGBAFunction
 *                 | HSBFunction | HSBAFunction
 *                 | HSLFunction | HSLAFunction
 *                 | HWBFunction
 *                 | LABFunction
 *                 | LCHFunction
 *                 | OKLABFunction
 *                 | OKLCHFunction
 *                 | COLORFunction
 *                 ;
 * RGBFunction   ::= 'rgb('   , color-params , ')' ;
 * RGBAFunction  ::= 'rgba('  , color-params , ')' ;
 * HSBFunction   ::= 'hsb('   , color-params , ')' ;
 * HSBAFunction  ::= 'hsba('   , color-params , ')' ;
 * HSLFunction   ::= 'hsl('   , color-params , ')' ;
 * HSLAFunction  ::= 'hsla('  , color-params , ')' ;
 * HWBFunction   ::= 'hwb('   , color-params , ')' ;
 * LABFunction   ::= 'lab('   , color-params , ')' ;
 * OKLABFunction ::= 'oklab(' , color-params , ')' ;
 * OKLCHFunction ::= 'oklch(' , color-params , ')' ;
 * COLORFunction ::= 'color(' ,  color-params , ')' ;
 *
 * color-params  ::= [ color-space-param ] , ( ( number | angle | percentage | 'none' ) , [ "," ] ) * 3 , alpha-param ;
 * alpha-param   ::= [ [ '/' ] ( number | percentage | 'none' ) ] ;
 * color-space-param ::= 'srgb'
 *                     | 'srgb-linear'
 *                     | 'display-p3'
 *                     | 'a98-rgb'
 *                     | 'prophoto-rgb'
 *                     | 'rec2020'
 *                     | 'xyz'
 *                     | 'xyz-d50'
 *                     | 'xyz-d65'
 *                     ;
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4. 4. Representing Colors: the <color> type.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-type">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. 4. Representing Colors: the <color> type.  4.1 The <color> syntax.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-syntax">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. 5. sRGB Colors.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#rgb-functions">w3.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class CssColorConverter implements CssConverter<CssColor> {
    private final static @NonNull NumberConverter number = new NumberConverter();

    boolean nullable;

    public CssColorConverter() {
        this(false);
    }

    public CssColorConverter(boolean nullable) {
        this.nullable = nullable;
    }

    private String colorParamToRgbString(List<CssSize> params) {
        StringBuilder buf = new StringBuilder(16);
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                buf.append(' ');
            }
            boolean asPercentage = "%".equals(params.get(0).getUnits());
            double cp = params.get(i).getValue();
            if (asPercentage) {
                buf.append(number.toString(cp * 100d / 255d));
                buf.append('%');
            } else {
                buf.append(number.toString(cp));
            }
        }
        if (params.size() == 4) {
            boolean asPercentage = "%".equals(params.get(3).getUnits());
            double clampedAlpha = clamp(params.get(3).getValue(), 0, 1);
            if (clampedAlpha != 1) {
                buf.append(" / ");
                if (asPercentage) {
                    buf.append(number.toString(clampedAlpha * 100));
                    buf.append('%');
                } else {
                    buf.append(number.toString(clampedAlpha));
                }
            }
        }
        return buf.toString();
    }

    private String colorParamToHslString(List<CssSize> params) {
        StringBuilder buf = new StringBuilder(16);
        buf.append(number.toString(params.get(0).getValue()));
        for (int i = 1; i < 3; i++) {
            buf.append(' ');
            double cp = params.get(i).getValue();
            if ("%".equals(params.get(i).getUnits())) {
                buf.append(number.toString(cp * 100));
                buf.append('%');
            } else {
                buf.append(number.toString(cp));
            }
        }
        if (params.size() == 4) {
            double clampedAlpha = clamp(params.get(3).getValue(), 0, 1);
            if (clampedAlpha != 1) {
                buf.append(" / ");
                if ("%".equals(params.get(3).getUnits())) {
                    buf.append(number.toString(clampedAlpha * 100));
                    buf.append('%');
                } else {
                    buf.append(number.toString(clampedAlpha));
                }
            }
        }
        return buf.toString();
    }

    @Override
    public @Nullable CssColor getDefaultValue() {
        return null;
    }

    @Override
    public @NonNull String getHelpText() {
        return "Format of ⟨Color⟩: " + "⟨name⟩｜#⟨hex⟩｜rgb(⟨r⟩,⟨g⟩,⟨b⟩)｜rgba(⟨r⟩,⟨g⟩,⟨b⟩,⟨a⟩)｜hsb(⟨h⟩,⟨s⟩,⟨b⟩)｜hsba(⟨h⟩,⟨s⟩,⟨b⟩,⟨a⟩)";
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public @Nullable CssColor parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        // CssColor ::= NamedColor | HexColor | ColorFunction  ;

        return switch (tt.next()) {
            case CssTokenType.TT_IDENT -> {
                tt.pushBack();
                yield parseNamedColor(tt);
            }
            case CssTokenType.TT_DIMENSION, CssTokenType.TT_HASH -> {
                tt.pushBack();
                yield parseHexColor(tt);
            }
            case CssTokenType.TT_FUNCTION -> {
                tt.pushBack();
                yield switch (tt.currentStringNonNull()) {
                    case "rgb", "rgba" -> {
                        yield parseRgbFunction(tt);
                    }
                    case "hsl", "hsla" -> {
                        yield parseHslFunction(tt);
                    }
                    case "hsb", "hsba" -> {
                        yield parseHsbFunction(tt);
                    }
                    case "hwb" -> {
                        yield null;
                    }
                    case "lab" -> {
                        yield null;
                    }
                    case "oklab" -> {
                        yield null;
                    }
                    case "oklch" -> {
                        yield null;
                    }
                    case "color" -> parseColorFunction(tt);
                    default ->
                            throw tt.createParseException("CssColor: unsupported function: " + tt.currentStringNonNull() + "().");
                };
            }
            default -> throw tt.createParseException("CssColor: named color, hex color or color function expected.");
        };
    }

    private @Nullable CssColor parseColorFunction(CssTokenizer tt) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "CssColor: function expected.");
        String functionName = tt.currentStringNonNull();

        String colorSpaceParam = "srgb";
        if ("color".equals(functionName)) {
            if (tt.next() == CssTokenType.TT_IDENT) {
                colorSpaceParam = tt.currentStringNonNull().toLowerCase();
                if ("xyz".equals(colorSpaceParam)) colorSpaceParam = "xyz-d65";
            } else {
                tt.pushBack();
            }
        }
        NamedColorSpace cs = CssColorSpaces.COLOR_SPACES.get(colorSpaceParam);
        if (cs == null) {
            throw tt.createParseException("CssColor: unsupported color space: '" + colorSpaceParam + "'.");
        }


        List<CssSize> params = parseParams(tt, cs);
        if (tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            throw tt.createParseException("CssColor: right bracket expected.");
        }
        float[] rgb = clampColors(params);
        return new CssColor(
                "color("
                        + colorSpaceParam + " "
                        + colorParamToRgbString(params)
                        + ")",
                new Color(rgb[0], rgb[1], rgb[2], params.size() == 4 ? clamp(params.get(3).getValue(), 0, 1) : 1.0));
    }

    private static @NonNull List<CssSize> parseParams(CssTokenizer tt, NamedColorSpace cs) throws IOException, ParseException {
        List<CssSize> params = new ArrayList<>();
        while (tt.next() != CssTokenType.TT_EOF && tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            switch (tt.current()) {
                case CssTokenType.TT_DIMENSION -> {
                    if (params.size() > 3) throw tt.createParseException("CssColor: too many parameters.");
                    float min = cs.getMinValue(params.size());
                    float max = cs.getMaxValue(params.size());
                    switch (tt.currentStringNonNull()) {
                        case "deg" -> {
                            params.add(CssSize.of((tt.currentNumberNonNull().doubleValue() % 360.0) * (max - min) / 360.0 + min, "deg"));
                        }
                        case "grad" -> {
                            params.add(CssSize.of(((tt.currentNumberNonNull().doubleValue() % 400.0) * (max - min) / 400.0 + min), "grad"));
                        }
                        case "rad" -> {
                            params.add(CssSize.of(((tt.currentNumberNonNull().doubleValue() % (2 * Math.PI)) * (max - min) * 0.5 / Math.PI + min), "rad"));
                        }
                        case "turn" -> {
                            params.add(CssSize.of(((tt.currentNumberNonNull().doubleValue() % 1.0) * (max - min) + min), "turn"));
                        }
                        default ->
                                throw tt.createParseException("CssColor: unsupported dimension: '" + tt.currentStringNonNull() + "'.");
                    }
                }
                case CssTokenType.TT_PERCENTAGE -> {
                    if (params.size() > 3) throw tt.createParseException("CssColor: too many parameters.");
                    if (params.size() == 3) {
                        // alpha
                        params.add(CssSize.of((tt.currentNumberNonNull().doubleValue() / 100.0), "%"));
                    } else {
                        float min = cs.getMinValue(params.size());
                        float max = cs.getMaxValue(params.size());
                        params.add(CssSize.of((tt.currentNumberNonNull().doubleValue() * (max - min) / 100.0 + min), "%"));
                    }
                }
                case CssTokenType.TT_NUMBER -> {
                    if (params.size() > 3) throw tt.createParseException("CssColor: too many parameters.");
                    params.add(CssSize.of(tt.currentNumberNonNull().doubleValue()));
                }
                case ',', '/' -> {
                }
                case CssTokenType.TT_IDENT -> {
                    switch (tt.currentStringNonNull()) {
                        case "none" -> {
                            if (params.size() > 3) throw tt.createParseException("CssColor: too many parameters.");
                            params.add(CssSize.ZERO);
                        }
                        default -> {
                            throw tt.createParseException("CssColor: 'none' expected.");
                        }
                    }
                }
            }
        }
        if (params.size() < 3) {
            throw tt.createParseException("CssColor: not enough parameters.");
        }
        return params;
    }

    private final static CssLegacySrgbColorSpace CSS_LEGACY_SRGB_COLOR_SPACE = new CssLegacySrgbColorSpace();
    private final static SrgbColorSpace CSS_SRGB_COLOR_SPACE = new SrgbColorSpace();
    private final static CssHslColorSpace CSS_HSL_COLOR_SPACE = new CssHslColorSpace();
    private final static HsbColorSpace HSB_COLOR_SPACE = new HsbColorSpace();

    @NonNull
    private @Nullable CssColor parseRgbFunction(CssTokenizer tt) throws ParseException, IOException {
        List<CssSize> params = parseParams(tt, CSS_LEGACY_SRGB_COLOR_SPACE);
        float[] rgb = toFloat(params);
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = rgb[i] / 255f;
        }
        clampColors(rgb);
        return new CssColor(
                "rgb(" + colorParamToRgbString(params) + ")",
                new Color(rgb[0], rgb[1], rgb[2], params.size() == 4 ? clamp(params.get(3).getValue(), 0, 1) : 1.0));
    }

    @NonNull
    private @Nullable CssColor parseHslFunction(CssTokenizer tt) throws ParseException, IOException {
        List<CssSize> params = parseParams(tt, CSS_HSL_COLOR_SPACE);
        float[] rgb = clampColors(CSS_HSL_COLOR_SPACE.toRGB(toFloat(params)));
        return new CssColor(
                "hsl(" + colorParamToHslString(params) + ")",
                new Color(rgb[0], rgb[1], rgb[2], params.size() == 4 ? clamp(params.get(3).getValue(), 0, 1) : 1.0));
    }

    @NonNull
    private @Nullable CssColor parseHsbFunction(CssTokenizer tt) throws ParseException, IOException {
        List<CssSize> params = parseParams(tt, HSB_COLOR_SPACE);
        float[] rgb = clampColors(HSB_COLOR_SPACE.toRGB(toFloat(params)));
        return new CssColor(
                "hsb(" + colorParamToHslString(params) + ")",
                new Color(rgb[0], rgb[1], rgb[2], params.size() == 4 ? clamp(params.get(3).getValue(), 0, 1) : 1.0));
    }

    private static float[] toFloat(double[] params) {
        float[] floats = new float[3];
        for (int i = 0; i < 3; i++) {
            floats[i] = (float) params[i];
        }
        return floats;
    }

    private static float[] toFloat(List<CssSize> params) {
        float[] floats = new float[3];
        for (int i = 0; i < 3; i++) {
            CssSize value = params.get(i);
            floats[i] = value == null ? 0 : (float) value.getValue();
        }
        return floats;
    }

    /**
     * FIXME Implement gamut mapping!
     * <p>
     * <a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#css-gamut-mapping">w3.org</a>
     *
     * @param param
     * @param params
     * @return
     */
    private static float[] clampColors(NamedColorSpace param, double[] params) {
        return clampColors(toFloat(params));
    }

    private static float[] clampColors(List<CssSize> params) {
        return clampColors(toFloat(params));
    }

    private static float[] clampColors(float[] params) {
        float[] rgb = new float[3];
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = clamp(params[i], 0, 1);
        }
        return rgb;
    }


    private @NonNull CssColor parseColorHexDigits(@NonNull String hexdigits, int startpos) throws ParseException {
        try {
            int v = (int) Long.parseLong(hexdigits, 16);
            int r, g, b, a;
            switch (hexdigits.length()) {
                case 3:
                    r = (((v & 0xf00) >>> 4) | (v & 0xf00) >>> 8);
                    g = (((v & 0x0f0)) | (v & 0x0f0) >>> 4);
                    b = ((v & 0x00f) << 4) | (v & 0x00f);
                    a = 255;
                    return new Uint4HexSrgbaCssColor(r, g, b, a);
                case 4:
                    r = (((v & 0xf000) >>> 8) | (v & 0xf000) >>> 12);
                    g = (((v & 0x0f00) >>> 4) | (v & 0x0f00) >>> 8);
                    b = (((v & 0x00f0)) | (v & 0x00f0) >>> 4);
                    a = ((v & 0x000f) << 4) | (v & 0x000f);
                    return new Uint4HexSrgbaCssColor(r, g, b, a);
                case 6:
                    r = (v & 0xff0000) >>> 16;
                    g = (v & 0x00ff00) >>> 8;
                    b = (v & 0x0000ff);
                    a = 255;
                    return new Uint8HexSrgbaCssColor(r, g, b, a);
                case 8:
                    r = (v & 0xff000000) >>> 24;
                    g = (v & 0x00ff0000) >>> 16;
                    b = (v & 0x0000ff00) >>> 8;
                    a = (v & 0xff);
                    return new Uint8HexSrgbaCssColor(r, g, b, a);
                default:
                    throw new ParseException("<hex-digits>: expected 3, 6  or 8 digits. Found:" + hexdigits, startpos);
            }
        } catch (NumberFormatException e) {
            ParseException pe = new ParseException("<hex-digits>: expected a hex-digit. Found:" + hexdigits, startpos);
            pe.initCause(e);
            throw pe;
        }
    }

    private @NonNull CssColor parseHexColor(CssTokenizer tt) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_DIMENSION -> {
                // If the color is written with a leading "0xabcdef", then the
                // color value is tokenized into a TT_DIMENSION. The unit
                // contains the leading 'x' and the color value 'abcdef'.
                if (tt.currentNumberNonNull().intValue() == 0 && (tt.currentNumber() instanceof Long)
                        && tt.currentStringNonNull().startsWith("x")) {
                    yield parseColorHexDigits(tt.currentStringNonNull().substring(1), tt.getStartPosition());
                } else {
                    throw tt.createParseException("CssColor: hex color expected.");
                }
            }
            case CssTokenType.TT_HASH -> {
                yield parseColorHexDigits(tt.currentStringNonNull(), tt.getStartPosition());
            }
            default -> throw tt.createParseException("CssColor: hex color expected.");
        };
    }

    private @Nullable CssColor parseNamedColor(CssTokenizer tt) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_IDENT, "CssColor: identifier expected.");
        String ident = tt.currentString();
        if ("none".equals(ident)) {
            return null;
        }
        CssColor color = NamedCssColor.of(tt.currentStringNonNull());
        if (color == null) {
            color = SystemCssColor.of(tt.currentStringNonNull());
        }
        return color;
    }

    //@Override
    public @Nullable CssColor parseOld(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws
            ParseException, IOException {
        CssColor color = null;

        if (nullable) {
            if (tt.nextIsIdentNone()) {
                return null;
            } else {
                tt.pushBack();
            }
        }

        switch (tt.next()) {
            case CssTokenType.TT_DIMENSION:
                // If the color is written with a leading "0xabcdef", then the
                // color value is tokenized into a TT_DIMENSION. The unit
                // contains the leading 'x' and the color value 'abcdef'.
                if (tt.currentNumberNonNull().intValue() == 0 && (tt.currentNumber() instanceof Long)
                        && tt.currentStringNonNull().startsWith("x")) {
                    color = parseColorHexDigits(tt.currentStringNonNull().substring(1), tt.getStartPosition());
                } else {
                    throw tt.createParseException("CssColor: hex color expected.");
                }
                break;
            case CssTokenType.TT_HASH:
                color = parseColorHexDigits(tt.currentStringNonNull(), tt.getStartPosition());
                break;
            case CssTokenType.TT_IDENT:
                color = NamedCssColor.of(tt.currentStringNonNull());
                if (color == null) {
                    color = SystemCssColor.of(tt.currentStringNonNull());
                }
                break;
            case CssTokenType.TT_FUNCTION:
                switch (tt.currentStringNonNull()) {
                    case "rgba":
                    case "rgb": {
                        color = parseSrgbaColor(tt);
                        break;
                    }
                    case "hsba":
                    case "hsb": {
                        color = parseShsbaColor(tt);
                        break;
                    }
                    default:
                        throw tt.createParseException("CssColor: color expected.");
                }
                if (tt.next() != ')') {
                    throw tt.createParseException("CssColor: ')' expected.");
                }
                break;
            default:
                throw tt.createParseException("CssColor: color expected.");
        }
        return color;
    }

    @NonNull
    private CssColor parseShsbaColor(@NonNull CssTokenizer tt) throws IOException, ParseException {
        CssColor color;
        int i = 0;
        CssSize[] sizes = new CssSize[4];
        while (i < 4 && (tt.next() == CssTokenType.TT_NUMBER
                || tt.current() == CssTokenType.TT_PERCENTAGE
                || tt.current() == CssTokenType.TT_DIMENSION)) {
            if (tt.current() == CssTokenType.TT_DIMENSION &&
                    (i != 0 || !UnitConverter.DEGREES.equals(tt.currentStringNonNull()))) {
                throw tt.createParseException("CssColor: hsb found unsupported dimension.");
            }
            if (tt.current() == CssTokenType.TT_PERCENTAGE) {
                sizes[i++] = CssSize.of(tt.currentNumberNonNull().doubleValue(), UnitConverter.PERCENTAGE);
            } else {
                sizes[i++] = CssSize.of(tt.currentNumberNonNull().doubleValue(), UnitConverter.DEFAULT);
            }
            if (tt.next() != ',') {
                tt.pushBack();
            }
        }

        if (i == 0) {
            color = ShsbaCssColor.BLACK;
            tt.pushBack();
        } else if (i == 3) {
            color = new ShsbaCssColor(sizes[0], sizes[1], sizes[2], CssSize.ONE);
            tt.pushBack();
        } else if (i == 4) {
            color = new ShsbaCssColor(sizes[0], sizes[1], sizes[2], sizes[3]);
        } else {
            throw tt.createParseException("CssColor: hsb values expected.");
        }
        return color;
    }

    @NonNull
    private CssColor parseSrgbaColor(@NonNull CssTokenizer tt) throws IOException, ParseException {
        int i = 0;
        CssColor color;
        CssSize[] sizes = new CssSize[4];
        while (i < 4 && (tt.next() == CssTokenType.TT_NUMBER || tt.current() == CssTokenType.TT_PERCENTAGE)) {
            if (tt.current() == CssTokenType.TT_PERCENTAGE) {
                sizes[i++] = CssSize.of(tt.currentNumberNonNull().doubleValue(), UnitConverter.PERCENTAGE);
            } else {
                sizes[i++] = CssSize.of(tt.currentNumberNonNull().doubleValue(), UnitConverter.DEFAULT);
            }
            if (tt.next() != ',') {
                tt.pushBack();
            }
        }

        if (i == 0) {
            color = SrgbaCssColor.BLACK;
            tt.pushBack();
        } else if (i == 3) {
            color = new SrgbaCssColor(sizes[0], sizes[1], sizes[2], CssSize.ONE);
            tt.pushBack();
        } else if (i == 4) {
            color = new SrgbaCssColor(sizes[0], sizes[1], sizes[2], sizes[3]);
        } else {
            throw tt.createParseException("CssColor: rgb values expected.");
        }
        return color;
    }

    @Override
    public <TT extends CssColor> void produceTokens(@Nullable TT value, @Nullable IdSupplier
            idSupplier, @NonNull Consumer<CssToken> out) {
        if (value == null) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
            return;
        }
        StreamCssTokenizer tt = new StreamCssTokenizer(value.getName(), null);
        try {
            while (tt.nextNoSkip() != CssTokenType.TT_EOF) {
                out.accept(new CssToken(tt.current(), tt.currentNumber(), tt.currentString()));
            }
        } catch (IOException e) {
            throw new AssertionError("unexpected io exception", e);
        }
    }
}
