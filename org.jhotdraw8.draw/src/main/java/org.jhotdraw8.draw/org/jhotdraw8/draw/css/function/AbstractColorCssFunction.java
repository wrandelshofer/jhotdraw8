/*
 * @(#)AbstractColorCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.function;

import org.jhotdraw8.css.function.AbstractCssFunction;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.ListCssTokenizer;
import org.jhotdraw8.draw.css.converter.ColorCssConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for CSS functions that process a color value.
 *
 * @param <T> the element type of the DOM
 */
public abstract class AbstractColorCssFunction<T> extends AbstractCssFunction<T> {
    protected final ColorCssConverter converter = new ColorCssConverter();

    public AbstractColorCssFunction(String name) {
        super(name);
    }

    protected @Nullable CssColor parseColorValue(T element, CssTokenizer tt, CssFunctionProcessor<T> functionProcessor) throws IOException, ParseException {
        CssColor color = null;
        switch (tt.next()) {
        case CssTokenType.TT_FUNCTION:
            String name = tt.currentString();
            tt.pushBack();
            List<CssToken> list = new ArrayList<>();
            functionProcessor.processToken(element, tt, list::add, new ArrayDeque<>());
            if (list.isEmpty()) {
                throw new ParseException(getName() + "(): function " + name + "() must return a value.", tt.getStartPosition());
            }
            color = parseResolvedColorValue(element, new ListCssTokenizer(list), functionProcessor);
            break;
        default:
            tt.pushBack();
            color = parseResolvedColorValue(element, tt, functionProcessor);
            break;
        }
        return color;
    }

    protected @Nullable CssColor parseResolvedColorValue(T element, CssTokenizer tt, CssFunctionProcessor<T> functionProcessor) throws IOException, ParseException {
        return converter.parse(tt, null);
    }

}
