/*
 * @(#)CssFontConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssFont;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * CssFontConverter.
 * <p>
 * Parses the following EBNF from the
 * <a href="https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html">JavaFX
 * CSS Reference Guide</a>.
 * </p>
 * <pre>
 * CssFont := [FontStyle] [FontWeight] FontSize FontFamily ;
 * FontStyle := normal|italic|oblique;
 * FontWeight := normal|bold|bolder|lighter|100|200|300|400|500|600|700|800|900;
 * FontSize := Size;
 * FontFamily := Word|Quoted;
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class FontCssConverter extends AbstractCssConverter<CssFont> {


    public static final String ITALIC_STYLE = "italic";
    public static final String BOLD_WEIGHT = "bold";
    public static final String NORMAL_STYLE = "normal";
    public static final String NORMAL_WEIGHT = "normal";
    public static final String OBLIQUE_STYLE = "oblique";
    public static final String BOLDER_WEIGHT = "bolder";
    public static final String LIGHTER_WEIGHT = "lighter";

    public FontCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public <TT extends CssFont> void produceTokensNonNull(@NonNull TT font, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        CssSize fontSize = font.getSize();
        String fontFamily = font.getFamily();
        final FontPosture posture = font.getPosture();

        boolean needsSpace = false;

        if (posture != null) {
            switch (font.getPosture()) {
            case ITALIC:
                out.accept(new CssToken(CssTokenType.TT_IDENT, ITALIC_STYLE));
                needsSpace = true;
                break;
            case REGULAR:
                break;
            default:
                throw new RuntimeException("Unknown fontPosture:" + font.getPosture());
            }
        }
        final FontWeight weight = font.getWeight();
        if (weight != null) {
            switch (weight) {
            case NORMAL:
                break;
            case BOLD:
                if (needsSpace) {
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                out.accept(new CssToken(CssTokenType.TT_IDENT, BOLD_WEIGHT));
                needsSpace = true;
                break;
            default:
                if (needsSpace) {
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                out.accept(new CssToken(CssTokenType.TT_NUMBER, weight.getWeight()));
                needsSpace = true;
                break;
            }
        }
        if (needsSpace) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, fontSize.getValue(), fontSize.getUnits()));
        out.accept(new CssToken(CssTokenType.TT_S, " "));
        if (fontFamily.contains("'") || fontFamily.contains("\"")) {
            out.accept(new CssToken(CssTokenType.TT_STRING, fontFamily));
        } else if (fontFamily.contains(" ")) {
            boolean first = true;
            for (String part : fontFamily.split(" +")) {
                if (first) {
                    first = false;
                } else {
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                out.accept(new CssToken(CssTokenType.TT_IDENT, part));
            }
        } else {
            out.accept(new CssToken(CssTokenType.TT_IDENT, fontFamily));
        }
    }

    @Override
    public @NonNull CssFont parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        FontPosture fontPosture = FontPosture.REGULAR;
        FontWeight fontWeight = FontWeight.NORMAL;
        CssSize fontSize = CssSize.of(12.0);
        StringBuilder fontFamily = new StringBuilder("System");

        // parse FontStyle
        if (tt.next() == CssTokenType.TT_IDENT) {
            switch (tt.currentStringNonNull().toLowerCase()) {
            case NORMAL_STYLE:
                fontPosture = FontPosture.REGULAR;
                break;
            case ITALIC_STYLE:
            case OBLIQUE_STYLE:
                fontPosture = FontPosture.ITALIC;
                break;
            default:
                tt.pushBack();
                break;
            }
        } else {
            tt.pushBack();
        }

        // parse FontWeight
        boolean fontWeightConsumed = false;
        if (tt.next() == CssTokenType.TT_IDENT) {
            switch (tt.currentStringNonNull().toLowerCase()) {
            case NORMAL_WEIGHT:
                fontWeight = FontWeight.NORMAL;
                fontWeightConsumed = true;
                break;
            case BOLD_WEIGHT:
                fontWeight = FontWeight.BOLD;
                fontWeightConsumed = true;
                break;
            case BOLDER_WEIGHT:
                // FIXME weight should be relative to parent font
                fontWeight = FontWeight.BOLD;
                fontWeightConsumed = true;
                break;
            case LIGHTER_WEIGHT:
                // FIXME weight should be relative to parent font
                fontWeight = FontWeight.LIGHT;
                fontWeightConsumed = true;
                break;
            default:
                tt.pushBack();
                break;
            }
        } else {
            tt.pushBack();
        }

        double fontWeightOrFontSize = 0.0;
        boolean fontWeightOrFontSizeConsumed = false;
        if (!fontWeightConsumed) {
            if (tt.next() == CssTokenType.TT_NUMBER) {
                fontWeightOrFontSize = tt.currentNumberNonNull().doubleValue();
                fontWeightOrFontSizeConsumed = true;
            } else {
                tt.pushBack();
            }
        }

        // parse FontSize
        if (tt.next() == CssTokenType.TT_DIMENSION || tt.current() == CssTokenType.TT_NUMBER) {
            if (tt.current() == CssTokenType.TT_DIMENSION) {
                fontSize = CssSize.of(tt.currentNumberNonNull().doubleValue(), tt.currentStringNonNull());
            } else if (tt.current() == CssTokenType.TT_NUMBER) {
                fontSize = CssSize.of(tt.currentNumberNonNull().doubleValue());
            }
            if (fontWeightOrFontSizeConsumed) {
                fontWeight = switch ((int) fontWeightOrFontSize) {
                    case 100 -> FontWeight.THIN;
                    case 200 -> FontWeight.EXTRA_LIGHT;
                    case 300 -> FontWeight.LIGHT;
                    case 400 -> FontWeight.NORMAL;
                    case 500 -> FontWeight.MEDIUM;
                    case 600 -> FontWeight.SEMI_BOLD;
                    case 700 -> FontWeight.BOLD;
                    case 800 -> FontWeight.EXTRA_BOLD;
                    case 900 -> FontWeight.BLACK;
                    default ->
                            throw new ParseException("⟨Font⟩: illegal font weight " + fontWeightOrFontSize, tt.getStartPosition());
                };
            }

        } else if (fontWeightOrFontSizeConsumed) {
            tt.pushBack();
            fontSize = CssSize.of(fontWeightOrFontSize);
        } else {
            tt.pushBack();
        }

        if (tt.next() == CssTokenType.TT_IDENT) {
            fontFamily = new StringBuilder(tt.currentString());
            while (tt.next() == CssTokenType.TT_IDENT) {
                fontFamily.append(" ").append(tt.currentString());
            }
        } else if (tt.current() == CssTokenType.TT_STRING) {
            fontFamily = new StringBuilder(tt.currentString());
        } else {
            throw new ParseException("⟨Font⟩: ⟨FontFamily⟩ expected", tt.getStartPosition());
        }
        CssFont font = CssFont.font(fontFamily.toString(), fontWeight, fontPosture, fontSize);
        if (font == null) {
            font = CssFont.font(null, fontWeight, fontPosture, fontSize);
        }
        return font;
    }


    @Override
    public String getHelpText() {
        return """
               Format of ⟨Font⟩: ［⟨FontStyle⟩］［⟨FontWeight⟩］ ⟨FontSize⟩ ⟨FontFamily⟩
                 with ⟨FontStyle⟩: normal｜italic｜oblique
                 with ⟨FontWeight⟩: normal｜bold｜bolder｜lighter｜100｜200｜300｜400｜500｜600｜700｜800｜900
                 with ⟨FontSize⟩: size
                 with ⟨FontFamily⟩: ⟨identifier⟩｜⟨string⟩"""
                ;
    }
}
