/*
 * @(#)CssSvgPathConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.geom.FXSvgPaths;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Converts an SVG path to a CSS String.
 * <p>
 * The null value will be converted to the CSS identifier "none".
 *
 */
public class FXPathCssConverter extends AbstractCssConverter<Path> {


    public FXPathCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public Path parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_STRING, "⟨SvgPath⟩: String expected.");
        final String svgPathString = tt.currentStringNonNull();

        Path path;
        try {
            List<PathElement> pathElements = FXSvgPaths.svgStringToPathElements(svgPathString);
            path = new Path(pathElements);
        } catch (final ParseException ex) {
            path = new Path(
                    new MoveTo(0, 0),
                    new LineTo(10, 0),
                    new LineTo(10, 10),
                    new LineTo(0, 10),
                    new ClosePath()
            );
        }
        path.setStroke(null);
        return path;
    }

    @Override
    protected <TT extends Path> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_STRING, FXSvgPaths.pathElementsToDoubleSvgString(value.getElements())));
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
    public @Nullable Path getDefaultValue() {
        return null;
    }


}
