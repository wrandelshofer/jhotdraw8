/*
 * @(#)CssPaintConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssLinearGradient;
import org.jhotdraw8.draw.css.value.CssRadialGradient;
import org.jhotdraw8.draw.css.value.Paintable;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * CssPaintableConverter.
 * <p>
 * Parses the following EBNF from the
 * <a href="https://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html">JavaFX
 * CSS Reference Guide</a>.
 * </p>
 * <pre>
 * Paintable := (Color|LinearGradient|RadialGradient|ImagePattern RepeatingImagePattern) ;
 * </pre>
 * <p>
 * FIXME currently only parses the Color and the LinearGradient productions
 * </p>
 *
 * @author Werner Randelshofer
 */
public class PaintCssConverter extends AbstractCssConverter<Paint> {

    protected static final @NonNull PaintableCssConverter paintableConverter = new PaintableCssConverter(false);

    public PaintCssConverter() {
        this(false);
    }

    public PaintCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull Paint parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        Paintable p = paintableConverter.parseNonNull(tt, idResolver);
        if (p.getPaint() == null) {
            throw new ParseException("paint", 0);
        }
        return p.getPaint();
    }

    @Override
    public @Nullable String getHelpText() {
        return paintableConverter.getHelpText();
    }

    @Override
    protected <TT extends Paint> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException {
        Paintable p = switch (value) {
            case Color color -> new CssColor(color);
            case LinearGradient linearGradient -> new CssLinearGradient(linearGradient);
            case RadialGradient radialGradient -> new CssRadialGradient(radialGradient);
            default -> throw new UnsupportedOperationException("unsupported value:" + value);
        };
        paintableConverter.produceTokensNonNull(p, idSupplier, out);
    }
}
