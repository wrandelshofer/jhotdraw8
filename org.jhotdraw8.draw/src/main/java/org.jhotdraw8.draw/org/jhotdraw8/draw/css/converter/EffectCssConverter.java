/*
 * @(#)CssEffectConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Shadow;
import javafx.scene.paint.Color;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.ColorCssConverter;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.converter.KebabCaseEnumCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * CssEffectConverter.
 * <p>
 * Parses the following EBNF:
 * </p>
 * <pre>
 * Effect = "none" | ( Blend | Bloom | BoxBlur | ColorAdjust | DropShadow | GaussianBlur | Glow | InnerShadow | Shadow ) , { Effect };
 * Blend = "blend(" , [
 *                 blendType
 *              ] , ")";
 * Bloom = "bloom(" , [
 *                 threshold
 *              ] , ")";
 * BoxBlur = "box-blur(" , [
 *                 width, Sep, height, Sep, iterations
 *              ] , ")";
 * ColorAdjust = "color-adjust(" , [
 *                 "hue", S, hue, Sep,
 *                 "saturation" , S , saturation, Sep,
 *                 "brightness", S, brightness, Sep,
 *                 "contrast", S, contrast
 *              ] , ")";
 * DropShadow = "drop-shadow(" , [
 *                 blurType , Sep , color , Sep ,
 *                 radius , Sep ,  spread , Sep ,  xOffset , Sep ,  yOffset
 *              ] , ")";
 * GaussianBlur = "gaussian-blur(" , [
 *                 radius
 *              ] , ")";
 * Glow = "glow(" , [
 *                 level
 *              ] , ")";
 * InnerShadow = "inner-shadow(" , [
 *                 blurType , Sep , color , Sep ,
 *                 radius , Sep, choke , Sep ,  xOffset , Sep ,  yOffset
 *               ] , ")";
 * Shadow = "shadow(" , [
 *                 blurType , Sep , color , Sep ,
 *                 radius
 *               ] , ")";
 *
 * Sep         = ( S , { S } | { S } , "," , { S } ) ;
 * S           = (* white space character *)
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>JavaFX CSS Reference Guide</dt>
 *     <dd><a href="https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html">oracle.com</a></dd>
 * </dl>
 *
 */
public class EffectCssConverter implements CssConverter<Effect> {
    private static final PersistentList<String> examples = VectorList.of(
            "blend(hard-light)",
            "bloom(10%)",
            "box-blur(10,3,3)",
            "color-adjust(hue -5%,saturation 20%,brightness 10%,contrast 10%)",
            "drop-shadow(three-pass-box,gray,5,0,3,3)",
            "gaussian-blur(10)",
            "inner-shadow(three-pass-box,gray,10,0,4,4)",
            "shadow(three-pass-box,black,10)"

    );

    private static final String BLEND = "blend";
    private static final String BLOOM = "bloom";
    private static final String BOX_BLUR = "box-blur";
    private static final String COLOR_ADJUST = "color-adjust";
    private static final String DROP_SHADOW = "drop-shadow";
    private static final String GAUSSIAN_BLUR = "gaussian-blur";
    private static final String GLOW = "glow";
    private static final String INNER_SHADOW = "inner-shadow";
    private static final String SHADOW = "shadow";

    private final KebabCaseEnumCssConverter<BlurType> blurTypeConverter = new KebabCaseEnumCssConverter<>(BlurType.class, false);
    private final KebabCaseEnumCssConverter<BlendMode> blendModeConverter = new KebabCaseEnumCssConverter<>(BlendMode.class, false);
    private final ColorCssConverter colorConverter = new ColorCssConverter(false);

    public EffectCssConverter() {
    }

    @Override
    public @Nullable Effect getDefaultValue() {
        return null;
    }

