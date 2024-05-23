/*
 * @(#)CssLocatorConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.draw.locator.BoundsLocator;
import org.jhotdraw8.draw.locator.Locator;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * CssLocatorConverter.
 * <p>
 * Currently converts relative locators only.
 *
 * @author Werner Randelshofer
 */
public class LocatorCssConverter extends AbstractCssConverter<Locator> {
    public static final String RELATIVE_FUNCTION = "relative";

    public LocatorCssConverter() {
        this(false);
    }


    public LocatorCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public Locator parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "Locator: function 'relative(' expected.");
        if (!RELATIVE_FUNCTION.equals(tt.currentStringNonNull())) {
            throw tt.createParseException("Locator: function 'relative(' expected.");
        }

        double x, y;
        x = switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().doubleValue();
            case CssTokenType.TT_PERCENTAGE -> tt.currentNumberNonNull().doubleValue() / 100.0;
            default -> throw tt.createParseException("BoundsLocator: x-value expected.");
        };
        switch (tt.next()) {
        case ',':
            break;
        default:
            tt.pushBack();
            break;
        }
        y = switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().doubleValue();
            case CssTokenType.TT_PERCENTAGE -> tt.currentNumberNonNull().doubleValue() / 100.0;
            default -> throw tt.createParseException("BoundsLocator: y-value expected.");
        };
        tt.requireNextToken(CssTokenType.TT_RIGHT_BRACKET, "BoundsLocator: ')' expected.");

        return new BoundsLocator(x, y);
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Locator⟩: relative(⟨x⟩%,⟨y⟩%)";
    }

    @Override
    protected <TT extends Locator> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        if (value instanceof BoundsLocator rl) {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, RELATIVE_FUNCTION));
            out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, rl.getRelativeX() * 100));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, rl.getRelativeY() * 100));
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        } else {
            throw new UnsupportedOperationException("only BoundsLocator supported, value:" + value);
        }
    }
}
