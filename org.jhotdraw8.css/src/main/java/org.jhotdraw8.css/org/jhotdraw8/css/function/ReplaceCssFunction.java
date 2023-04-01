/*
 * @(#)ReplaceCssFunction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Processes the replaceAll() function.
 * <pre>
 * replace     = "replace(", string, [","], regex, [","], replacement, ")" ;
 * string      = string-token ;
 * regex       = string-token ;
 * replacement = string-token ;
 * </pre>
 *
 * @param <T> the element type of the DOM
 */
public class ReplaceCssFunction<T> extends AbstractStringCssFunction<T> {
    /**
     * Function name.
     */
    public static final String NAME = "replace";

    public ReplaceCssFunction() {
        this(NAME);
    }

    public ReplaceCssFunction(String name) {
        super(name);
    }

    @Override
    public void process(@NonNull T element, @NonNull CssTokenizer tt,
                        @NonNull SelectorModel<T> model, @NonNull CssFunctionProcessor<T> functionProcessor,
                        @NonNull Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, getName() + "():  " + getName() + "() function expected.");
        if (!getName().equals(tt.currentStringNonNull())) {
            throw new ParseException(getName() + "(): " + getName() + "() function expected.", tt.getStartPosition());
        }

        int line = tt.getLineNumber();
        int start = tt.getStartPosition();

        String str = evalString(element, tt, getName(), functionProcessor);
        if (tt.next() != CssTokenType.TT_COMMA) {
            tt.pushBack();
        }
        String regex = evalString(element, tt, getName(), functionProcessor);
        if (tt.next() != CssTokenType.TT_COMMA) {
            tt.pushBack();
        }
        String repl = evalString(element, tt, getName(), functionProcessor);
        if (tt.next() != CssTokenType.TT_RIGHT_BRACKET) {
            throw new ParseException(getName() + "():  right bracket ')' expected.", tt.getStartPosition());
        }

        try {
            String result = Pattern.compile(regex).matcher(str).replaceAll(repl);
            int end = tt.getEndPosition();
            out.accept(new CssToken(CssTokenType.TT_STRING, result, null, line, start, end));
        } catch (IllegalArgumentException e) {
            ParseException ex = new ParseException(getName() + "(): " + e.getMessage(), tt.getStartPosition());
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public String getHelpText() {
        return getName() + "(〈string〉, 〈regex〉, 〈replacement〉)"
                + "\n    Replaces matches of 〈regex〉 by 〈replacement〉 in the given 〈string〉.";
    }
}
