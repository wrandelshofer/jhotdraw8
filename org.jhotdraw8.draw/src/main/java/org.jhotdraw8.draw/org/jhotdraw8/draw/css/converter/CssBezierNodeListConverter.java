/*
 * @(#)CssBezierNodeListConverter.java
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
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierNodePath;
import org.jhotdraw8.geom.shape.BezierNodePathBuilder;
import org.jhotdraw8.icollection.immutable.ImmutableList;

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
public class CssBezierNodeListConverter extends AbstractCssConverter<ImmutableList<BezierNode>> {

    public CssBezierNodeListConverter(boolean nullable) {
        super(nullable);
    }


    @Override
    public @NonNull ImmutableList<BezierNode> parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_STRING) {
            throw new ParseException("⟨BezierNodePath⟩ String expected.", tt.getStartPosition());
        }
        BezierNodePathBuilder builder = new BezierNodePathBuilder();
        SvgPaths.buildFromSvgString(builder, tt.currentStringNonNull());
        return builder.build();
    }

    @Override
    protected <TT extends ImmutableList<BezierNode>> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        if (value.isEmpty()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else {
            // FIXME we lose smooth here! Use a PathBuilder instead.
            out.accept(new CssToken(CssTokenType.TT_STRING, SvgPaths.doubleSvgStringFromAwt(new BezierNodePath(value).getPathIterator(null))));
        }
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨BezierNodePath⟩: \"⟨SvgPath⟩\"";
    }
}
