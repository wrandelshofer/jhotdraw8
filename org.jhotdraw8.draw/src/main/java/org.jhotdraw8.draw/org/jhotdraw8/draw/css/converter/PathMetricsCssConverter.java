/*
 * @(#)CssBezierPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.geom.SvgPaths;
import org.jhotdraw8.geom.shape.PathMetrics;
import org.jhotdraw8.geom.shape.PathMetricsBuilder;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts an BezierNodeList path to a CSS String.
 * <p>
 * The null value will be converted to the CSS identifier "none".
 *
 */
public class PathMetricsCssConverter extends AbstractCssConverter<PathMetrics> {

    public PathMetricsCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public PathMetrics parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_STRING) {
            throw new ParseException("⟨BezierPath⟩ String expected.", tt.getStartPosition());
        }
        PathMetricsBuilder builder = new PathMetricsBuilder();
        SvgPaths.buildSvgString(builder, tt.currentStringNonNull());
        return builder.build();
    }

    @Override
    protected <TT extends PathMetrics> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_STRING, SvgPaths.awtPathIteratorToDoubleSvgString(value.getPathIterator(null))));
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨BezierPath⟩: \"⟨SvgPath⟩\"";
    }
}