    @Override
    public PersistentList<String> getExamples() {
        return examples;
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Effect⟩: none｜（⟨Blend⟩｜⟨Bloom⟩｜⟨BoxBlur⟩｜⟨ColorAdjust⟩｜⟨DropShadow⟩｜⟨GaussianBlur⟩｜ ⟨InnerShadow⟩）｛, ⟨Effect⟩｝"
                + "\nFormat of ⟨Blend⟩: blend(⟨BlendMode⟩)"
                + "\nFormat of ⟨Bloom⟩: bloom(⟨luminosity⟩%)"
                + "\nFormat of ⟨BoxBlur⟩: box-blur(⟨width⟩,⟨height⟩,⟨iterations⟩)"
                + "\nFormat of ⟨ColorAdjust⟩: color-adjust(hue ±⟨h⟩%, saturation ±⟨s⟩%, brightness ±⟨b⟩%, contrast ±⟨c⟩%)"
                + "\nFormat of ⟨DropShadow⟩: drop-shadow(⟨BlurType⟩,⟨Color⟩,⟨radius⟩,⟨spread⟩,⟨xoffset⟩,⟨yoffset⟩)"
                + "\nFormat of ⟨GaussianBlur⟩: gaussian-blur(⟨radius⟩)"
                + "\nFormat of ⟨InnerShadow⟩: inner-shadow(⟨BlurType⟩,⟨Color⟩,⟨radius⟩,⟨choke⟩,⟨xoffset⟩,⟨yoffset⟩)"
                + "\nFormat of ⟨Shadow⟩: shadow(⟨BlurType⟩,⟨Color⟩,⟨radius⟩)"
                + "\n" + blendModeConverter.getHelpText()
                + "\n" + blurTypeConverter.getHelpText()
                + "\n" + colorConverter.getHelpText();
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public @Nullable Effect parse(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.nextIsIdentNone()) {
            return null;
        }
        tt.pushBack();
        return parseEffect(tt);
    }

    private Effect parseBlend(CssTokenizer tt) throws ParseException, IOException {
        BlendMode mode = BlendMode.SRC_OVER;
        if (tt.next() == CssTokenType.TT_IDENT) {
            tt.pushBack();
            mode = blendModeConverter.parse(tt, null);
        }
        if (tt.next() != ')') {
            throw tt.createParseException("CSS Effect: ')' expected.");
        }
        return new Blend(mode);
    }

