/*
 * @(#)CssColorConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import javafx.scene.paint.Color;
import org.jhotdraw8.base.converter.FloatConverter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.color.CssColorSpaces;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.ParametricHlsColorSpace;
import org.jhotdraw8.color.ParametricHsvColorSpace;
import org.jhotdraw8.color.ParametricScaledColorSpace;
import org.jhotdraw8.color.SrgbColorSpace;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.NamedCssColor;
import org.jhotdraw8.css.value.ShsbaCssColor;
import org.jhotdraw8.css.value.SrgbaCssColor;
import org.jhotdraw8.css.value.SystemCssColor;
import org.jhotdraw8.css.value.Uint4HexSrgbaCssColor;
import org.jhotdraw8.css.value.Uint8HexSrgbaCssColor;
import org.jhotdraw8.css.value.UnitConverter;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
 *     <dt>CSS Color Module Level 4. 4. Representing Colors: the &lt;color&gt; type.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-type">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. 4. Representing Colors: the &lt;color&gt; type.  4.1 The &lt;color&gt; syntax.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-syntax">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. 5. sRGB Colors.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#rgb-functions">w3.org</a></dd>
 *
 *     <dt>JavaFX 20. JavaFX CSS Reference Guide. Types. {@literal <color>}.</dt>
 *     <dd><a href="https://openjfx.io/javadoc/20/javafx.graphics/javafx/scene/doc-files/cssref.html#typecolor">openjfx.io</a></dd>
 * </dl>
 *
 */
public class ColorCssConverter implements CssConverter<CssColor> {
    /**
     * Configure the number convert so that it preserves 32-bit float values,
     * which have a precision of 8 decimal digits.
     */
    private final static FloatConverter number = new FloatConverter();

    final boolean nullable;

    public ColorCssConverter() {
        this(false);
    }

    public ColorCssConverter(boolean nullable) {
        this.nullable = nullable;
    }

