/*
 * @(#)AbstractStringCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for CSS functions that process a {@link CssTokenType#TT_STRING}.
 *
 * @param <T> the element type of the DOM
 */
public abstract class AbstractStringCssFunction<T> extends AbstractCssFunction<T> {
    public AbstractStringCssFunction(String name) {
        super(name);
    }

    protected @NonNull String evalString(@NonNull T element, @NonNull CssTokenizer tt, String expressionName, CssFunctionProcessor<T> functionProcessor) throws IOException, ParseException {
        StringBuilder buf = new StringBuilder();
        List<CssToken> temp = new ArrayList<>();

        int count = 0;

        // skip white space
        while (tt.next() == CssTokenType.TT_S) {
        }
        tt.pushBack();

        functionProcessor.processToken(element, tt, temp::add, new ArrayDeque<>());
        for (CssToken t : temp) {
            switch (t.getType()) {
            case CssTokenType.TT_STRING:
            case CssTokenType.TT_URL:
                buf.append(t.getStringValue());
                count++;
                break;
            case CssTokenType.TT_NUMBER:
            case CssTokenType.TT_DIMENSION:
            case CssTokenType.TT_PERCENTAGE:
                buf.append(t.fromToken());
                count++;
                break;
            default:
                throw new ParseException(getName() + "(): String, Number, Dimension, Percentage or URL expected.", t.getStartPos());
            }
        }
        if (count == 0) {
            throw new ParseException(getName() + "(): String, Number, Dimension, Percentage or URL expected.", 0);
        }

        return buf.toString();
    }

}
