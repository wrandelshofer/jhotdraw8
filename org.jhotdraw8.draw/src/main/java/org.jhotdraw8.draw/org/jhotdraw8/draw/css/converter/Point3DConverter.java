/*
 * @(#)Point3DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.geometry.Point3D;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts a {@code javafx.geometry.Point3D} into a {@code String} and vice
 * versa.
 *
 */
public class Point3DConverter extends AbstractCssConverter<Point3D> {

    private final boolean withComma = false;
    private final boolean withSpace;

    public Point3DConverter(boolean nullable) {
        this(nullable, true);
    }

    public Point3DConverter(boolean nullable, boolean withSpace) {
        super(nullable);
        this.withSpace = withSpace;
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Point3D⟩: ⟨x⟩ ⟨y⟩ ｜ ⟨x⟩ ⟨y⟩ ⟨z⟩";
    }

    @Override
    public Point3D parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final double x, y, z;
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Point3D⟩: ⟨x⟩ expected.");
        x = tt.currentNumberNonNull().doubleValue();
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Point3D⟩: ⟨y⟩ expected.");
        y = tt.currentNumberNonNull().doubleValue();
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        if (tt.next() == CssTokenType.TT_NUMBER) {
            tt.pushBack();
            z = tt.currentNumberNonNull().doubleValue();
        } else {
            z = 0;
        }

        return new Point3D(x, y, z);
    }

    private void produceDelimiter(Consumer<CssToken> out) {
        if (withComma) {
            out.accept(new CssToken(CssTokenType.TT_COMMA));
        }
        if (withSpace) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
    }

    @Override
    protected <TT extends Point3D> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getX()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getY()));
        double z = value.getZ();
        if (z != 0.0) {
            produceDelimiter(out);
            out.accept(new CssToken(CssTokenType.TT_NUMBER, z));
        }
    }
}
