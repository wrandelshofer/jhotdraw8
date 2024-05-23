/*
 * @(#)CalcCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides protected methods for processing the following productions:
 * <pre>
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
 *     Paragraph 10.8. Syntax</dt>
 *     <dd><a href="https://drafts.csswg.org/css-values/#calc-syntax">csswg.org</a></dd>
 * </dl>
 *
 * @param <T> the element type of the DOM
 */
public abstract class AbstractMathCssFunction<T> extends AbstractCssFunction<T> {

    public AbstractMathCssFunction(String name) {
        super(name);
    }


    protected @Nullable CssSize parseCalcSum(T element, CssTokenizer tt, CssFunctionProcessor<T> functionProcessor) throws IOException, ParseException {
        CssSize dim = parseCalcProduct(element, tt, functionProcessor);
        DefaultUnitConverter c = DefaultUnitConverter.getInstance();
        Loop:
        for (; ; ) {
            switch (tt.next()) {
                case '+': {
                    CssSize dim2 = parseCalcProduct(element, tt, functionProcessor);
                    if (dim2.getUnits().equals(UnitConverter.DEFAULT)
                            || dim.getUnits().equals(dim2.getUnits())) {
                        dim = CssSize.of(dim.getValue() + dim2.getValue(), dim.getUnits());
                    } else {
                        dim = CssSize.of(dim.getValue() + c.convert(dim2, dim.getUnits()), dim.getUnits());
                    }
                    break;
                }
                case '-': {
                    CssSize dim2 = parseCalcProduct(element, tt, functionProcessor);
                    if (dim2.getUnits().equals(UnitConverter.DEFAULT)
                            || dim.getUnits().equals(dim2.getUnits())) {
                        dim = CssSize.of(dim.getValue() - dim2.getValue(), dim.getUnits());
                    } else {
                        dim = CssSize.of(dim.getValue() - c.convert(dim2, dim.getUnits()), dim.getUnits());
                    }
                    break;
                }
                default:
                    tt.pushBack();
                    break Loop;
            }
        }
        return dim;
    }

    protected @Nullable CssSize parseCalcProduct(T element, CssTokenizer tt, CssFunctionProcessor<T> functionProcessor) throws IOException, ParseException {
        CssSize dim = parseCalcValue(element, tt, functionProcessor);
        DefaultUnitConverter c = DefaultUnitConverter.getInstance();
        Loop:
        for (; ; ) {
            switch (tt.next()) {
                case '*': {
                    CssSize dim2 = parseCalcProduct(element, tt, functionProcessor);
                    if (dim2.getUnits().equals(UnitConverter.DEFAULT)
                            || dim.getUnits().equals(dim2.getUnits())) {
                        dim = CssSize.of(dim.getValue() * dim2.getValue(), dim.getUnits());
                    } else {
                        dim = c.convertSize(CssSize.of(dim.getConvertedValue() * dim2.getConvertedValue(), UnitConverter.DEFAULT), dim.getUnits());
                    }
                    break;
                }
                case '/': {
                    CssSize dim2 = parseCalcProduct(element, tt, functionProcessor);
                    if (dim2.getUnits().equals(UnitConverter.DEFAULT)
                            || dim.getUnits().equals(dim2.getUnits())) {
                        dim = CssSize.of(dim.getValue() / dim2.getValue(), dim.getUnits());
                    } else {
                        dim = c.convertSize(CssSize.of(dim.getConvertedValue() / dim2.getConvertedValue(), UnitConverter.DEFAULT), dim.getUnits());
                    }
                    break;
                }
                default:
                    tt.pushBack();
                    break Loop;
            }
        }
        return dim;
    }

    protected void produceNumberPercentageOrDimension(Consumer<CssToken> out, CssSize dim, int line, int start, int end) {
        if ("%".equals(dim.getUnits())) {
            out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, null, dim.getValue(), line, start, end));
        } else {
            out.accept(new CssToken(CssTokenType.TT_DIMENSION, dim.getUnits(), dim.getValue(), line, start, end));
        }
    }

    protected @Nullable CssSize parseCalcValue(T element, CssTokenizer tt, CssFunctionProcessor<T> functionProcessor) throws IOException, ParseException {
        switch (tt.next()) {
            case CssTokenType.TT_NUMBER:
                return CssSize.of(tt.currentNumberNonNull().doubleValue());
            case CssTokenType.TT_PERCENTAGE:
                return CssSize.of(tt.currentNumberNonNull().doubleValue(), "%");
            case CssTokenType.TT_DIMENSION:
                return CssSize.of(tt.currentNumberNonNull().doubleValue(), tt.currentStringNonNull());
            case '(':
                CssSize dim = parseCalcSum(element, tt, functionProcessor);
                tt.requireNextToken(')', getName() + "(): right bracket ')' expected.");
                return dim;
            case CssTokenType.TT_FUNCTION:
                String name = tt.currentString();
                tt.pushBack();
                List<CssToken> list = new ArrayList<>();
                functionProcessor.processToken(element, tt, list::add, new ArrayDeque<>());
                if (list.size() != 1) {
                    throw new ParseException(getName() + "(): function " + name + "() must return single value.", tt.getStartPosition());
                }
                CssToken token = list.getFirst();
                return switch (token.getType()) {
                    case CssTokenType.TT_NUMBER -> CssSize.of(token.getNumericValue().doubleValue());
                    case CssTokenType.TT_PERCENTAGE -> CssSize.of(token.getNumericValue().doubleValue(), "%");
                    case CssTokenType.TT_DIMENSION ->
                            CssSize.of(token.getNumericValue().doubleValue(), token.getStringValue());
                    default ->
                            throw new ParseException(getName() + "(): function " + name + "() must return numeric value.", tt.getStartPosition());
                };
            default:
                throw tt.createParseException(getName() + "(): number, percentage, dimension or (sum) expected.");
        }
    }
}
