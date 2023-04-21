/*
 * @(#)CssIdentConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

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
 * CssIdentifier converter.
 * <pre>
 * ident         = [ '-' ] , nmstart , { nmchar } ;
 * name          = { nmchar }- ;
 * nmstart       = '_' | letter | nonascii | escape ;
 * nonascii      = ? U+00A0 through U+10FFFF ? ;
 * letter        = ? 'a' through 'z' or 'A' through 'Z' ?
 * unicode       = '\' , ( 6 * hexd
 *                       | hexd , 5 * [hexd] , w
 *                       );
 * escape        = ( unicode
 *                 | '\' , -( newline | hexd)
 *                 ) ;
 * nmchar        = '_' | letter | digit | '-' | nonascii | escape ;
 * num           = [ '+' | '-' ] ,
 *                 ( { digit }-
 *                 | { digit } , '.' , { digit }-
 *                 )
 *                 [ 'e'  , [ '+' | '-' ] , { digit }- ] ;
 * digit         = ? '0' through '9' ?
 * letter        = ? 'a' through 'z' ? | ? 'A' through 'Z' ? ;
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class CssIdentConverter extends AbstractCssConverter<String> {

    public CssIdentConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull String parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_IDENT, " ⟨Ident⟩: ⟨identifier⟩ expected");
        return tt.currentStringNonNull();
    }


    @Override
    protected <TT extends String> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_IDENT, value));
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨Ident⟩: ⟨identifier⟩";
    }
}
