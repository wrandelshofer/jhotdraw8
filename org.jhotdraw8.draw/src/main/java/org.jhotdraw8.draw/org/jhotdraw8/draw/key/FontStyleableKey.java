/*
 * @(#)FontStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.draw.css.converter.CssFontConverter;
import org.jhotdraw8.draw.css.value.CssFont;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;

/**
 * FontStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class FontStyleableKey extends AbstractStyleableKey<CssFont> implements WritableStyleableMapAccessor<CssFont> {

    private static final long serialVersionUID = 1L;

    private final Converter<CssFont> converter = new CssFontConverter(false);

    /**
     * Creates a new instance with the specified name and with null as the
     * default value.
     *
     * @param name The name of the key.
     */
    public FontStyleableKey(String name) {
        this(name, null);
    }

    /**
     * Creates a new instance with the specified name and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public FontStyleableKey(String name, CssFont defaultValue) {
        super(name, CssFont.class, defaultValue);
    }

    @Override
    public @NonNull Converter<CssFont> getCssConverter() {
        return converter;
    }
}
