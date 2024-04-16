/*
 * @(#)CssRadialGradientConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.paint.CycleMethod;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssRadialGradient;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * CssRadialGradientConverter.
 * <p>
 * Parses the following EBNF from the
 * <a href="https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html">JavaFX
 * CSS Reference Guide</a>.
 * </p>
 * <pre>
 * Paint := (Color|RadialGradient|RadialGradient|ImagePattern RepeatingImagePattern) ;
 * RadialGradient := "radial-gradient(", RadialGradientParameters,  Cycle, ColorStop,{"," ColorStop}")"
 * RadialGradientParameters := [ FocusAngle "," ], [ FocusDistance "," ], [ Center "," ], Radius ;
 * FocusAngle = "focus-angle", CssSize"deg";
 * FocusDistance = "focus-distance", Percentage ;
 * Center = "center", Point ;
 * Cycle = ( "repeat" | "reflect" )
 * Radius = "radius", ( Length | Percentage ) ;
 * ColorStop = Color, [" ", Offset] ;
 * Point = (Number|CssSize|Percentage), (Number|CssSize|Percentage) ;
 *
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class RadialGradientCssConverter extends AbstractCssConverter<CssRadialGradient> {

    private static final @NonNull ColorCssConverter colorConverter = new ColorCssConverter(false);
    public static final @NonNull String RADIAL_GRADIENT_FUNCTION = "radial-gradient";

    public RadialGradientCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    protected <TT extends CssRadialGradient> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_FUNCTION, RADIAL_GRADIENT_FUNCTION));
        CssRadialGradient lg = value;
        final boolean proportional = lg.isProportional();
        final double focusAngle = lg.getFocusAngle();
        final double focusDistance = lg.getFocusDistance();
        final double centerX = lg.getCenterX();
        final double centerY = lg.getCenterY();
        final double radius = lg.getRadius();
        boolean needsComma = false;
        if (focusAngle != 0.0) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, "focus-angle"));
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            out.accept(new CssToken(CssTokenType.TT_DIMENSION, focusAngle, "deg"));
            needsComma = true;
        }
        if (focusDistance != 0.0) {
            if (needsComma) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
            }
            out.accept(new CssToken(CssTokenType.TT_IDENT, "focus-distance"));
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, focusDistance * 100));
            needsComma = true;
        }
        if (centerX != 0.5 || centerY != 0.5) {
            if (needsComma) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
            }
            out.accept(new CssToken(CssTokenType.TT_IDENT, "center"));
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            if (proportional) {
                out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, centerX * 100));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
                out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, centerY * 100));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
            } else {
                out.accept(new CssToken(CssTokenType.TT_NUMBER, centerX));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, centerY));
            }
            needsComma = true;
        }
        if (needsComma) {
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
        out.accept(new CssToken(CssTokenType.TT_IDENT, "radius"));
        out.accept(new CssToken(CssTokenType.TT_S, " "));
        if (proportional) {
            out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, radius * 100));
        } else {
            out.accept(new CssToken(CssTokenType.TT_NUMBER, radius));
        }
        needsComma = true;

        {
            switch (lg.getCycleMethod()) {
            case NO_CYCLE:
                break;
            case REPEAT:
                if (needsComma) {
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                out.accept(new CssToken(CssTokenType.TT_IDENT, "repeat"));
                needsComma = true;
                break;
            case REFLECT:
                if (needsComma) {
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                out.accept(new CssToken(CssTokenType.TT_IDENT, "reflect"));
                needsComma = true;
                break;
            default:
                throw new UnsupportedOperationException("unknown cycle method " + lg.getCycleMethod());
            }
            for (CssStop stop : lg.getStops()) {
                if (needsComma) {
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                colorConverter.produceTokens(stop.color(), idSupplier, out);
                if (stop.offset() != null) {
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                    out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, stop.offset() * 100.0));
                }
                needsComma = true;
            }
        }
        out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
    }

    @Override
    public @NonNull CssRadialGradient parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "⟨RadialGradient⟩: \"radial-gradient(\"  expected");
        switch (tt.currentStringNonNull()) {
        case RADIAL_GRADIENT_FUNCTION:
            break;
        default:
            throw new ParseException("⟨RadialGradient⟩: \"radial-gradient\" expected, found: " + tt.getToken(), tt.getStartPosition());
        }

        double focusAngle = 0;
        double focusDistance = 0;
        double centerX = 0.5;
        double centerY = 0.5;
        double radius = 1;
        Boolean isProportional = null;
        while (tt.next() == CssTokenType.TT_IDENT) {
            if ("focus-angle".equals(tt.currentString())) {
                focusAngle = switch (tt.next()) {
                    case CssTokenType.TT_DIMENSION -> {
                        if (!"deg".equals(tt.currentString())) {
                            throw new ParseException("CSS RadialGradient: expected focus-angle given in degrees with unit  \"deg\", found: " + tt.getToken(), tt.getStartPosition());
                        }
                        yield tt.currentNumberNonNull().doubleValue();
                    }
                    case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().doubleValue();
                    default ->
                            throw new ParseException("CSS RadialGradient: focus-angle  expected, found: " + tt.getToken(), tt.getStartPosition());
                };

            } else if ("focus-distance".equals(tt.currentString())) {
                focusDistance = switch (tt.next()) {
                    case CssTokenType.TT_PERCENTAGE -> tt.currentNumberNonNull().doubleValue() / 100;
                    case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().doubleValue();
                    default ->
                            throw new ParseException("CSS RadialGradient: focus-distance expected, found: " + tt.getToken(), tt.getStartPosition());
                };

            } else if ("center".equals(tt.currentString())) {
                switch (tt.next()) {
                case CssTokenType.TT_PERCENTAGE:
                    if (isProportional == null) {
                        isProportional = true;
                    }
                    if (!isProportional) {
                        throw new ParseException("CSS RadialGradient: absolute value expected, found: " + tt.getToken(), tt.getStartPosition());
                    }
                    centerX = tt.currentNumberNonNull().doubleValue() / 100;
                    break;
                case CssTokenType.TT_NUMBER:
                    if (isProportional == null) {
                        isProportional = false;
                    }
                    if (isProportional) {
                        throw new ParseException("CSS RadialGradient: percentage expected, found: " + tt.getToken(), tt.getStartPosition());
                    }
                    centerX = tt.currentNumberNonNull().doubleValue();
                    break;
                default:
                    throw new ParseException("CSS RadialGradient: center x-value expected, found: " + tt.getToken(), tt.getStartPosition());
                }
                switch (tt.next()) {
                case CssTokenType.TT_PERCENTAGE:
                    if (isProportional == null) {
                        isProportional = true;
                    }
                    if (!isProportional) {
                        throw new ParseException("CSS RadialGradient: absolute value  expected, found: " + tt.getToken(), tt.getStartPosition());
                    }
                    centerY = tt.currentNumberNonNull().doubleValue() / 100;
                    break;
                case CssTokenType.TT_NUMBER:
                    if (isProportional == null) {
                        isProportional = false;
                    }
                    if (isProportional) {
                        throw new ParseException("CSS RadialGradient: percentage  expected, found: " + tt.getToken(), tt.getStartPosition());
                    }
                    centerY = tt.currentNumberNonNull().doubleValue();
                    break;
                default:
                    throw new ParseException("CSS RadialGradient: center y-value  expected, found: " + tt.getToken(), tt.getStartPosition());
                }
            } else if ("radius".equals(tt.currentString())) {
                switch (tt.next()) {
                case CssTokenType.TT_PERCENTAGE:
                    if (isProportional == null) {
                        isProportional = true;
                    }
                    if (!isProportional) {
                        throw new ParseException("CSS RadialGradient: absolute value expected, found: " + tt.getToken(), tt.getStartPosition());
                    }
                    radius = tt.currentNumberNonNull().doubleValue() / 100;
                    break;
                case CssTokenType.TT_NUMBER:
                    if (isProportional == null) {
                        isProportional = false;
                    }
                    if (isProportional) {
                        throw new ParseException("CSS RadialGradient: percentage  expected, found: " + tt.getToken(), tt.getStartPosition());
                    }
                    radius = tt.currentNumberNonNull().doubleValue();
                    break;
                default:
                    throw new ParseException("CSS RadialGradient: center x-value  expected, found: " + tt.getToken(), tt.getStartPosition());
                }
            } else {
                tt.pushBack();
                break;
            }

            if (tt.next() != ',') {
                tt.pushBack();
            }
        }
        tt.pushBack();

        CycleMethod cycleMethod = CycleMethod.NO_CYCLE;
        if (tt.next() == CssTokenType.TT_IDENT) {
            if ("repeat".equals(tt.currentString())) {
                cycleMethod = CycleMethod.REPEAT;

            } else if ("reflect".equals(tt.currentString())) {
                cycleMethod = CycleMethod.REFLECT;
            } else {
                tt.pushBack();
            }
        } else {
            tt.pushBack();
        }

        if (tt.next() != ',') {
            tt.pushBack();
        }
        List<CssStop> stops = new ArrayList<>();
        while (tt.next() != ')' && tt.current() != CssTokenType.TT_EOF) {
            tt.pushBack();
            stops.add(parseColorStop(tt, idResolver));
            if (tt.next() != ',') {
                tt.pushBack();
            }
        }

        if (tt.current() != ')') {
            throw new ParseException("CSS RadialGradient: ')'  expected, found: " + tt.getToken(), tt.getStartPosition());
        }
        if (isProportional == null) {
            isProportional = true;
        }

        return new CssRadialGradient(focusAngle, focusDistance, centerX, centerY, radius, isProportional, cycleMethod, stops.toArray(new CssStop[0]));
    }

    private @NonNull CssStop parseColorStop(@NonNull CssTokenizer tt, IdResolver idResolver) throws IOException, ParseException {
        CssColor color = colorConverter.parse(tt, idResolver);
        Double offset = null;
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            offset = tt.currentNumberNonNull().doubleValue();
            break;
        case CssTokenType.TT_PERCENTAGE:
            offset = tt.currentNumberNonNull().doubleValue() / 100.0;
            break;
        default:
            tt.pushBack();
        }
        return new CssStop(offset, color);
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨RadialGradient⟩: radial-gradient(［⟨RadialGradientParameters⟩］［,⟨Cycle⟩］,⟨ColorStop⟩｛,⟨ColorStop⟩｝)"
                + "\nFormat of ⟨RadialGradientParameters⟩: ［⟨FocusAngle⟩］［, ⟨FocusDistance⟩］［, ⟨Center⟩］, ⟨Radius⟩"
                + "\nFormat of ⟨FocusAngle⟩: focus-angle ⟨angle⟩deg"
                + "\nFormat of ⟨FocusDistance⟩: focus-distance ⟨percentage⟩%"
                + "\nFormat of ⟨Center⟩: center ⟨cx⟩,⟨cy⟩｜center ⟨cx⟩%,⟨cy⟩%"
                + "\nFormat of ⟨Radius⟩: ⟨radius⟩｜⟨percentage⟩%"
                + "\nFormat of ⟨Cycle⟩: repeat｜reflect"
                + "\nFormat of ⟨ColorStop⟩: ⟨Color⟩ ⟨percentage⟩%"
                + "\n" + colorConverter.getHelpText();
    }
}
