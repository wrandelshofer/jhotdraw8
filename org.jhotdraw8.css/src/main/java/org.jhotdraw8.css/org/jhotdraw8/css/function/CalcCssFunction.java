/*
 * @(#)CalcCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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
 * Processes the {@code calc()} function.
 * <pre>
 * calc               = "calc(", calc-sum, ")" ;
 * calc-sum            = calc-product ,  { [ '+' | '-' ] , calc-product } ;
 * calc-product        = calc-value , { '*' , calc-value | '/' , calc-number-value } ;
 * calc-value          = number | dimension | percentage | '(' , calc-sum , ')' ;
 * calc-number-sum     = calc-number-product , { [ '+' | '-' ] calc-number-product } ;
 * calc-number-product = calc-number-value> , { '*' , calc-number-value | '/' , calc-number-value } ;
 * calc-number-value   = number | calc-number-sum ;
 * </pre>
 * In addition, white space is required on both sides of the '+' and '-' operators.
 * (The '*' and '/' operaters can be used without white space around them.)
 * References:
 * <dl>
 *     <dt>CSS Values and Units Module Level 4.
 *     Paragraph 10.1. Basic Arithmetic: calc()</dt>
 *     <dd><a href="https://drafts.csswg.org/css-values/#calc-func">csswg.org</a></dd>
 * </dl>
 * <dl>
 *     <dt>CSS Values and Units Module Level 4.
 *     Paragraph 10.8. Syntax</dt>
 *     <dd><a href="https://drafts.csswg.org/css-values/#calc-syntax">csswg.org</a></dd>
 * </dl>
 *
 * @param <T> the element type of the DOM
 */
public class CalcCssFunction<T> extends AbstractMathCssFunction<T> {
    /**
     * Function name.
     */
    public static final String NAME = "calc";

    public CalcCssFunction() {
        this(NAME);
    }

    public CalcCssFunction(String name) {
        super(name);
    }

    @Override
    public void process(@NonNull T element,
                        @NonNull CssTokenizer tt,
                        @NonNull SelectorModel<T> model,
                        @NonNull CssFunctionProcessor<T> functionProcessor,
                        @NonNull Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {
        int line = tt.getLineNumber();
        int start = tt.getStartPosition();
        CssSize dim = parseCalcFunction(element, tt, functionProcessor);
        int end = tt.getEndPosition();
        produceNumberPercentageOrDimension(out, dim, line, start, end);
    }

    private @Nullable CssSize parseCalcFunction(@NonNull T element, @NonNull CssTokenizer tt, CssFunctionProcessor<T> functionProcessor) throws IOException, ParseException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, getName() + "(): " + getName() + "() function expected.");
        if (!getName().equals(tt.currentStringNonNull())) {
            throw new ParseException(getName() + "(): " + getName() + "() function expected.", tt.getStartPosition());
        }
        CssSize dim = parseCalcSum(element, tt, functionProcessor);
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, getName() + "():  right bracket \")\" expected.");
        return dim;
    }


    @Override
    public String getHelpText() {
        return getName() + "(⟨expression⟩)"
                + "\n    Computes a mathematical expression with addition (+),"
                + " subtraction (-), multiplication (*), and division (/)."
                + "\n    It can be used wherever ⟨length⟩, ⟨frequency⟩, "
                + "⟨angle⟩, ⟨time⟩, ⟨percentage⟩, ⟨number⟩, or ⟨integer⟩ values are allowed. ";
    }
}