    private Effect parseBloom(CssTokenizer tt) throws ParseException, IOException {
        double threshold = 0.3;
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            threshold = tt.currentNumberNonNull().doubleValue();
            break;
        case CssTokenType.TT_PERCENTAGE:
            threshold = tt.currentNumberNonNull().doubleValue() / 100;
            break;
        default:
            tt.pushBack();
        }
        if (tt.next() != ')') {
            throw tt.createParseException("CSS Effect: ')' expected.");
        }
        return new Bloom(Math.clamp(threshold, 0, 1));
    }

    private Effect parseBoxBlur(CssTokenizer tt) throws ParseException, IOException {
        double width = 5;
        double height = 5;
        int iterations = 1;
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            double value = tt.currentNumberNonNull().doubleValue();
            width = Math.clamp(value, 0, 255);
            break;
        default:
            tt.pushBack();
        }
        if (tt.next() != ',') {
            tt.pushBack();
        }
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            double value = tt.currentNumberNonNull().doubleValue();
            height = Math.clamp(value, 0, 255);
            break;
        default:
            tt.pushBack();
        }
        if (tt.next() != ',') {
            tt.pushBack();
        }
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            int value = tt.currentNumberNonNull().intValue();
            iterations = Math.clamp(value, 0, 3);
            break;
        default:
            tt.pushBack();
        }
        if (tt.next() != ')') {
            throw tt.createParseException("CSS Effect: ')' expected.");
        }
        return new BoxBlur(width, height, iterations);
    }

    private Effect parseColorAdjust(CssTokenizer tt) throws ParseException, IOException {
        double hue = 0.0;
        double saturation = 0.0;
        double brightness = 0.0;
        double contrast = 0.0;
        while (tt.next() == CssTokenType.TT_IDENT) {
            String ident = tt.currentStringNonNull();
            double adjust = 0.0;
            switch (tt.next()) {
            case CssTokenType.TT_NUMBER:
                adjust = tt.currentNumberNonNull().doubleValue();
                break;
            case CssTokenType.TT_PERCENTAGE:
                adjust = tt.currentNumberNonNull().doubleValue() / 100;
                break;
            default:
                tt.pushBack();
            }
            adjust = Math.clamp(adjust, 0, 1);
            switch (ident) {
            case "hue":
                hue = adjust;
                break;
            case "saturation":
                saturation = adjust;
                break;
            case "brightness":
                brightness = adjust;
                break;
            case "contrast":
                contrast = adjust;
                break;
            default:
                throw tt.createParseException("CSS \"hue\", \"saturation\", \"brightness\", or \"contrast\" expected.");
            }
            if (tt.next() != ',') {
                tt.pushBack();
            }
        }
        if (tt.current() != ')') {
            throw tt.createParseException("CSS Effect: ')' expected.");
        }
        return new ColorAdjust(hue, saturation, brightness, contrast);
    }

    private Effect parseDropShadow(CssTokenizer tt) throws ParseException, IOException {
        return parseDropShadowOrInnerShadow(tt, true);
    }

    private Effect parseDropShadowOrInnerShadow(CssTokenizer tt, boolean isDropShadow) throws ParseException, IOException {
        String func = isDropShadow ? DROP_SHADOW : INNER_SHADOW;
        BlurType blurType = BlurType.GAUSSIAN;
        Color color = new Color(0, 0, 0, 0.25);
        double radius = 10.0;
        double spreadOrChocke = 0.0;
        double offsetX = 0.0;
        double offsetY = 4.0;
        Effect input = null;

        if (tt.next() != ')') {
            if (tt.current() != CssTokenType.TT_IDENT) {
                throw tt.createParseException("CSS Effect: " + func + "(<blur-type>,color,radius,spread,offset-x,offset-y) expected.");
            }
            tt.pushBack();
            blurType = blurTypeConverter.parseNonNull(tt, null);

            if (tt.next() != ',') {
                tt.pushBack();
            }
            if (tt.next() == CssTokenType.TT_HASH) {
                color = Color.web('#' + tt.currentString());
            } else if (tt.current() == CssTokenType.TT_IDENT) {
                color = Color.web(tt.currentStringNonNull());
            } else if (tt.current() == CssTokenType.TT_FUNCTION) {
                tt.pushBack();
                CssColor colorOrNull = colorConverter.parse(tt, null);
                color = colorOrNull == null ? Color.BLACK : colorOrNull.getColor();
            } else {
                throw tt.createParseException("CSS Effect: " + func + "(" + blurType.toString().toLowerCase().replace('_', '-') + ",  <color> expected.");
            }
            if (tt.next() != ',') {
                tt.pushBack();
            }
            if (tt.next() != CssTokenType.TT_NUMBER) {
                throw tt.createParseException("CSS Effect: radius number expected.");
            }
            radius = tt.currentNumberNonNull().doubleValue();

            if (tt.next() != ',') {
                tt.pushBack();
            }
            spreadOrChocke = switch (tt.next()) {
                case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().doubleValue();
                case CssTokenType.TT_PERCENTAGE -> tt.currentNumberNonNull().doubleValue() / 100.0;
                default -> throw tt.createParseException("CSS Shadow-Effect: spread or chocke number expected.");
            };
            if (tt.next() != ',') {
                tt.pushBack();
            }
            if (tt.next() != CssTokenType.TT_NUMBER) {
                throw tt.createParseException("CSS Shadow-Effect: offset-x number expected.");
            }
            offsetX = tt.currentNumberNonNull().doubleValue();
            if (tt.next() != ',') {
                tt.pushBack();
            }
            if (tt.next() != CssTokenType.TT_NUMBER) {
                throw tt.createParseException("CSS Shadow-Effect: offset-y number expected.");
            }
            offsetY = tt.currentNumberNonNull().doubleValue();
            if (tt.next() != ',') {
                tt.pushBack();
            } else {
                input = parseEffect(tt);
            }
            if (tt.next() != ')') {
                throw tt.createParseException("CSS Shadow-Effect: ')' expected.");
            }
        }

        final Effect effect;
        if (isDropShadow) {
            DropShadow dropShadow = new DropShadow(blurType, color, Math.clamp(radius, 0, 127), spreadOrChocke, offsetX, offsetY);
            if (input != null) {
                dropShadow.setInput(input);
            }
            effect = dropShadow;
        } else {
            InnerShadow innerhShadow = new InnerShadow(blurType, color, Math.clamp(radius, 0, 127), spreadOrChocke, offsetX, offsetY);
            if (input != null) {
                innerhShadow.setInput(input);
            }
            effect = innerhShadow;
        }
        return effect;
    }

    private @Nullable Effect parseEffect(CssTokenizer tt) throws ParseException, IOException {
        Effect first = null;
        Effect previous = null;
        while (tt.next() == CssTokenType.TT_FUNCTION) {

            Effect current = switch (tt.currentStringNonNull()) {
                case BLEND -> parseBlend(tt);
                case BLOOM -> parseBloom(tt);
                case BOX_BLUR -> parseBoxBlur(tt);
                case COLOR_ADJUST -> parseColorAdjust(tt);
                case DROP_SHADOW -> parseDropShadow(tt);
                case GAUSSIAN_BLUR -> parseGaussianBlur(tt);
                case GLOW -> parseGlow(tt);
                case INNER_SHADOW -> parseInnerShadow(tt);
                case SHADOW -> parseShadow(tt);
                default ->
                        throw tt.createParseException("CSS Effect: \"" + BLEND + ", " + DROP_SHADOW + "(\" or \"" + INNER_SHADOW + "(\"  expected.");
            };
            if (first == null) {
                first = previous = current;
            } else {
                try {
                    previous.getClass().getDeclaredMethod("setInput", Effect.class).invoke(previous, current);
                } catch (NoSuchMethodException | SecurityException |
                         IllegalAccessException | IllegalArgumentException |
                         InvocationTargetException ex) {
                    ParseException pe = tt.createParseException("CSS Effect: can not combine effects.");
                    pe.initCause(ex);
                    throw pe;
                }
                previous = current;
            }

            if (tt.next() != ',') {
                tt.pushBack();
            }
        }
        return first;
    }

    private Effect parseGaussianBlur(CssTokenizer tt) throws ParseException, IOException {
        double radius = 5;
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            double value = tt.currentNumberNonNull().doubleValue();
            radius = Math.clamp(value, 0, 63);
            break;
        default:
            tt.pushBack();
        }
        if (tt.next() != ')') {
            throw tt.createParseException("CSS Effect: ')' expected.");
        }
        return new GaussianBlur(radius);
    }

    private Effect parseGlow(CssTokenizer tt) throws ParseException, IOException {
        double level = 0.3;
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            level = tt.currentNumberNonNull().doubleValue();
            break;
        case CssTokenType.TT_PERCENTAGE:
            level = tt.currentNumberNonNull().doubleValue() / 100;
            break;
        default:
            tt.pushBack();
        }
        if (tt.next() != ')') {
            throw tt.createParseException("CSS Effect: ')' expected.");
        }
        return new Glow(Math.clamp(level, 0, 1));
    }

    private Effect parseInnerShadow(CssTokenizer tt) throws ParseException, IOException {
        return parseDropShadowOrInnerShadow(tt, false);
    }

    private Effect parseShadow(CssTokenizer tt) throws ParseException, IOException {
        String func = SHADOW;
        BlurType blurType = BlurType.GAUSSIAN;
        Color color = new Color(0, 0, 0, 0.75);
        double radius = 10.0;

        if (tt.next() != ')') {
            if (tt.current() != CssTokenType.TT_IDENT) {
                throw tt.createParseException("CSS Effect: " + func + "(<blur-type>,color,radius,spread,offset-x,offset-y) expected.");
            }
            tt.pushBack();
            blurType = blurTypeConverter.parseNonNull(tt, null);

            if (tt.next() != ',') {
                tt.pushBack();
            }
            if (tt.next() == CssTokenType.TT_HASH) {
                color = Color.web('#' + tt.currentStringNonNull());
            } else if (tt.current() == CssTokenType.TT_IDENT) {
                color = Color.web(tt.currentStringNonNull());
            } else if (tt.current() == CssTokenType.TT_FUNCTION) {
                tt.pushBack();
                CssColor colorOrNull = colorConverter.parse(tt, null);
                color = colorOrNull == null ? Color.BLACK : colorOrNull.getColor();
            } else {
                throw tt.createParseException("CSS Effect: " + func + "(" + blurType.toString().toLowerCase().replace('_', '-') + ",  <color> expected.");
            }
            if (tt.next() != ',') {
                tt.pushBack();
            }
            if (tt.next() != CssTokenType.TT_NUMBER) {
                throw tt.createParseException("CSS Effect: radius number expected.");
            }
            radius = tt.currentNumberNonNull().doubleValue();

            if (tt.next() != ')') {
                throw tt.createParseException("CSS Effect: ')' expected.");
            }
        }
        return new Shadow(blurType, color, Math.clamp(radius, 0, 127));
    }

    @Override
    public <TT extends Effect> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        Deque<Effect> effects = new ArrayDeque<>();
        for (Effect chainedEffect = value; chainedEffect != null; ) {
            effects.add(chainedEffect);
            try {
                Object inputEffect = chainedEffect.getClass().getDeclaredMethod("getInput", Effect.class).invoke(chainedEffect);
                if (inputEffect instanceof Effect) {
                    chainedEffect = (Effect) inputEffect;
                    effects.addFirst(chainedEffect);
                }
            } catch (NoSuchMethodException | SecurityException |
                     IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException ex) {
                chainedEffect = null;
            }
        }

        boolean first = true;
        for (Effect eff : effects) {
            if (first) {
                first = false;
            } else {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
            }

            switch (eff) {
                case Blend fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, BLEND));
                    blendModeConverter.produceTokens(fx.getMode(), idSupplier, out);
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    //FIXME
            /* if (fx.getInput() != null) {
                out.accept(new CssToken(CssTokenType.,", ");
                toString(out, idFactory, fx.getInput());
            }*/
                }
                case Bloom fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, BLOOM));
                    out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, fx.getThreshold() * 100));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_COMMA));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                }
                case BoxBlur fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, BOX_BLUR));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getWidth()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getHeight()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getIterations()));
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_COMMA));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                }
                case ColorAdjust fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, COLOR_ADJUST));
                    boolean needComma = false;
                    final double hue = fx.getHue();
                    final double saturation = fx.getSaturation();
                    final double brightness = fx.getBrightness();
                    final double contrast = fx.getContrast();
                    boolean all = hue == 0 && saturation == 0 && brightness == 0 && contrast == 0;
                    if (hue != 0 || all) {
                        out.accept(new CssToken(CssTokenType.TT_IDENT, "hue"));
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        if (hue > 0) {
                            out.accept(new CssToken(CssTokenType.TT_PLUS));
                        }
                        out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, hue * 100));
                        needComma = true;
                    }
                    if (saturation != 0 || all) {
                        if (needComma) {
                            out.accept(new CssToken(CssTokenType.TT_COMMA));
                        }
                        out.accept(new CssToken(CssTokenType.TT_IDENT, "saturation"));
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        if (saturation > 0) {
                            out.accept(new CssToken(CssTokenType.TT_PLUS));
                        }
                        out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, saturation * 100));
                        needComma = true;
                    }
                    if (brightness != 0 || all) {
                        if (needComma) {
                            out.accept(new CssToken(CssTokenType.TT_COMMA));
                        }
                        out.accept(new CssToken(CssTokenType.TT_IDENT, "brightness"));
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        if (brightness > 0) {
                            out.accept(new CssToken(CssTokenType.TT_PLUS));
                        }
                        out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, brightness * 100));
                        needComma = true;
                    }
                    if (contrast != 0 || all) {
                        if (needComma) {
                            out.accept(new CssToken(CssTokenType.TT_COMMA));
                        }
                        out.accept(new CssToken(CssTokenType.TT_IDENT, "contrast"));
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        if (contrast > 0) {
                            out.accept(new CssToken(CssTokenType.TT_PLUS));
                        }
                        out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, contrast * 100));
                    }
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                }
                case DropShadow fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, DROP_SHADOW));
                    blurTypeConverter.produceTokens(fx.getBlurType(), idSupplier, out);
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    colorConverter.produceTokens(new CssColor(fx.getColor()), idSupplier, out);
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getRadius()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, fx.getSpread() * 100));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getOffsetX()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getOffsetY()));
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_COMMA));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                }
                case GaussianBlur fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, GAUSSIAN_BLUR));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getRadius()));
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_COMMA));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                }
                case Glow fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, GLOW));
                    out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, fx.getLevel() * 100));
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                }
                case InnerShadow fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, INNER_SHADOW));
                    blurTypeConverter.produceTokens(fx.getBlurType(), idSupplier, out);
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    colorConverter.produceTokens(new CssColor(fx.getColor()), idSupplier, out);
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getRadius()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, fx.getChoke() * 100));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getOffsetX()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getOffsetY()));
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                }
                case Shadow fx -> {
                    out.accept(new CssToken(CssTokenType.TT_FUNCTION, SHADOW));
                    blurTypeConverter.produceTokens(fx.getBlurType(), idSupplier, out);
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    colorConverter.produceTokens(new CssColor(fx.getColor()), idSupplier, out);
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, fx.getRadius()));
                    out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
                    if (fx.getInput() != null) {
                        out.accept(new CssToken(CssTokenType.TT_S, " "));
                        produceTokens(fx.getInput(), idSupplier, out);
                    }
                }
                case null, default -> out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
            }
        }
    }
}