    private String colorParamToString(List<CssSize> params) {
        StringBuilder buf = new StringBuilder(16);
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                if (i == 3) {
                    buf.append(" / ");
                } else {
                    buf.append(' ');
                }
            }
            buf.append(number.toString((float) params.get(i).getValue()));
            buf.append(params.get(i).getUnits());
        }
        return buf.toString();
    }

    @Override
    public @Nullable CssColor getDefaultValue() {
        return null;
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Color⟩: " + "⟨name⟩｜#⟨hex⟩｜rgb(⟨r⟩,⟨g⟩,⟨b⟩)｜rgba(⟨r⟩,⟨g⟩,⟨b⟩,⟨a⟩)｜hsb(⟨h⟩,⟨s⟩,⟨b⟩)｜hsba(⟨h⟩,⟨s⟩,⟨b⟩,⟨a⟩)";
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public @Nullable CssColor parse(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
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
                yield switch (tt.currentStringNonNull().toLowerCase()) {
                    case "rgb", "rgba" -> parseRgbFunction(tt);
                    case "hsl", "hsla" -> parseHslFunction(tt);
                    case "hsb", "hsba" -> parseHsbFunction(tt);
                    case "hwb", "oklch", "oklab", "lab" -> null;
                    case "color" -> parseColorFunction(tt);
                    default ->
                            throw tt.createParseException("Could not convert a string to a CssColor because the function " + tt.currentStringNonNull() + "() is not supported.");
                };
            }
            default ->
                    throw tt.createParseException("Could not convert a string to a CssColor because unexpected " + tt.getToken() + " was found.");
        };
    }

    private @Nullable CssColor parseColorFunction(CssTokenizer tt) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "Could not convert a string to a CssColor because unexpected " + tt.getToken() + " was found.");
        String functionName = tt.currentStringNonNull();

        String colorSpaceParam = "srgb";
        if ("color".equals(functionName)) {
            if (tt.next() == CssTokenType.TT_IDENT) {
                colorSpaceParam = tt.currentStringNonNull().toLowerCase();

            } else {
                tt.pushBack();
            }
        }
        NamedColorSpace cs = CssColorSpaces.COLOR_SPACES.get(colorSpaceParam);
        if (cs == null) {
            throw tt.createParseException("Could not convert a string to a CssColor because the color space=\"" + colorSpaceParam + "\" is not supported.");
        }


        List<CssSize> params = parseParams(tt, cs);
        if (tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            throw tt.createParseException("Could not convert a string to a CssColor because the closing bracket ')' is missing.");
        }
        float[] rgb = clampColors(params);
        return new CssColor(
                "color("
                        + colorSpaceParam + " "
                        + colorParamToString(params)
                        + ")",
                new Color(rgb[0], rgb[1], rgb[2], params.size() == 4 ? Math.clamp(params.get(3).getValue(), 0, 1) : 1.0));
    }

    private static List<CssSize> parseParams(CssTokenizer tt, NamedColorSpace cs) throws IOException, ParseException {
        List<CssSize> params = new ArrayList<>();
        while (tt.next() != CssTokenType.TT_EOF && tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            switch (tt.current()) {
                case CssTokenType.TT_DIMENSION, CssTokenType.TT_PERCENTAGE -> {
                    if (params.size() > 3) {
                        throw tt.createParseException("Could not convert a string to a CssColor because the function has too many parameters.");
                    }
                    params.add(CssSize.of(tt.currentNumberNonNull().doubleValue(), tt.currentStringNonNull()));
                }
                case CssTokenType.TT_NUMBER -> {
                    if (params.size() > 3) {
                        throw tt.createParseException("Could not convert a string to a CssColor because the function has too many parameters.");
                    }
                    params.add(CssSize.of(tt.currentNumberNonNull().doubleValue()));
                }
                case ',', '/' -> {
                }
                case CssTokenType.TT_IDENT -> {
                    switch (tt.currentStringNonNull()) {
                        case "none" -> {
                            if (params.size() > 3) {
                                throw tt.createParseException("Could not convert a string to a CssColor because the function has too many parameters.");
                            }
                            params.add(CssSize.ZERO);
                        }
                        default ->
                                throw tt.createParseException("Could not convert a string to a CssColor because the identifier 'none' or a number is expected.");
                    }
                }
            }
        }
        if (params.size() < 3) {
            throw tt.createParseException("Could not convert a string to a CssColor because the function has not enough parameters.");
        }
        return params;
    }

    private static float toDeg(CssSize size, CssTokenizer tt) throws ParseException {
        double v = size.getValue();
        return (float) switch (size.getUnits()) {
            case "", "deg" -> v;
            case "grad" -> v * 360f / 400f;
            case "rad" -> v * (360f / 2f) / Math.PI;
            case "turn" -> v * 360f;
            default ->
                    throw tt.createParseException("Could not convert a string to a color because the value " + size + " has unexpected units=\"" + size.getUnits() + "\".");
        };
    }

    private final static NamedColorSpace CSS_SRGB_COLOR_SPACE = new SrgbColorSpace();
    private final static NamedColorSpace CSS_LEGACY_SRGB_COLOR_SPACE = new ParametricScaledColorSpace("CSS sRGB*255",
            255f, CSS_SRGB_COLOR_SPACE);
    private final static NamedColorSpace CSS_HLS_COLOR_SPACE = new ParametricHlsColorSpace("CSS HSL", CSS_SRGB_COLOR_SPACE);
    private final static NamedColorSpace JAVAFX_HSB_COLOR_SPACE = new ParametricHsvColorSpace("HSB", CSS_SRGB_COLOR_SPACE);

    private CssColor parseRgbFunction(CssTokenizer tt) throws ParseException, IOException {
        List<CssSize> params = parseParams(tt, CSS_LEGACY_SRGB_COLOR_SPACE);
        float[] rgb = {
                toPercentage(params.get(0), 2.55, tt),
                toPercentage(params.get(1), 2.55, tt),
                toPercentage(params.get(2), 2.55, tt)
        };
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = rgb[i] / 255f;
        }
        var clamped = clampColors(rgb);
        float alpha;
        if (params.size() == 4) {
            float value = toPercentage(params.get(3), 0.01, tt);
            alpha = Math.clamp(value, (float) 0, (float) 1);
        } else {
            alpha = 1;
        }
        return new CssColor(
                "rgb(" + colorParamToString(params) + ")",
                new Color(clamped[0], clamped[1], clamped[2], alpha));
    }

    private CssColor parseHslFunction(CssTokenizer tt) throws ParseException, IOException {
        List<CssSize> params = parseParams(tt, CSS_HLS_COLOR_SPACE);
        float[] hls = {
                toDeg(params.get(0), tt),
                toPercentage(params.get(2), 1 / 100d, tt),
                toPercentage(params.get(1), 1 / 100d, tt)
        };
        float[] rgb = clampColors(CSS_HLS_COLOR_SPACE.toRGB(hls));
        float alpha;
        if (params.size() == 4) {
            float value = toPercentage(params.get(3), 0.01, tt);
            alpha = Math.clamp(value, (float) 0, (float) 1);
        } else {
            alpha = 1;
        }
        return new CssColor(
                "hsl(" + colorParamToString(params) + ")",
                new Color(rgb[0], rgb[1], rgb[2], alpha)
        );
    }

    private CssColor parseHsbFunction(CssTokenizer tt) throws ParseException, IOException {
        List<CssSize> params = parseParams(tt, JAVAFX_HSB_COLOR_SPACE);
        float[] hsb = {
                toDeg(params.get(0), tt),
                toPercentage(params.get(1), 0.01, tt),
                toPercentage(params.get(2), 0.01, tt)
        };
        float[] rgb = JAVAFX_HSB_COLOR_SPACE.toRGB(hsb);
        float[] clamped = clampColors(rgb);
        float alpha;
        if (params.size() == 4) {
            float value = toPercentage(params.get(3), 0.01, tt);
            alpha = Math.clamp(value, (float) 0, (float) 1);
        } else {
            alpha = 1;
        }
        return new CssColor(
                "hsb(" + colorParamToString(params) + ")",
                new Color(clamped[0], clamped[1], clamped[2], alpha));
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

    private static float toPercentage(CssSize param, double percentageConversionFactor, CssTokenizer tt) {
        return switch (param.getUnits()) {
            case "%" -> (float) (param.getValue() * percentageConversionFactor);
            default -> (float) param.getValue();
        };
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
        float[] clamped = new float[3];
        for (int i = 0; i < clamped.length; i++) {
            clamped[i] = Math.clamp(params[i], (float) 0, (float) 1);
        }
        return clamped;
    }


    private CssColor parseColorHexDigits(String hexdigits, int startpos) throws ParseException {
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
                    throw new ParseException("Could not convert a string to a CssColor because a hex digits value must have 3, 6  or 8 digits. Found " + hexdigits + " digits.", startpos);
            }
        } catch (NumberFormatException e) {
            ParseException pe = new ParseException("Could not convert a string to a CssColor because it does not contain hex digits. Found \"" + hexdigits + "\".", startpos);
            pe.initCause(e);
            throw pe;
        }
    }

    private CssColor parseHexColor(CssTokenizer tt) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_DIMENSION -> {
                // If the color is written with a leading "0xabcdef", then the
                // color value is tokenized into a TT_DIMENSION. The unit
                // contains the leading 'x' and the color value 'abcdef'.
                if (tt.currentNumberNonNull().intValue() == 0 && (tt.currentNumber() instanceof Long)
                        && tt.currentStringNonNull().startsWith("x")) {
                    yield parseColorHexDigits(tt.currentStringNonNull().substring(1), tt.getStartPosition());
                } else {
                    throw tt.createParseException("Could not convert a string to CssColor because it does not contain the expected hex digits.");
                }
            }
            case CssTokenType.TT_HASH -> parseColorHexDigits(tt.currentStringNonNull(), tt.getStartPosition());
            default ->
                    throw tt.createParseException("Could not convert a string to CssColor because it does not contain the expected hex digits.");
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

    private CssColor parseShsbaColor(CssTokenizer tt) throws IOException, ParseException {
        CssColor color;
        int i = 0;
        CssSize[] sizes = new CssSize[4];
        while (i < 4 && (tt.next() == CssTokenType.TT_NUMBER
                || tt.current() == CssTokenType.TT_PERCENTAGE
                || tt.current() == CssTokenType.TT_DIMENSION)) {
            if (tt.current() == CssTokenType.TT_DIMENSION &&
                    (i != 0 || !UnitConverter.DEGREES.equals(tt.currentStringNonNull()))) {
                throw tt.createParseException("Could not convert a string to a HSB color because of the unexpected value=" + tt.getToken() + ".");
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
            throw tt.createParseException("Could not convert a string to a HSB color because the function must have 0, 3 or 4 arguments. Found " + i + " arguments.");
        }
        return color;
    }

    private CssColor parseSrgbaColor(CssTokenizer tt) throws IOException, ParseException {
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
            throw tt.createParseException("Could not convert a string to a sRGB color because the function must have 0, 3 or 4 arguments. Found " + i + " arguments.");
        }
        return color;
    }

    @Override
    public <TT extends CssColor> void produceTokens(@Nullable TT value, @Nullable IdSupplier
            idSupplier, Consumer<CssToken> out) {
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
            throw new AssertionError("Unexpected IOException", e);
        }
    }
}
