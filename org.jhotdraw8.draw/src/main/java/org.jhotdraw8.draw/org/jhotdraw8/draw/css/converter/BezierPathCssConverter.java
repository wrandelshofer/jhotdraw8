/*
 * @(#)CssBezierPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.geom.SvgPaths;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jhotdraw8.geom.shape.BezierPathBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts an BezierNodeList path to a CSS String.
 * <p>
 * The null value will be converted to the CSS identifier "none".
 *
 * @author Werner Randelshofer
 */
public class BezierPathCssConverter extends AbstractCssConverter<BezierPath> {

    public BezierPathCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull BezierPath parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_STRING) {
            throw new ParseException("Could not convert " + tt.getToken() + " to a BezierPath.", tt.getStartPosition());
        }
        BezierPathBuilder builder = new BezierPathBuilder();
        SvgPaths.svgStringToBuilder(tt.currentStringNonNull(), builder);
        return builder.build();
    }

    @Override
    protected <TT extends BezierPath> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        if (value.isEmpty()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else {
            // FIXME we lose smooth here! Use a PathBuilder instead.
            out.accept(new CssToken(CssTokenType.TT_STRING, SvgPaths.awtPathIteratorToDoubleSvgString(value.getPathIterator(null))));
        }
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨BezierPath⟩: \"⟨SvgPath⟩\"";
    }
}
