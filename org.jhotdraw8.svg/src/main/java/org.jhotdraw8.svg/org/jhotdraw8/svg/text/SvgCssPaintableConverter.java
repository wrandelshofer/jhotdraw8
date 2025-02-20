/*
 * @(#)SvgCssPaintableConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.text;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.converter.ColorCssConverter;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.Paintable;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * SvgCssPaintableConverter.
 *
 */
public class SvgCssPaintableConverter extends AbstractCssConverter<Paintable> {
    /**
     * The currentColor keyword.
     */
    public static final String CURRENT_COLOR_KEYWORD = "currentColor";

    private static final ColorCssConverter colorConverter = new ColorCssConverter(false);

    public SvgCssPaintableConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    protected <TT extends Paintable> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        if (value instanceof CssColor c) {
            colorConverter.produceTokens(c, idSupplier, out);
        } else {
            throw new UnsupportedOperationException("not yet implemented for " + value);
        }
    }

    @Override
    public Paintable parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() == CssTokenType.TT_URL) {
            String url = tt.currentStringNonNull();
            if (url.startsWith("#")) {
                Object object = idResolver.getObject(url.substring(1));
                if (object instanceof Paintable) {
                    return (Paintable) object;
                }
            }
            throw tt.createParseException("SvgPaintable illegal URL: " + url);
        } else {
            tt.pushBack();
            return colorConverter.parseNonNull(tt, idResolver);
        }
    }

    @Override
    public @Nullable String getHelpText() {
        String[] lines = ("Format of ⟨Paint⟩: none｜（⟨Color⟩｜ ⟨LinearGradient⟩｜ ⟨RadialGradient⟩"
                + "\n" + colorConverter.getHelpText()).split("\n");
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
}
