/*
 * @(#)CssStrokeStyleConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.CssStrokeStyle;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Allows to set all stroke properties at once.
 * <pre>
 *     StrokeStyle := {Options};
 *     Options = ( Type | Linecap | Linejoin | Miterlimit | Dashoffset | Dasharray );
 *     Type = "type(" , ("inside"|"outside"|"centered"), ")";
 *     Linecap = "linecap(",("square"|"butt"|"round"),")";
 *     Linejoin = "linecap(",("miter"|"bevel"|"round"),")";
 *     Miterlimit = "miterlimit(",Size,")";
 *     Dashoffset = "dashoffset(",Size,")";
 *     Dasharray = "dasharray(",Size,{Size},")";
 * </pre>
 */
public class StrokeStyleCssConverter extends AbstractCssConverter<CssStrokeStyle> {

    public static final String INSIDE = "inside";
    public static final String OUTSIDE = "outside";
    public static final String CENTERED = "centered";
    public static final String BUTT = "butt";
    public static final String MITER = "miter";
    public static final String ROUND = "round";
    public static final String BEVEL = "bevel";
    public static final String SQUARE = "square";
    public static final String TYPE = "type";
    public static final String LINEJOIN = "linejoin";
    public static final String LINECAP = "linecap";
    public static final String DASHOFFSET = "dashoffset";
    public static final String DASHARRAY = "dasharray";
    public static final String MITERLIMIT = "miterlimit";
    private final boolean printAllValues = true;

    public StrokeStyleCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public CssStrokeStyle parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        StrokeType type = StrokeType.CENTERED;
        StrokeLineCap lineCap = StrokeLineCap.BUTT;
        StrokeLineJoin lineJoin = StrokeLineJoin.MITER;
        CssSize miterLimit = CssSize.of(4);
        CssSize dashOffset = CssSize.of(0);
        PersistentList<CssSize> dashArray = VectorList.of();

        while (tt.next() == CssTokenType.TT_FUNCTION) {
            tt.pushBack();
            switch (tt.currentStringNonNull()) {
                case TYPE:
                    type = parseStrokeType(tt);
                    break;
                case LINECAP:
                    lineCap = parseLineCap(tt);
                break;
            case LINEJOIN:
                lineJoin = parseLineJoin(tt);
                break;
            case MITERLIMIT:
                miterLimit = parseNumericFunction(MITERLIMIT, CssSize.of(10), tt, idResolver);
                break;
            case DASHOFFSET:
                dashOffset = parseNumericFunction(DASHOFFSET, CssSize.of(0), tt, idResolver);
                break;
            case DASHARRAY:
                dashArray = parseDashArray(tt, idResolver);
                break;
            default:
                throw new ParseException("⟨StrokeStyle⟩:: Unsupported function: " + tt.currentStringNonNull(), tt.getStartPosition());
            }
        }

