/*
 * @(#)CssFunction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.CssFunctionProcessor;
import org.jhotdraw8.css.CssToken;
import org.jhotdraw8.css.CssTokenizer;
import org.jhotdraw8.css.SelectorModel;

import java.io.IOException;
import java.text.ParseException;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * Interface for CSS macro functions. A CSS macro function processes
 * CssTokens.
 *
 * @param <T> the element type of the DOM
 */
public interface CssFunction<T> {
    /**
     * Processes the function.
     *
     * @param element           the DOM element
     * @param tt                the tokenizer providing the unprocessed tokens
     * @param model             the selector model
     * @param functionProcessor the function processor
     * @param out               the consumer for the processed tokens
     * @param recursionStack    the recursion stack
     * @throws IOException    on IO failure
     * @throws ParseException on parsing failure
     */
    void process(@NonNull T element,
                 @NonNull CssTokenizer tt,
                 @NonNull SelectorModel<T> model,
                 @NonNull CssFunctionProcessor<T> functionProcessor,
                 @NonNull Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException;


    /**
     * Gets localized help text about this function.
     *
     * @return localized help text
     */
    String getHelpText();

    /**
     * Returns the function name.
     *
     * @return the function name
     */
    String getName();

}
