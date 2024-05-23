/*
 * @(#)ConcatCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * Processes the concat() function.
 * <pre>
 * concat              = "concat(", string-list, ")" ;
 * string-list         = value ,  { [ ',' ] , value } ;
 * value               = string | number | dimension | percentage | url ;
 * </pre>
 *
 * @param <T> the element type of the DOM
 */
public class ConcatCssFunction<T> extends AbstractStringCssFunction<T> {
    /**
     * Function name.
     */
    public static final String NAME = "concat";

    public ConcatCssFunction() {
        super(NAME);
    }

    public ConcatCssFunction(String name) {
        super(name);
    }

    @Override
    public String getHelpText() {
        return getName() + "(⟨string⟩, ...)"
                + "\n    Concatenates a list of strings.";
    }


    @Override
    public void process(T element, CssTokenizer tt, SelectorModel<T> model,
                        CssFunctionProcessor<T> functionProcessor, Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, getName() + "():  concat() function expected.");
        if (!getName().equals(tt.currentStringNonNull())) {
            throw new ParseException(getName() + "():  concat() function expected.", tt.getStartPosition());
        }

        int line = tt.getLineNumber();
        int start = tt.getStartPosition();

        StringBuilder buf = new StringBuilder();
        boolean first = true;
        while (tt.next() != CssTokenType.TT_EOF && tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            switch (tt.current()) {
            case CssTokenType.TT_COMMA:
                if (!first) {
                    continue;
                }
                tt.pushBack();
                buf.append(evalString(element, tt, getName(), functionProcessor));
                break;
            default:
                tt.pushBack();
                buf.append(evalString(element, tt, getName(), functionProcessor));
                break;
            }
            first = false;
        }
        if (tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            throw new ParseException(getName() + "():  right bracket ')' expected.", tt.getStartPosition());
        }
        int end = tt.getEndPosition();
        out.accept(new CssToken(CssTokenType.TT_STRING, buf.toString(), null, line, start, end));
    }


}
