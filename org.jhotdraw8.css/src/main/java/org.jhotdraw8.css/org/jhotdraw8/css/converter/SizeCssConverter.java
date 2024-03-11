/*
 * @(#)CssSizeConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * CssSizeConverter.
 * <p>
 * Parses the following EBNF from the
 * <a href="https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html">JavaFX
 * CSS Reference Guide</a>.
 * </p>
 * <pre>
 * Size := Double, [Unit] ;
 * Unit := ("px"|"mm"|"cm"|in"|"pt"|"pc"]"em"|"ex") ;
 * </pre>
 *
 * @author Werner Randelshofer
 */
public final class SizeCssConverter implements CssConverter<CssSize> {
    private final boolean nullable;

    /**
     *
     */
    public SizeCssConverter(boolean nullable) {
        this.nullable = nullable;
    }


    @Override
    public @Nullable CssSize parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (nullable) {
            if (tt.next() == CssTokenType.TT_IDENT && CssTokenType.IDENT_NONE.equals(tt.currentString())) {
                return null;
            } else {
                tt.pushBack();
            }
        }
        return parseSizeOrPercentage(tt, "size");
    }

    public static CssSize parseSize(@NonNull CssTokenizer tt, String variable) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> CssSize.of(tt.currentNumberNonNull().doubleValue());
            case CssTokenType.TT_DIMENSION -> CssSize.of(tt.currentNumberNonNull().doubleValue(), tt.currentString());
            case CssTokenType.TT_IDENT -> switch (tt.currentStringNonNull()) {
                case "NaN" -> CssSize.of(Double.NaN);
                case "INF" -> CssSize.of(Double.POSITIVE_INFINITY);
                case "-INF" -> CssSize.of(Double.NEGATIVE_INFINITY);
                default ->
                        throw new ParseException("Could not convert " + tt.getToken() + " to a double value.", tt.getStartPosition());
            };
            default ->
                    throw new ParseException("Could not convert " + tt.getToken() + " to a double value.", tt.getStartPosition());
        };
    }

    public static CssSize parseSizeOrPercentage(@NonNull CssTokenizer tt, String variable) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> CssSize.of(tt.currentNumberNonNull().doubleValue());
            case CssTokenType.TT_DIMENSION -> CssSize.of(tt.currentNumberNonNull().doubleValue(), tt.currentString());
            case CssTokenType.TT_PERCENTAGE -> CssSize.of(tt.currentNumberNonNull().doubleValue(), "%");
            case CssTokenType.TT_IDENT -> switch (tt.currentStringNonNull()) {
                case "NaN" -> CssSize.of(Double.NaN);
                case "INF" -> CssSize.of(Double.POSITIVE_INFINITY);
                case "-INF" -> CssSize.of(Double.NEGATIVE_INFINITY);
                default ->
                        throw new ParseException("Could not convert the " + tt.getToken() + " to a double value.", tt.getStartPosition());
            };
            default ->
                    throw new ParseException("Could not convert " + tt.getToken() + " to a double value.", tt.getStartPosition());
        };
    }


    @Override
    public <TT extends CssSize> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        if (value == null) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else if (UnitConverter.DEFAULT.equals(value.getUnits())) {
            out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getValue(), ""));
        } else {
            switch (value.getUnits()) {
                case UnitConverter.PERCENTAGE:
                    out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, value.getValue(), "%"));
                    break;
                default:
                    out.accept(new CssToken(CssTokenType.TT_DIMENSION, value.getValue(), value.getUnits()));
                    break;
            }
        }
    }

    @Override
    public @Nullable CssSize getDefaultValue() {
        return nullable ? null : CssSize.ZERO;
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Size⟩: ⟨size⟩ | ⟨percentage⟩% | ⟨size⟩⟨Units⟩"
                + "\nFormat of ⟨Units⟩: mm | cm | em | ex | in | pc | px | pt";
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    public boolean nullable() {
        return nullable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SizeCssConverter) obj;
        return this.nullable == that.nullable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nullable);
    }

    @Override
    public String toString() {
        return "SizeCssConverter[" +
                "nullable=" + nullable + ']';
    }


}
