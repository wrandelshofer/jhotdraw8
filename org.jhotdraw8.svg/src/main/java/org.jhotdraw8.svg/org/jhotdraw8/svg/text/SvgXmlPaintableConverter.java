/*
 * @(#)SvgXmlPaintableConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.draw.css.value.Paintable;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Paintable representation in XML files is different from representation in CSS.
 */
public class SvgXmlPaintableConverter extends AbstractCssConverter<Paintable> implements Converter<Paintable> {
    private final @NonNull SvgCssPaintableConverter cssPaintableConverter = new SvgCssPaintableConverter(false);

    public SvgXmlPaintableConverter() {
        this(true);
    }

    public SvgXmlPaintableConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull Paintable parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        Objects.requireNonNull(idResolver, "idResolver");
        if (tt.next() == CssTokenType.TT_URL) {
            final String urlString = tt.currentStringNonNull();
            if (urlString.startsWith("#")) {
                final Object object = idResolver.getObject(urlString.substring(1));
                if (object instanceof Paintable) {
                    return (Paintable) object;
                }
            }
            throw new ParseException("Paintable: Could not resolve " + urlString, tt.getStartPosition());
        } else {
            tt.pushBack();
        }
        tt.pushBack();
        return cssPaintableConverter.parseNonNull(tt, idResolver);
    }

    @Override
    public @Nullable String getHelpText() {
        return null;
    }

    @Override
    public boolean needsIdResolver() {
        return true;
    }

    @Override
    protected <TT extends Paintable> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException {
        cssPaintableConverter.produceTokensNonNull(value, idSupplier, out);
    }
}
