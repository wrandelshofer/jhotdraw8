/*
 * @(#)CssScale3DConverter.java
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
 * @author Werner Randelshofer
 */
public class Scale3DCssConverter extends AbstractCssConverter<Point3D> {

    private final boolean withSpace;
    private final boolean withComma = false;

    public Scale3DCssConverter() {
        this(false, true);
    }

    public Scale3DCssConverter(boolean nullable) {
        this(nullable, true);
    }

    public Scale3DCssConverter(boolean nullable, boolean withSpace) {
        super(nullable);
        this.withSpace = withSpace;
    }

    @Override
    public Point3D parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final double x, y, z;
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Scale3D⟩: ⟨x⟩ expected.");
        x = tt.currentNumberNonNull().doubleValue();
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        if (tt.next() == CssTokenType.TT_NUMBER) {
            y = tt.currentNumberNonNull().doubleValue();
        } else {
            y = x;
            tt.pushBack();
        }

        tt.skipIfPresent(CssTokenType.TT_COMMA);
        if (tt.next() == CssTokenType.TT_NUMBER) {
            z = tt.currentNumberNonNull().doubleValue();
        } else {
            z = 1;
            tt.pushBack();
        }


        return new Point3D(x, y, z);
    }

    @Override
    protected <TT extends Point3D> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        double x = value.getX();
        double y = value.getY();
        double z = value.getZ();
        out.accept(new CssToken(CssTokenType.TT_NUMBER, x));
        if (x != y || z != 1) {
            produceDelimiter(out);
            out.accept(new CssToken(CssTokenType.TT_NUMBER, y));
        }
        if (z != 1) {
            produceDelimiter(out);
            out.accept(new CssToken(CssTokenType.TT_NUMBER, z));
        }
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
    public @Nullable Point3D getDefaultValue() {
        return new Point3D(1, 1, 1);
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Scale3D⟩: ⟨s⟩ ｜ ⟨xs⟩ ⟨ys⟩ ｜ ⟨xs⟩ ⟨ys⟩ ⟨zs⟩";
    }

}
