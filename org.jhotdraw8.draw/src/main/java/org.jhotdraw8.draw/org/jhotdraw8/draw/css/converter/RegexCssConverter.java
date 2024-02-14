/*
 * @(#)CssRegexConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.text.RegexReplace;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * CssRegexConverter.
 * <p>
 * Parses the following EBNF:
 * <pre>
 * RegexReplace := "none" | "replace(" Find  ","   [ Replace ] ")" ;
 * Find := TT_STRING;
 * Replace := TT_STRING;
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class RegexCssConverter extends AbstractCssConverter<RegexReplace> {

    public static final String REPLACE_FUNCTION = "replace";

    public RegexCssConverter(final boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull RegexReplace parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "⟨" + REPLACE_FUNCTION + "⟩: function expected.");
        if (!REPLACE_FUNCTION.equals(tt.currentStringNonNull())) {
            throw tt.createParseException("⟨" + REPLACE_FUNCTION + "⟩: " + REPLACE_FUNCTION + "() function expected.");
        }
        tt.requireNextToken(CssTokenType.TT_STRING, "⟨" + REPLACE_FUNCTION + "⟩: find string expected.");
        String find = tt.currentStringNonNull();
        String replace;
        if (tt.next() != CssTokenType.TT_COMMA) {
            tt.pushBack();
        }
        if (tt.next() == CssTokenType.TT_STRING) {
            replace = tt.currentStringNonNull();
        } else {
            tt.pushBack();
            replace = null;
        }
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, "⟨" + REPLACE_FUNCTION + "⟩: right bracket expected.");
        return new RegexReplace(find, replace);
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨" + REPLACE_FUNCTION + "⟩: none | " + REPLACE_FUNCTION + "(⟨Match⟩, ⟨Replace⟩)"
                + "\nFormat of ⟨Match⟩: \"match\""
                + "\nFormat of ⟨Replace⟩: \"replacement\"";
    }

    @Override
    protected <TT extends RegexReplace> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_FUNCTION, REPLACE_FUNCTION));
        String find = value.getFind();
        out.accept(new CssToken(CssTokenType.TT_STRING, find == null ? "" : find));
        out.accept(new CssToken(CssTokenType.TT_COMMA));
        String replace = value.getReplace();
        out.accept(new CssToken(CssTokenType.TT_STRING, replace == null ? "" : replace));
        out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
    }

}
