/*
 * @(#)SvgStrokeAlignmentConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.text;

import javafx.scene.shape.StrokeType;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts SVG 2 stroke-alignment.
 * <pre>
 *  StrokeAlignment = "type(" , ("inner"|"outer"|"center"), ")";
 * </pre>
 * <p>
 * References:
 * <dl>
 * <dt>SVG Strokes, § 2.2. Specifying stroke alignment: the ‘stroke-alignment’ property</dt>
 * <dd><a href="https://www.w3.org/TR/svg-strokes/#SpecifyingStrokeAlignment">w3.org</a></dd>
 * </dl>
 */
public class SvgStrokeAlignmentConverter extends AbstractCssConverter<StrokeType> {

    public static final String INSIDE = "inner";
    public static final String OUTSIDE = "outer";
    public static final String CENTERED = "center";

    public SvgStrokeAlignmentConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull StrokeType parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        StrokeType type;
        tt.requireNextToken(CssTokenType.TT_IDENT, "One of " + INSIDE + ", " + OUTSIDE + ", " + CENTERED + " expected.");
        type = switch (tt.currentStringNonNull()) {
            case INSIDE -> StrokeType.INSIDE;
            case OUTSIDE -> StrokeType.OUTSIDE;
            case CENTERED -> StrokeType.CENTERED;
            default ->
                    throw tt.createParseException("One of " + INSIDE + ", " + OUTSIDE + ", " + CENTERED + " expected.");
        };
        return type;
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨StrokeAlignment⟩: (inside｜outside｜centered)"
                ;
    }


    @Override
    protected <TT extends StrokeType> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        final StrokeType type = value;
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
    }

}
