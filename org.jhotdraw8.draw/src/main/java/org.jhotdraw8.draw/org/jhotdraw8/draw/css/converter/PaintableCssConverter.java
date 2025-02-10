/*
 * @(#)CssPaintableConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssLinearGradient;
import org.jhotdraw8.draw.css.value.CssRadialGradient;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * CssPaintableConverter.
 * <p>
 * Parses the following EBNF from the
 * <a href="https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html">JavaFX
 * CSS Reference Guide</a>.
 * </p>
 * <pre>
 * Paintable := (Color|LinearGradient|RadialGradient|ImagePattern|RepeatingImagePattern) ;
 * </pre>
 * <p>
 * FIXME currently only parses the Color and the LinearGradient productions
 * </p>
 *
 */
public class PaintableCssConverter extends AbstractCssConverter<Paintable> {

    private static final ColorCssConverter colorConverter = new ColorCssConverter(false);
    private static final LinearGradientCssConverter linearGradientConverter = new LinearGradientCssConverter(false);
    private static final RadialGradientCssConverter radialGradientConverter = new RadialGradientCssConverter(false);

    public PaintableCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @Nullable String getHelpText() {
        String[] lines = ("Format of ⟨Paint⟩: none｜（⟨Color⟩｜ ⟨LinearGradient⟩｜ ⟨RadialGradient⟩"
                + "\n" + colorConverter.getHelpText()
                + "\n" + linearGradientConverter.getHelpText()
                + "\n" + radialGradientConverter.getHelpText()).split("\n");
        StringBuilder buf = new StringBuilder();
        Set<String> duplicateLines = new HashSet<>();
        for (String line : lines) {
            if (duplicateLines.add(line)) {
                if (!buf.isEmpty()) {
                    buf.append('\n');
                }
                buf.append(line);
            }
        }
        return buf.toString();
    }

    @Override
    public Paintable parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() == CssTokenType.TT_FUNCTION) {
            switch (tt.currentStringNonNull()) {
                case LinearGradientCssConverter.LINEAR_GRADIENT_FUNCTION:
                tt.pushBack();
                return linearGradientConverter.parseNonNull(tt, idResolver);
                case RadialGradientCssConverter.RADIAL_GRADIENT_FUNCTION:
                tt.pushBack();
                return radialGradientConverter.parseNonNull(tt, idResolver);
            default:
                break;
            }
        }
        tt.pushBack();
        return colorConverter.parseNonNull(tt, idResolver);
    }

    @Override
    protected <TT extends Paintable> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) throws IOException {
        switch (value) {
            case CssColor c -> colorConverter.produceTokens(c, idSupplier, out);
            case CssLinearGradient lg -> linearGradientConverter.produceTokens(lg, idSupplier, out);
            case CssRadialGradient lg -> radialGradientConverter.produceTokens(lg, idSupplier, out);
            default -> throw new UnsupportedOperationException("not yet implemented for " + value);
        }
    }
}
