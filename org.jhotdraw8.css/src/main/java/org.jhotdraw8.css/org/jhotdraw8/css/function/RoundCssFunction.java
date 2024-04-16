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
import org.jhotdraw8.css.value.DefaultUnitConverter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Processes the {@code round()} function.
 * <pre>
 * round               = "round(", [ rounding-strategy , "," ] , calc-sum , [ "," , calc-sum ] ")" ;
 * rounding-strategy   = "nearest" | "up" | "down" | "to-zero" ;
 * calc-sum            = (* see superclass *)
 * </pre>
 * <dl>
 *     <dt>CSS Values and Units Module Level 4.
 *     Paragraph 10.3. Stepped Value Functions: round(), mod(), and rem()</dt>
 *     <dd><a href="https://drafts.csswg.org/css-values/#round-func">csswg.org</a></dd>
 * </dl>
 * <dl>
 *     <dt>CSS Values and Units Module Level 4.
 *     Paragraph 10.8. Syntax</dt>
 *     <dd><a href="https://drafts.csswg.org/css-values/#calc-syntax">csswg.org</a></dd>
 * </dl>
 *
 * @param <T> the element type of the DOM
 */
public class RoundCssFunction<T> extends CalcCssFunction<T> {
    /**
     * Function name.
     */
    public static final @NonNull String NAME = "round";

    public RoundCssFunction(@NonNull String name) {
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
        String roundingStrategy;
        if (tt.next() == CssTokenType.TT_IDENT) {
            roundingStrategy = tt.currentString();
        } else {
            roundingStrategy = null;
            tt.pushBack();
        }
        CssSize dimA = parseCalcValue(element, tt, functionProcessor);
        CssSize dimB;
        if (tt.next() == CssTokenType.TT_COMMA) {
            dimB = parseCalcValue(element, tt, functionProcessor);
        } else {
            dimB = CssSize.ONE;
            tt.pushBack();
        }
        if (dimB.getUnits().isEmpty()) {
            dimB = CssSize.of(dimB.getValue(), dimA.getUnits());
        }

        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, getName() + "():  right bracket \")\" expected.");
        int end = tt.getEndPosition();

        final double valueA = Objects.equals(dimA.getUnits(), dimB.getUnits()) ? dimA.getValue() : dimA.getConvertedValue(DefaultUnitConverter.getInstance(), dimB.getUnits());
        final double valueB = dimB.getValue();
        CssSize rounded = CssSize.of(switch (roundingStrategy) {
            case "up" -> Math.ceil(valueA / valueB) * valueB;
            case "down" -> Math.floor(valueA / valueB) * valueB;
            case "to-zero" -> (valueA < 0)
                    ? Math.ceil(valueA / valueB) * valueB
                    : Math.floor(valueA / valueB) * valueB;
            case null, default -> Math.round(valueA / valueB) * valueB;
        }, dimB.getUnits());

        produceNumberPercentageOrDimension(out, rounded, line, start, end);
    }

    @Override
    public String getHelpText() {
        return NAME + "(⟨value⟩)"
                + "\n    Rounds the specified value."
                + "\n    The value can be given as a number, dimension or a percentage.";

    }
}
