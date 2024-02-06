/*
 * @(#)VarCssFunction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.function;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.manager.CssFunctionProcessor;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.parser.ListCssTokenizer;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Processes the var() function.
 * <pre>
 *     var = "var(" ,  s* , custom-property-name, s* , [ "," ,  s* , declaration-value ] ,  s* , ")" ;
 *     custom-property-name = ident-token;
 *     declaration-value = fallback-value;
 * </pre>
 * The custom-property-name must start with two dashes "--".
 * <p>
 * References:
 * <dl>
 * <dt>CSS Custom Properties for Cascading Variables Module Level 2.
 * Paragraph 3. Using Cascading Variables: the var() notation</dt>
 * <dd><a href="https://drafts.csswg.org/css-variables-2/#using-variables">csswg.org</a></dd>
 * </dl>
 *
 * @param <T> the element type of the DOM
 */
public class VarCssFunction<T> extends AbstractCssFunction<T> {
    /**
     * Function name.
     */
    public static final String NAME = "var";

    public VarCssFunction() {
        this(NAME);
    }

    public VarCssFunction(String name) {
        super(name);
    }

    @Override
    public void process(@NonNull T element, @NonNull CssTokenizer tt, @NonNull SelectorModel<T> model,
                        @NonNull CssFunctionProcessor<T> functionProcessor,
                        @NonNull Consumer<CssToken> out, Deque<CssFunction<T>> recursionStack) throws IOException, ParseException {

        tt.requireNextToken(CssTokenType.TT_FUNCTION, getName() + "(): function var() expected.");
        if (!getName().equals(tt.currentString())) {
            throw tt.createParseException(getName() + "(): function var() expected.");
        }

        tt.requireNextToken(CssTokenType.TT_IDENT, getName() + "(): function custom-property-name expected.");

        String customPropertyName = tt.currentStringNonNull();
        List<CssToken> attrFallback = new ArrayList<>();
        if (tt.next() == CssTokenType.TT_COMMA) {
            while (tt.nextNoSkip() != CssTokenType.TT_EOF && tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
                attrFallback.add(tt.getToken());
            }
        }
        if (tt.current() != CssTokenType.TT_RIGHT_BRACKET) {
            throw tt.createParseException(getName() + "(): right bracket expected.");
        }

        if (!customPropertyName.startsWith("--")) {
            throw tt.createParseException(getName() + "(): custom-property-name starting with two dashes \"--\" expected.");
        }
        ReadOnlyList<CssToken> customValue = functionProcessor.getCustomProperties().get(customPropertyName);
        recursionStack.push(this);
        if (customValue == null) {
            if (attrFallback.isEmpty()) {
                // We have not been able to substitute the value.
                // The value is "invalid at computed-value-time".
                // https://drafts.csswg.org/css-variables/#using-variables
                throw new ParseException(getName() + "(): Could not find a custom property with this name: \"" + customPropertyName + "\".", tt.getStartPosition());
            } else {
                functionProcessor.process(element, new ListCssTokenizer(attrFallback), out, recursionStack);
            }
        } else {
            functionProcessor.process(element, new ListCssTokenizer(customValue), out, recursionStack);
        }
        recursionStack.pop();
    }

    @Override
    public String getHelpText() {
        return NAME + "(⟨custom-property-name⟩, ⟨fallback⟩)"
                + "\n    Retrieves a custom-property by name."
                + "\n    If the custom-property is not found, the fallback is used."
                + "\n    A custom-property is a property defined on a parent element (or on ':root')."
                + "\n    The name of a custom-property must start with two dashes: '--'.";

    }

}
