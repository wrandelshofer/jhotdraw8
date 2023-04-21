/*
 * @(#)RoundCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;

import java.io.IOException;
import java.text.ParseException;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * Processes the round() function.
 * <pre>
 * round              = "round(", value, ")" ;
 * value               = number | dimension | percentage ;
 * </pre>
 *
 * @param <T> the element type of the DOM
 */
public class RoundCssFunction<T> extends CalcCssFunction<T> {
    /**
     * Function name.
     */
    public static final String NAME = "round";

    public RoundCssFunction(String name) {
        super(name);
    }

    public RoundCssFunction() {
        this(NAME);
    }


    @Override
    public void process(@NonNull T element, @NonNull CssTokenizer tt,
                        @NonNull SelectorModel<T> model, @NonNull CssFunctionProcessor<T> functionProcessor,
                        @NonNull Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        int line = tt.getLineNumber();
        int start = tt.getStartPosition();
        tt.requireNextToken(CssTokenType.TT_FUNCTION, getName() + "():  " + getName() + "() function expected.");
        if (!getName().equals(tt.currentStringNonNull())) {
            throw new ParseException(getName() + "():  " + getName() + "() function expected.", tt.getStartPosition());
        }
        CssSize dim = parseCalcValue(element, tt, functionProcessor);
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, getName() + "():  right bracket \")\" expected.");
        int end = tt.getEndPosition();

        CssSize rounded = CssSize.of(Math.round(dim.getValue()), dim.getUnits());
        produceNumberPercentageOrDimension(out, rounded, line, start, end);
    }

    @Override
    public String getHelpText() {
        return NAME + "(⟨value⟩)"
                + "\n    Rounds the specified value."
                + "\n    The value can be given as a number, dimension or a percentage.";

    }
}
