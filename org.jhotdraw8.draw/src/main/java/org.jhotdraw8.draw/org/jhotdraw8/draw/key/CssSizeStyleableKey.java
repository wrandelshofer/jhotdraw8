/*
 * @(#)CssSizeStyleableKey.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * CssSizeStyleableKey.
 *
 */
public class CssSizeStyleableKey extends AbstractStyleableKey<CssSize> implements WritableStyleableMapAccessor<CssSize>,
        NonNullKey<CssSize> {

    private static final long serialVersionUID = 1L;

    private final Converter<CssSize> converter = new SizeCssConverter(false);


    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public CssSizeStyleableKey(String name, CssSize defaultValue) {
        super(name, CssSize.class, defaultValue);

    }


    @Override
    public Converter<CssSize> getCssConverter() {
        return converter;
    }

}
