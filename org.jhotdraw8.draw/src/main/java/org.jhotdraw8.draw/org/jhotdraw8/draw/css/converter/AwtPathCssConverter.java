/*
 * @(#)CssAwtSvgPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.geom.AwtPathBuilder;
import org.jhotdraw8.geom.SvgPaths;
import org.jspecify.annotations.Nullable;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts an SVG path to a AWT path.
 * <p>
 * The null value will be converted to the CSS identifier "none".
 *
 */
public class AwtPathCssConverter extends AbstractCssConverter<Path2D.Double> {


    public AwtPathCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public Path2D.Double parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_STRING, "⟨SvgPath⟩: String expected.");
        final String svgPathString = tt.currentStringNonNull();

        try {
            final AwtPathBuilder builder = new AwtPathBuilder();
            SvgPaths.buildSvgString(builder, svgPathString);
            return builder.build();
        } catch (final ParseException ex) {
            final Path2D.Double p = new Path2D.Double();
            p.moveTo(0, 0);
            p.lineTo(10, 0);
            p.lineTo(10, 10);
            p.lineTo(0, 10);
            p.closePath();
            p.trimToSize();
            return p;
        }
    }

    @Override
    protected <TT extends Path2D.Double> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_STRING, SvgPaths.awtPathIteratorToDoubleSvgString(((Shape) value).getPathIterator(null))));
    }

    @Override
    public @Nullable String getHelpText() {
        String buf = """
                     Format of ⟨SvgPath⟩: " ⟨moveTo ⟩｛ moveTo｜⟨lineTo⟩｜⟨quadTo⟩｜⟨cubicTo⟩｜⟨arcTo⟩｜⟨closePath⟩ ｝ "
                     Format of ⟨moveTo ⟩: M ⟨x⟩ ⟨y⟩ ｜m ⟨dx⟩ ⟨dy⟩\s
                     Format of ⟨lineTo ⟩: L ⟨x⟩ ⟨y⟩ ｜l ⟨dx⟩ ⟨dy⟩ | H ⟨x⟩ | h ⟨dx⟩ | V ⟨y⟩ | v ⟨dy⟩
                     Format of ⟨quadTo ⟩: Q ⟨x⟩ ⟨y⟩  ⟨x1⟩ ⟨y1⟩ ｜q ⟨dx⟩ ⟨dy⟩  ⟨x1⟩ ⟨y1⟩ ｜T ⟨x⟩ ⟨y⟩ ｜t ⟨dx⟩ ⟨dy⟩
                     Format of ⟨cubicTo ⟩: C ⟨x⟩ ⟨y⟩  ⟨x1⟩ ⟨y1⟩  ⟨x2⟩ ⟨y2⟩ ｜c ⟨dx⟩ ⟨dy⟩  ⟨dx1⟩ ⟨dy1⟩  ⟨dx2⟩ ⟨dy2⟩｜ S ⟨x⟩ ⟨y⟩  ⟨x1⟩ ⟨y1⟩ ｜s ⟨dx⟩ ⟨dy⟩  ⟨dx1⟩ ⟨dy1⟩
                     Format of ⟨arcTo ⟩: A ⟨x⟩ ⟨y⟩ ⟨r1⟩ ⟨r2⟩ ⟨angle⟩ ⟨larrgeArcFlag⟩ ⟨sweepFlag⟩ ｜a ⟨dx⟩ ⟨dy⟩ ⟨r1⟩ ⟨r2⟩ ⟨angle⟩ ⟨larrgeArcFlag⟩ ⟨sweepFlag⟩\s
                     Format of ⟨closePath ⟩: Z ｜z\s""";
        return buf;
    }


    @Override
    public Path2D.@Nullable Double getDefaultValue() {
        return null;
    }


}