        return new CssStrokeStyle(type, lineCap, lineJoin, miterLimit, dashOffset, dashArray);
    }

    protected StrokeLineJoin parseLineJoin(CssTokenizer tt) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_FUNCTION || !LINEJOIN.equals(tt.currentStringNonNull())) {
            throw new ParseException("⟨StrokeStyle⟩:: Function " + LINEJOIN + "() expected.", tt.getStartPosition());
        }
        StrokeLineJoin lineJoin;
        tt.requireNextToken(CssTokenType.TT_IDENT, "⟨StrokeStyle⟩: One of " + MITER + ", " + BEVEL + ", " + ROUND + " expected.");
        lineJoin = switch (tt.currentStringNonNull()) {
            case MITER -> StrokeLineJoin.MITER;
            case BEVEL -> StrokeLineJoin.BEVEL;
            case ROUND -> StrokeLineJoin.ROUND;
            default ->
                    throw tt.createParseException("⟨StrokeStyle⟩: One of " + MITER + ", " + BEVEL + ", " + ROUND + " expected.");
        };
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, "⟨StrokeStyle⟩:: ⟨" + LINEJOIN + "⟩ right bracket expected.");
        return lineJoin;
    }

    protected StrokeLineCap parseLineCap(CssTokenizer tt) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_FUNCTION || !LINECAP.equals(tt.currentStringNonNull())) {
            throw new ParseException("⟨StrokeStyle⟩:: Function " + LINECAP + "() expected.", tt.getStartPosition());
        }
        StrokeLineCap lineCap;
        tt.requireNextToken(CssTokenType.TT_IDENT, "⟨StrokeStyle⟩: One of " + SQUARE + ", " + BUTT + ", " + ROUND + " expected.");
        lineCap = switch (tt.currentStringNonNull()) {
            case SQUARE -> StrokeLineCap.SQUARE;
            case BUTT -> StrokeLineCap.BUTT;
            case ROUND -> StrokeLineCap.ROUND;
            default ->
                    throw new ParseException("⟨StrokeStyle⟩: One of " + SQUARE + ", " + BUTT + ", " + ROUND + " expected.", tt.getStartPosition());
        };
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, "⟨StrokeStyle⟩:: ⟨" + LINECAP + "⟩ right bracket expected.");
        return lineCap;
    }

    protected StrokeType parseStrokeType(CssTokenizer tt) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_FUNCTION || !TYPE.equals(tt.currentStringNonNull())) {
            throw new ParseException("⟨StrokeStyle⟩:: Function " + TYPE + "() expected.", tt.getStartPosition());
        }
        StrokeType type;
        tt.requireNextToken(CssTokenType.TT_IDENT, "One of " + INSIDE + ", " + OUTSIDE + ", " + CENTERED + " expected.");
        type = switch (tt.currentStringNonNull()) {
            case INSIDE -> StrokeType.INSIDE;
            case OUTSIDE -> StrokeType.OUTSIDE;
            case CENTERED -> StrokeType.CENTERED;
            default ->
                    throw new ParseException("One of " + INSIDE + ", " + OUTSIDE + ", " + CENTERED + " expected.", tt.getStartPosition());
        };
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, "⟨StrokeStyle⟩:: ⟨" + TYPE + "⟩ right bracket expected.");
        return type;
    }

    private PersistentList<CssSize> parseDashArray(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_FUNCTION || !DASHARRAY.equals(tt.currentStringNonNull())) {
            throw new ParseException("⟨StrokeStyle⟩: Function " + DASHARRAY + "() expected.", tt.getStartPosition());
        }

        List<CssSize> list = new ArrayList<>();
        while (tt.next() == CssTokenType.TT_NUMBER || tt.current() == CssTokenType.TT_DIMENSION) {
            tt.pushBack();
            list.add(parseSize(DASHARRAY, null, tt, idResolver));
            if (tt.next() != CssTokenType.TT_COMMA) {
                tt.pushBack();
            }
        }
        tt.pushBack();
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, "⟨StrokeStyle⟩: ⟨" + DASHARRAY + "⟩ right bracket expected.");
        return VectorList.copyOf(list);
    }

    private CssSize parseNumericFunction(String functionName, CssSize defaultValue, CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_FUNCTION || !functionName.equals(tt.currentStringNonNull())) {
            throw new ParseException("Function " + functionName + "() expected.", tt.getStartPosition());
        }

        CssSize value;
        value = parseSize(functionName, defaultValue, tt, idResolver);
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, "⟨StrokeStyle⟩: ⟨" + functionName + "⟩ right bracket expected.");
        return value;
    }

    private CssSize parseSize(String name, CssSize defaultValue, CssTokenizer tt, IdResolver idResolver) throws IOException {
        CssSize value;
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            value = CssSize.of(tt.currentNumberNonNull().doubleValue());
            break;
        case CssTokenType.TT_DIMENSION:
            value = CssSize.of(tt.currentNumberNonNull().doubleValue(), tt.currentStringNonNull());
            break;
        default:
            value = defaultValue;
            tt.pushBack();
        }
        return value;
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨StrokeStyle⟩: [⟨Type⟩］［⟨Linecap⟩］［⟨Linejoin⟩］［⟨Miterlimit⟩］［⟨Dashoffset⟩］［⟨Dasharray⟩］"
                + "\n  with ⟨Type⟩: " + TYPE + "(inside｜outside｜centered)"
                + "\n  with ⟨Linecap⟩: " + LINECAP + "(square｜butt｜round)"
                + "\n  with ⟨Linejoin⟩: " + LINEJOIN + "(miter｜bevel｜round)"
                + "\n  with ⟨Miterlimit⟩: " + MITERLIMIT + "(size)"
                + "\n  with ⟨Dashoffset⟩: " + DASHOFFSET + "(size)"
                + "\n  with ⟨Dasharray⟩: " + DASHARRAY + "(size...)"
                ;
    }

    @Override
    public PersistentList<String> getExamples() {
        return VectorList.of(
                "type(inside)",
                "type(centered)",
                "type(outside)",
                "linecap(round) linejoin(round)",
                "dashoffset(2) dasharray(5 10)"
        );
    }

    @Override
    protected <TT extends CssStrokeStyle> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        final StrokeType type = value.getType();
        if (printAllValues || type != StrokeType.CENTERED) {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, TYPE));
            switch (type) {
            case INSIDE:
                out.accept(new CssToken(CssTokenType.TT_IDENT, INSIDE));
                break;
            case OUTSIDE:
                out.accept(new CssToken(CssTokenType.TT_IDENT, OUTSIDE));
                break;
            case CENTERED:
                out.accept(new CssToken(CssTokenType.TT_IDENT, CENTERED));
                break;
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        }

        final StrokeLineCap lineCap = value.getLineCap();

        if (printAllValues || lineCap != StrokeLineCap.BUTT) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, LINECAP));
            switch (lineCap) {
            case BUTT:
                out.accept(new CssToken(CssTokenType.TT_IDENT, BUTT));
                break;
            case ROUND:
                out.accept(new CssToken(CssTokenType.TT_IDENT, ROUND));
                break;
            case SQUARE:
                out.accept(new CssToken(CssTokenType.TT_IDENT, SQUARE));
                break;
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        }
        final StrokeLineJoin lineJoin = value.getLineJoin();
        if (printAllValues || lineJoin != StrokeLineJoin.MITER) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, LINEJOIN));
            switch (lineJoin) {
            case BEVEL:
                out.accept(new CssToken(CssTokenType.TT_IDENT, BUTT));
                break;
            case ROUND:
                out.accept(new CssToken(CssTokenType.TT_IDENT, ROUND));
                break;
            case MITER:
                out.accept(new CssToken(CssTokenType.TT_IDENT, MITER));
                break;
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        }

        CssSize miterLimit = value.getMiterLimit();
        if (printAllValues || !miterLimit.getUnits().equals(UnitConverter.DEFAULT) || miterLimit.getConvertedValue() != 4.0) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, MITERLIMIT));
            out.accept(new CssToken(CssTokenType.TT_DIMENSION, miterLimit.getValue(), miterLimit.getUnits()));
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        }

        CssSize dashOffset = value.getDashOffset();
        if (printAllValues || !dashOffset.getUnits().equals(UnitConverter.DEFAULT) || dashOffset.getConvertedValue() != 0.0) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, DASHOFFSET));
            out.accept(new CssToken(CssTokenType.TT_DIMENSION, dashOffset.getValue(), dashOffset.getUnits()));
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        }

        PersistentList<CssSize> dashArray = value.getDashArray();
        if (printAllValues || !dashArray.isEmpty()) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, DASHARRAY));
            for (int i = 0, n = dashArray.size(); i < n; i++) {
                if (i != 0) {
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                }
                CssSize dash = dashArray.get(i);
                out.accept(new CssToken(CssTokenType.TT_DIMENSION, dash.getValue(), dash.getUnits()));
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        }
    }
}
