/*
 * @(#)SvgFontSizeConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.converter.MappedConverter;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

import static org.jhotdraw8.icollection.MapEntries.entry;
import static org.jhotdraw8.icollection.MapEntries.linkedHashMap;
import static org.jhotdraw8.icollection.MapEntries.ofEntries;

/**
 * Converts SVG font-size.
 * <p>
 * Reference:
 * <dl>
 * <dt>Font Size</dt><dd><a href="https://www.w3.org/TR/css-fonts-3/#font-size-prop">
 *     link</a></dd>
 * </dl>
 */
public class SvgFontSizeConverter extends AbstractCssConverter<SvgFontSize> {

    private final MappedConverter<SvgFontSize.SizeKeyword> mappedConverter =
            new MappedConverter<SvgFontSize.SizeKeyword>(linkedHashMap(ofEntries(
                    entry("xx-small", SvgFontSize.SizeKeyword.XX_SMALL),
                    entry("x-small", SvgFontSize.SizeKeyword.X_SMALL),
                    entry("small", SvgFontSize.SizeKeyword.SMALL),
                    entry("medium", SvgFontSize.SizeKeyword.MEDIUM),
                    entry("large", SvgFontSize.SizeKeyword.LARGE),
                    entry("x-large", SvgFontSize.SizeKeyword.X_LARGE),
                    entry("xx-large", SvgFontSize.SizeKeyword.XX_LARGE),
                    entry("smaller", SvgFontSize.SizeKeyword.SMALLER),
                    entry("larger", SvgFontSize.SizeKeyword.LARGER)
            )));
    private final SizeCssConverter sizeConverter = new SizeCssConverter(false);

    public SvgFontSizeConverter() {
        super(false);
    }


    @Override
    public @NonNull SvgFontSize parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        SvgFontSize.SizeKeyword sizeKeyword;
        CssSize cssSize;
        if (tt.next() == CssTokenType.TT_IDENT) {
            sizeKeyword = mappedConverter.fromString(tt.currentStringNonNull());
            cssSize = null;
        } else {
            tt.pushBack();
            sizeKeyword = null;
            cssSize = sizeConverter.parseNonNull(tt, idResolver);
        }
        return new SvgFontSize(sizeKeyword, cssSize);
    }

    @Override
    public @Nullable String getHelpText() {
        return null;
    }

    @Override
    protected <TT extends SvgFontSize> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException {
        if (value.getKeyword() != null) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, mappedConverter.toString(value.getKeyword())));
        } else if (value.getLength() != null) {
            sizeConverter.produceTokens(value.getLength(), idSupplier, out);
        } else {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        }
    }
}
