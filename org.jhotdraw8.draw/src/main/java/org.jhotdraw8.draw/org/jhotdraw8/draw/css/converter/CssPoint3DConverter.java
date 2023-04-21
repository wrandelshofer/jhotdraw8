/*
 * @(#)CssPoint3DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.converter.CssSizeConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssPoint3D;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts a {@code javafx.geometry.CssPoint3D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class CssPoint3DConverter extends AbstractCssConverter<CssPoint3D> {
    private final boolean withSpace;
    private final boolean withComma;

    public CssPoint3DConverter(boolean nullable) {
        this(nullable, true, false);
    }

    public CssPoint3DConverter(boolean nullable, boolean withSpace, boolean withComma) {
        super(nullable);
        this.withSpace = withSpace || !withComma;
        this.withComma = withComma;
    }

    @Override
    public @NonNull CssPoint3D parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final CssSize x, y, z;
        x = CssSizeConverter.parseSize(tt, "x");
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        y = CssSizeConverter.parseSize(tt, "y");
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        if (tt.next() == CssTokenType.TT_EOF) {
            z = CssSize.ZERO;
        } else {
            tt.pushBack();
            z = CssSizeConverter.parseSize(tt, "z");
        }

        return new CssPoint3D(x, y, z);
    }


    @Override
    protected <TT extends CssPoint3D> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        CssSize x = value.getX();
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, x.getValue(), x.getUnits()));
        produceDelimiter(out);
        CssSize y = value.getY();
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, y.getValue(), y.getUnits()));
        CssSize z = value.getZ();
        if (z.getValue() != 0) {
            produceDelimiter(out);
            out.accept(new CssToken(CssTokenType.TT_DIMENSION, z.getValue(), z.getUnits()));
        }
    }

    private void produceDelimiter(@NonNull Consumer<CssToken> out) {
        if (withComma) {
            out.accept(new CssToken(CssTokenType.TT_COMMA));
        }
        if (withSpace) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨CssPoint3D⟩: ⟨x⟩ ⟨y⟩ ｜ ⟨x⟩ ⟨y⟩ ⟨z⟩";
    }
}
