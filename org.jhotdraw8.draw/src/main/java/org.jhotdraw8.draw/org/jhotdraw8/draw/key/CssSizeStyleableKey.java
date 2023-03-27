/*
 * @(#)CssSizeStyleableKey.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.key;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.converter.CssSizeConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;

/**
 * CssSizeStyleableKey.
 *
 * @author Werner Randelshofer
 */
public class CssSizeStyleableKey extends AbstractStyleableKey<@NonNull CssSize> implements WritableStyleableMapAccessor<@NonNull CssSize>,
        NonNullKey<@NonNull CssSize> {

    static final long serialVersionUID = 1L;

    private final Converter<@NonNull CssSize> converter = new CssSizeConverter(false);


    /**
     * Creates a new instance with the specified name, mask and default value.
     *
     * @param name         The name of the key.
     * @param defaultValue The default value.
     */
    public CssSizeStyleableKey(String name, @NonNull CssSize defaultValue) {
        super(name, CssSize.class, defaultValue);

    }


    @Override
    public @NonNull Converter<CssSize> getCssConverter() {
        return converter;
    }

}
